/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.beandoc;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.*;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.springframework.beandoc.output.Decorator;
import org.springframework.beandoc.output.Tags;
import org.springframework.beandoc.output.Transformer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class DefaultContextProcessor implements ContextProcessor {
    
    private Log logger = LogFactory.getLog(getClass());

    private boolean validateFiles = true;
    
    private File outputDir;
    
    private Resource[] inputFiles;
    
    private Map beanMap;
    
    private List transformers;
    
    private List decorators;

    private List ignoreBeans = new LinkedList();

    /**
     * Convert string values to actual resources
     * 
     * @param inputFileNames
     * @return
     */
    private static Resource[] getResources(String[] inputFileNames) throws IOException {
        // resolve resources assuming Files as the default (rather than classpath resources)
        ResourcePatternResolver resolver = 
            new PathMatchingResourcePatternResolver(new DefaultFileSystemResourceLoader());
        List allResources = new ArrayList();
        
        // each input location could resolve to multiple Resources..
        for (int i = 0; i < inputFileNames.length; i++) {
            Resource[] resources = resolver.getResources(inputFileNames[i]);
            allResources.addAll(Arrays.asList(resources));
        }

        File outputDir = new File(inputFileNames[inputFileNames.length - 1]);
        Resource[] inputFiles = (Resource[]) 
            allResources.toArray(new Resource[allResources.size()]);
            
        return inputFiles;
    }

    /**
     * Construct with an array of Spring Resources used as input files for the program
     * 
     * @param inputFiles
     * @param outputDir
     */
    public DefaultContextProcessor(Resource[] inputFiles, File outputDir) throws IOException {
        this.inputFiles = inputFiles;
        this.outputDir = outputDir;

        if (!outputDir.canWrite() || !outputDir.isDirectory())
            throw new IOException(
                "Unable to find or write to output directory [" + outputDir.getAbsolutePath() + "]"
            );
    }

    /**
     * Construct with an array of resource names that will resolve to one or more input
     * resources using standard Spring Resource resolution strategies.
     * 
     * @param inputFileNames
     * @param outputDir
     */
    public DefaultContextProcessor(String[] inputFileNames, File outputDir) throws IOException {
        this(getResources(inputFileNames), outputDir);
    }

    /**
     * Construct with an array of resource names that will resolve to one or more input
     * resources using standard Spring Resource resolution strategies.
     * 
     * @param inputFileNames
     * @param outputDirName
     */
    public DefaultContextProcessor(String[] inputFileNames, String outputDirName) throws IOException {
        this(getResources(inputFileNames), new File(outputDirName));
    }
    
    /**
     * Handles each input file in turn, parsing the XML (which must validate against
     * the DTD unless validation has been turned off (see {@link #setValidateFiles})
     * creating an array of in-memory DOM documents of all input files. 
     * The method adds an attribute to each tag that points to another bean such as
     * &lt;ref/&gt; tags.  This attribute contains the file name of the input file that
     * contains the bean definition and can be used to link bean definitions for example
     * in HTML documentation.
     * <p> 
     * The array of Document objects is passed to each <code>Decorator</code>
     * configured for use which can incrementally modify the attributes in the DOM trees.
     * <p>
     * The process method is threadsafe, synchronizing on the current instance during 
     * the execution of documentation output.  A configured DefaultContextProcessor
     * is therefore re-usable from client code, and the configuration properties can be 
     * modified between calls to the process method.
     * 
     * @throws IOException if the input files cannot be read, if the output 
     *      directory does not exist or is not writable, or if media files cannot
     *      be copied from the classpath to the output directory.
     * @throws BeanDocException of the input files do not validate against the
     *      DTD, if a problem occurs attempting to create graphs from the .dot
     *      files (such as the GraphViz program being unavailable) or other
     *      unknown problem occurs.
     * @see org.springframework.beandoc.ContextProcessor#process()
     */
    public void process() throws IOException, BeanDocException {
        // debug logging will cause all of the config to be output upon construction,
        // there's no value in repeating any of it in here.    
        logger.info("Processing input files..");
        
        synchronized (this) {
            // build jdom docs from input files
            Document[] contextDocuments = buildDomsFromInputFiles();      
            
            // generate the Map of bean names to context file locations
            beanMap = generateBeanNameMap(contextDocuments);
            
            // add filename references for all beans to link references defined in 
            // separate files
            Filter filter = new ContentFilter(ContentFilter.ELEMENT);
            for (int i = 0; i < contextDocuments.length; i++) {
                Document doc = contextDocuments[i];
                markupBeanReferences(doc.getDescendants(filter));
            }
            
            // decorate beans with attributes that may be used by multiple Transformers
            if (decorators != null && decorators.size() > 0)
                for (Iterator i = decorators.iterator(); i.hasNext();)
                    ((Decorator) i.next()).decorate(contextDocuments);
                
            // apply transformers to generate output
            if (transformers != null && transformers.size() > 0)
                for (Iterator i = transformers.iterator(); i.hasNext();)
                    ((Transformer) i.next()).transform(contextDocuments, outputDir);          
                  
        }
        
        logger.info("Processing complete.");
    }

    /**
     * generate in memory DOM representations from the input files on disk
     * 
     * @return a Document[]
     * @throws IOException
     */
    private Document[] buildDomsFromInputFiles() throws IOException {
        logger.debug("Starting building DOM trees fro input files");
        Document[] contextDocuments = new Document[inputFiles.length];
        
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(validateFiles);
        
        // process each context file, decorating and consolidating.  
        for (int i = 0; i < inputFiles.length; i++) {
            String fileName = inputFiles[i].getFilename();
            logger.info("  building [" + fileName + "]");
            
            try {
                contextDocuments[i] = builder.build(inputFiles[i].getInputStream());
            } catch (JDOMException e) {
                throw new BeanDocException(
                    "Unable to parse or validate input resource [" + fileName + "]", e);
            }
            
            // remove the DTD as our output no longer subscribes to it.
            contextDocuments[i].removeContent(contextDocuments[i].getDocType());
            
            // kill comments for efficiency
            Filter filter = new ContentFilter(ContentFilter.COMMENT | ContentFilter.TEXT);
            contextDocuments[i].getRootElement().removeContent(filter);
            contextDocuments[i].removeContent(filter);
            
            // set an attribute on the root element to mark the original input file
            Element root = contextDocuments[i].getRootElement();
            root.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, fileName);
            
            // force a description even if empty
            if (root.getChild(Tags.TAGNAME_DESCRIPTION) == null) {
                Element description = new Element(Tags.TAGNAME_DESCRIPTION);
                description.setText("[Empty Description]");
                root.addContent(description);
            }
        }
        return contextDocuments;
    }

    /**
     * generate a Map of bean names pointing to the original file
     * name that the bean was defined in.  This allows transformers
     * such as the HTML generator to create links between beans in
     * different input files. 
     */
    private Map generateBeanNameMap(Document[] contextDocuments) {
        Map beanMap = new HashMap();
        for (int i = 0; i < contextDocuments.length; i++) {
            Document doc = contextDocuments[i];
            List beans = doc.getRootElement().getChildren();
            String fileName = doc.getRootElement().getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
        
            for (Iterator j = beans.iterator(); j.hasNext();) {
                Element bean = (Element) j.next();
                String idRef = getBeanIdentifier(bean);
                if (idRef != null && "bean".equals(bean.getName()))  
                    beanMap.put(idRef, fileName);                
            }
        }
        
        logger.debug("Map generated: " + beanMap);        
        return beanMap;
    }

    /**
     * return a String representing the bean identifier - either its id attribute
     * or its name attribute.  Returns null if the bean is anonymous.
     * 
     * @param bean
     * @return
     */
    private String getBeanIdentifier(Element bean) {
        String id = bean.getAttributeValue(Tags.ATTRIBUTE_ID);
        if (id == null) return bean.getAttributeValue(Tags.ATTRIBUTE_NAME);
        return id;
    }

    /**
     * add filename references to <ref/> elements and anything else that may need them
     * 
     * @param element
     */
    private void markupBeanReferences(Iterator iter) {
        
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            // process element
            String tag = element.getName();
            String referencedBean;
            try {
                if (Tags.TAGNAME_REF.equals(tag) || Tags.TAGNAME_IDREF.equals(tag)) {            
                    referencedBean = element.getAttributeValue("bean");
                    if (referencedBean != null)
                        element.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(referencedBean));
                    else
                        element.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(element.getAttributeValue("local")));
                    
                }
                    
                if (Tags.TAGNAME_BEAN.equals(tag) && element.getAttribute(Tags.ATTRIBUTE_PARENT) != null) {
                    element.setAttribute(
                    Tags.ATTRIBUTE_BD_FILENAME, 
                        (String) beanMap.get(element.getAttributeValue(Tags.ATTRIBUTE_PARENT))
                    );
                    logger.debug(
                        "decorated " + element + 
                        " with [" + Tags.ATTRIBUTE_BD_FILENAME + 
                        "=" + element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME) + "]"
                    );
                }
                
            } catch (IllegalDataException ide) {
                logger.error("Failed to decorate element " + element, ide);
            }
        }
        
    }

    /**
     * Set to false to prevent the XML parser validating input files against a DTD.
     * 
     * @param validateFiles set to true to enable validation, false otherwise.  True by default.
     */
    public void setValidateFiles(boolean validateFiles) {
        this.validateFiles = validateFiles;
    }

    /**
     * The <code>File</code> representing an output directory that the beandoc tool will use for
     * outputting HTML and graph images.
     * 
     * @return the directory that output will be written to.
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * The input resources (files, classpath resources) used as the actual inputs to the 
     * beandoc tool.  Typical non-trivial application contexts will be made up of two or
     * more resources.
     * 
     * @return the array of input resources that make up the application context being
     *      documented.
     */
    public Resource[] getInputFiles() {
        return inputFiles;
    }

    /**
     * Input files can optionally be validated against a DTD in the XML file.  True by
     * default.
     * 
     * @return true if input files should be validated against a DTD, false otherwise.
     */
    public boolean isValidateFiles() {
        return validateFiles;
    }

    /**
     * @return the List of Transformer objects that will be applied to the context
     *      files and generate the output.
     */
    public List getTransformers() {
        return transformers;
    }

    /**
     * Set a List of Transformers.  Each of these will be applied in turn by the 
     * process() method in order to generate output.
     * 
     * @param list the List of Transformer objects
     */
    public void setTransformers(List list) {
        transformers = list;
    }

    /**
     * @return the List of Decorator implementations that will have an opportunity
     * to markup the DOM trees with additional attributes or elements based on the
     * configuration
     */
    public List getDecorators() {
        return decorators;
    }

    /**
     * @param list the List of Decorator implementations that will have an opportunity
     * to markup the DOM trees with additional attributes or elements based on the
     * configuration
     */
    public void setDecorators(List list) {
        decorators = list;
    }

    /**
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the output.
     * <p>
     * The returned underlying <code>List</code> is modifiable and will, if modified, affect
     * subsequent calls to the <code>ContextProcessor</code>'s <code>process()</code> method if
     * you are using the tool programmatically.  The preferred way to modify this list is 
     * through the {@link #addIgnoreBeans} convenience method.
     * 
     * @return a <code>List</code> of patterns of bean names to be excluded from graphs
     * @see #addIgnoreBeans
     * @see #isBeanIgnored
     */
    public List getIgnoreBeans() {
        return ignoreBeans;
    }

    /**
     * Patterns of bean or classnames can be used to indicate that some beans should be
     * excluded from the output.
     * 
     * @return true if the bean should be ignored on graphing output, false
     *      otherwise.
     * @see #addIgnoreBeans
     */
    public boolean isBeanIgnored(String idOrName, String className) {
        
        String[] ignored = (String[]) ignoreBeans.toArray(new String[ignoreBeans.size()]);
        for (int i = 0; i < ignored.length; i++) {
            String key = ignored[i];
            if (
                (key.startsWith("*") && 
                    ((idOrName != null && idOrName.endsWith(key.substring(1))) || 
                    (className != null && className.endsWith(key.substring(1)))))
                ||
                (key.endsWith("*") && 
                    ((idOrName != null && idOrName.startsWith(key.substring(0, key.length() - 1))) || 
                    (className != null && className.startsWith(key.substring(0, key.length() - 1)))))
                ||
                (key.equals(idOrName) || key.equals(className))
            )
                return true;
        }
        return false;
    }

    /**
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the output documents.  The preferred way to modify this list is 
     * through the {@link #addIgnoreBeans} convenience method.
     * 
     * @param list a <code>List</code> of patterns of bean names to be excluded from graphs
     * @see #addIgnoreBeans
     */
    public void setIgnoreBeans(List list) {
        ignoreBeans = list;
    }    

    /**
     * Add a naming pattern of bean id's or bean names or classnames that should not be displayed
     * on output.  Some beans (such as PropertyConfigurers and MessageSources)
     * are auxilliary and you may wish to exclude them from documents to keep the output
     * focused.
     * <p>
     * This method may be called any number of times to add different patterns to the
     * list of ignored beans.  Pattern may not be null (such a value will be ignored).
     * 
     * @param pattern a String representing a pattern to match.  The pattern can be prefixed or
     *      suffixed with a wildcard (*) but does not use RegEx matching.  May not be null
     */
    public void addIgnoreBeans(String pattern) {
        if (pattern != null) ignoreBeans.add(pattern);
    }

}
