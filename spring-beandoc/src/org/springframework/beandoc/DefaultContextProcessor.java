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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.springframework.beandoc.output.Decorator;
import org.springframework.beandoc.output.DocumentCompiler;
import org.springframework.beandoc.output.Tags;
import org.springframework.beandoc.output.Transformer;
import org.springframework.beandoc.util.BeanDocUtils;
import org.springframework.beandoc.util.MatchedPatternCallback;
import org.springframework.beandoc.util.PatternMatcher;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;


/**
 * Default implementation of the <code>ContextProcessor</code> interface that
 * generates documentation in a file system directory. Input context files can
 * be any resolveable Spring <code>Resource</code>.
 * <p>
 * This class maintains a <code>List</code> of <code>Decorator</code>
 * objects and a <code>List</code> of <code>Transformer</code> objects that
 * it manages and uses to conduct most of the actual output. Input resources are
 * verified and loaded into memory before being cleaned of XML comments and
 * extra whitespace. Bean references are marked with an additional attribute
 * denoting the original name of the file that they were found in prior to each
 * <code>Decorator</code> being applied in turn. Following decoration of the
 * DOM trees, each <code>Transformer</code> is subsequently applied to the
 * array of input DOM's.
 * <p>
 * The processor offers the ability to ignore some beans in the context by means
 * of simple pattern matching. Ignored bean definitions are stripped from the
 * DOM prior to decoration and transformation and will therefore not show up in
 * any output.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class DefaultContextProcessor implements ContextProcessor {
    
    private static final Object synchLock = new Object();
    
    private Log logger = LogFactory.getLog(getClass());

    private boolean validateFiles = true;
    
    private Map mergeProxies = new HashMap();
    
    private File outputDir;
    
    private Resource[] inputFiles;
    
    private Map beanMap;
    
    private List transformers;
    
    private List decorators;
    
    private List compilers;
    
    private static Filter beanFilter = new ElementFilter(Tags.TAGNAME_BEAN);
        

    /**
     * Construct with an array of Spring Resources used as input files for the program
     * 
     * @param inputFiles
     * @param outputDir
     */
    protected DefaultContextProcessor(Resource[] inputFiles, File outputDir) throws IOException {
        this.inputFiles = inputFiles;
        this.outputDir = outputDir;

        if (logger.isDebugEnabled()) {
            String resourceNames = "";
            for (int i = 0; i < inputFiles.length; i++)
                resourceNames += (inputFiles[i].getFile().getAbsolutePath() + ",");
            logger.debug("Attempting to construct with input files [" + 
                resourceNames + "] and output directory [" + outputDir.getAbsolutePath() + "]");
        }
        
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
        this(BeanDocUtils.getResources(inputFileNames), outputDir);
    }

    /**
     * Construct with an array of resource names that will resolve to one or
     * more input resources using standard Spring Resource resolution
     * strategies.
     * 
     * @param inputFileNames
     * @param outputDirName
     */
    public DefaultContextProcessor(String[] inputFileNames, String outputDirName) throws IOException {
        this(BeanDocUtils.getResources(inputFileNames), new File(outputDirName));
    }
    
    /**
     * Handles each input file in turn, parsing the XML (which must validate
     * against the DTD unless validation has been turned off (see
     * {@link #setValidateFiles}) creating an array of in-memory DOM documents
     * of all input files. The method adds an attribute to each tag that points
     * to another bean such as &lt;ref/&gt; tags. This attribute contains the
     * file name of the input file that contains the bean definition and can be
     * used to link bean definitions for example in HTML documentation.
     * <p>
     * The array of Document objects is passed to each <code>Decorator</code>
     * configured for use which can incrementally modify the attributes in the
     * DOM trees.
     * <p>
     * The process method is threadsafe, synchronizing on a private lock during
     * the execution of documentation output. A configured
     * DefaultContextProcessor is therefore re-usable from client code, and the
     * configuration properties can be modified between calls to the process
     * method.
     * 
     * @throws IOException if the input files cannot be read, if the output
     *         directory does not exist or is not writable, or if media files
     *         cannot be copied from the classpath to the output directory.
     * @throws BeanDocException of the input files do not validate against the
     *         DTD, if a problem occurs attempting to create graphs from the
     *         .dot files (such as the GraphViz program being unavailable) or
     *         other unknown problem occurs.
     * @see org.springframework.beandoc.ContextProcessor#process()
     */
    public void process() throws IOException, BeanDocException {
        // debug logging will cause all of the config to be output upon construction,
        // there's no value in repeating any of it in here.    
        logger.info("Processing input files..");
        
        synchronized (synchLock) {
            // build jdom docs from input files
            Document[] contextDocuments = buildDomsFromInputFiles();      
            
            // optionally merge some proxies and their target beans
            if (mergeProxies.size() > 0) mergeProxiesInContextDocs(contextDocuments);
            
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
                    
            // apply compilers to build final output
            if (compilers != null && compilers.size() > 0)
                for (Iterator i = compilers.iterator(); i.hasNext();)
                    ((DocumentCompiler) i.next()).compile(contextDocuments, outputDir);                      
                  
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
        logger.debug("Starting building DOM trees from input files");
        Document[] contextDocuments = new Document[inputFiles.length];
        
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new BeansDtdResolver());
        builder.setValidation(validateFiles);
        logger.debug("Input file validation is set to [" + validateFiles + "]");
        
        // two or more input files may have the same name but different paths.
        String[] normalisedFileNames = BeanDocUtils.normaliseFileNames(inputFiles);
        
        // process each context file, decorating and consolidating.  
        for (int i = 0; i < inputFiles.length; i++) {
            logger.info("  building [" + normalisedFileNames[i] + "]");
            
            try {
                contextDocuments[i] = builder.build(inputFiles[i].getInputStream());
            } catch (JDOMException e) {
                throw new BeanDocException(
                    "Unable to parse or validate input resource [" + normalisedFileNames[i] + "]", e);
            }
            
            // remove the DTD as our output no longer subscribes to it.
            contextDocuments[i].removeContent(contextDocuments[i].getDocType());
            
            // kill comments for efficiency
            Filter filter = new ContentFilter(ContentFilter.COMMENT | ContentFilter.TEXT);
            contextDocuments[i].getRootElement().removeContent(filter);
            contextDocuments[i].removeContent(filter);
            logger.debug("Extraneous content removed");
            
            // set an attribute on the root element to mark the original input file
            Element root = contextDocuments[i].getRootElement();
            root.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, normalisedFileNames[i]);
            logger.debug("Attribute [" + Tags.ATTRIBUTE_BD_FILENAME + "] set to [" + normalisedFileNames[i] + "]");
            
            logger.debug("Checking for imports in [" + normalisedFileNames[i] + "]");
            handleImports(root, builder); 
            
            // set a root attribute denoting the path relative to the output root
            String relativePath = BeanDocUtils.getRelativePath(normalisedFileNames[i]);
            root.setAttribute(Tags.ATTRIBUTE_BD_PATHRELATIVE, relativePath);
            logger.debug("Attribute [" + Tags.ATTRIBUTE_BD_PATHRELATIVE + "] set to [" + normalisedFileNames[i] + "]");
            
            // force a description even if empty
            Element desc = root.getChild(Tags.TAGNAME_DESCRIPTION);
            if (desc == null || desc.getText().equals("")) {
                desc = new Element(Tags.TAGNAME_DESCRIPTION);
                desc.setText("[Empty Description]");
                root.addContent(desc);
            }
        }
        return contextDocuments;
    }
    
    /**
     * Handles a list of import tags from the Document currently being
     * processed. Adds all beans from the imported resource as children of the
     * rootElement.
     * <p>
     * This method is recursive, repeatedly searching deeper levels of imports
     * with no maximum depth.
     * 
     * @param rootElement the root Element to add imported beans to
     * @param builder a pre-initialized builder to build the Document
     * @throws IOException if the resource cannot be read
     */
    private void handleImports(Element rootElement, SAXBuilder builder) throws IOException {
        Iterator importIter = rootElement.getDescendants(
            new ElementFilter(Tags.TAGNAME_IMPORT));
        if (importIter.hasNext()) {
            List importedBeans = new ArrayList();
            ResourceLoader loader = new DefaultResourceLoader();
            
            while (importIter.hasNext()) {
                Element includedImport = (Element) importIter.next();
                String importfile = includedImport.getAttributeValue(Tags.ATTRIBUTE_RESOURCE);
                logger.debug("Found import reference [" + importfile + "]");
                Resource res = loader.getResource(importfile);
                try {
                    Document importDocument = builder.build(res.getInputStream());
                    handleImports(importDocument.getRootElement(), builder);
                    Iterator beanIter = importDocument.getRootElement().getDescendants(beanFilter);
                    
                    // can't directly add to the root element - must save temporarily
                    while (beanIter.hasNext()) 
                        importedBeans.add(beanIter.next());
                    
    
                } catch (JDOMException e) {
                    throw new BeanDocException(
                        "Unable to parse or validate imported resource [" + res + "]", e);
                    
                } catch (FileNotFoundException fnfe) {
                    logger.warn("Unable to load imported resource [" + res + "].  Ignoring it.");
                }
    
            }
            
            // now safe to add beans to root element
            for (Iterator i = importedBeans.iterator(); i.hasNext();)
                rootElement.addContent(((Element) i.next()).detach());     
        }
    } 

    /**
     * User can specify a map of proxy bean RegEx's --> property name mappings.
     * Each mapping denotes a split proxy/target in the context that should be
     * merged in the output. This method takes those <ref>'s and replaces them
     * with an inlined <bean> definition instead for the property named as the
     * Map value.
     * 
     * @param contextDocuments
     */
    private void mergeProxiesInContextDocs(Document[] contextDocuments) {
        logger.debug("Attempting to merge Proxy beans and their targets");
        
        Pattern[] mergedProxyPatterns = PatternMatcher.convertStringsToPatterns(mergeProxies.keySet());
        logger.debug("Generated [" + mergedProxyPatterns.length + "] legal patterns");
            
        final Map proxy2target = new HashMap();
        
        for (int i = 0; i < contextDocuments.length; i++) {
            Element rootElement = contextDocuments[i].getRootElement();
            Iterator allBeans = rootElement.getDescendants(beanFilter);
            while (allBeans.hasNext()) {
                final Element bean = (Element) allBeans.next();
                final String idOrName = getBeanIdentifier(bean);
                String className = bean.getAttributeValue(Tags.ATTRIBUTE_CLASSNAME);
                String[] testForMatches = {idOrName, className};
                logger.debug("Testing bean [" + idOrName + "] against all patterns");
                
                // patterns of beans to be merged
                PatternMatcher.matchPatterns(
                    mergedProxyPatterns, 
                    testForMatches,
                    
                    new MatchedPatternCallback() {
                        public void patternMatched(String pattern, int index) {
                            logger.debug("Got a match against pattern [" + pattern + "]");
                            String targetPropertyName = (String) mergeProxies.get(pattern);
                            Element targetProperty = null;
                            Element targetRef = null;
                            
                            // find target ref
                            Filter propertyFilter = new ElementFilter(Tags.TAGNAME_PROPERTY);
                            Iterator properties = bean.getDescendants(propertyFilter);
                            logger.debug("Checking properties of bean for property named [" + 
                                targetPropertyName + "]");
                            
                            while (properties.hasNext()) {
                                targetProperty = (Element) properties.next();
                                if (targetPropertyName.equals(targetProperty.getAttributeValue(Tags.ATTRIBUTE_NAME))) {
                                    logger.debug("Found matching property");
                                    targetRef = targetProperty.getChild(Tags.TAGNAME_REF);
                                    if (targetRef != null) 
                                        proxy2target.put(bean, targetRef);
                                    else
                                        logger.warn("Found matching target property for outer bean [" + 
                                            idOrName + "] against pattern [" + 
                                            pattern + "] but no <ref/> element was found at the target property [" + 
                                            targetPropertyName + "]");
                                }
                            }
                        }            
                    }
                    
                );
            }
        }
        
        for (Iterator i = proxy2target.keySet().iterator(); i.hasNext();) {
            Element proxy = (Element) i.next();
            Element targetRef = (Element) proxy2target.get(proxy);
            String refId = getRefIdentifier(targetRef);
            proxy.setAttribute(Tags.ATTRIBUTE_PROXY_FOR, refId);
            
            logger.info("Merging proxy bean [" + getBeanIdentifier(proxy) + 
                "] and its target bean [" + refId + "]");
                        
            // and replace it with the first class target - demoting it to an inner bean
            Element targetBean = getBeanElement(contextDocuments, refId);
            
            if (targetBean != null) {
                logger.debug("Target bean [" + refId + "] found in context, detaching from parent");                
                targetBean.detach();
                logger.debug("Converting [" + refId + "] to a named inner bean");
                targetRef.getParentElement().addContent(targetBean);
                // break the REF link
                targetRef.getParentElement().removeChild(Tags.TAGNAME_REF);  
            }
                                               
        }
        
    }

    /**
     * @param contextDocuments 
     * @param refId
     * @return
     */
    private Element getBeanElement(Document[] contextDocuments, String refId) {
        logger.debug("Searching entire context for bean [" + refId + "]");
        for (int i = 0; i < contextDocuments.length; i++) {
            Iterator beans = contextDocuments[i].getRootElement().getDescendants(beanFilter);
            while (beans.hasNext()) {
                Element targetBean = (Element) beans.next();
                logger.debug("Found bean with id [" + 
                    targetBean.getAttributeValue(Tags.ATTRIBUTE_ID) + "]");
                String id;
                if ( (id = getBeanIdentifier(targetBean)) != null && id.equals(refId))
                    return targetBean;
            }
        }
        logger.debug("Unable to find [" + refId + "] in any context file");
        return null;
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
            List beans = doc.getRootElement().getContent(beanFilter);
            String fileName = doc.getRootElement().getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
        
            for (Iterator j = beans.iterator(); j.hasNext();) {
                Element bean = (Element) j.next();
                String idRef = getBeanIdentifier(bean);
                if (idRef != null)  
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
     * return a String representing the <ref> identifier - either its local attribute
     * or its bean attribute.
     * 
     * @param ref
     * @return the value of either the local or bean attribute
     * @throws IllegalArgumentException if the Element is not a <ref> tag
     */
    private String getRefIdentifier(Element ref) throws IllegalArgumentException {
        if (!Tags.TAGNAME_REF.equals(ref.getName()))
            throw new IllegalArgumentException("Not a valid <ref> tag");
        
        String local = ref.getAttributeValue(Tags.ATTRIBUTE_REF_LOCAL);
        if (local == null) 
            return ref.getAttributeValue(Tags.ATTRIBUTE_REF_BEAN);
        return local;
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
            try {
                if (Tags.TAGNAME_REF.equals(tag) || 
                    Tags.TAGNAME_IDREF.equals(tag) || 
                    Tags.TAGNAME_LOOKUP.equals(tag)|| 
                    Tags.TAGNAME_REPLACE.equals(tag)) { 
                    
                    String reference;
                    if ((reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_BEAN)) != null)
                        element.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(reference));
                    else if ((reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_LOCAL)) != null)
                        element.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(reference));
                    else if ((reference = element.getAttributeValue(Tags.ATTRIBUTE_REF_REPLACER)) != null)
                        element.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, (String) beanMap.get(reference));
                    
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
                logger.warn("Failed to decorate element [" + element + 
                "].  Probably a bean was referenced that doesn't exist anywhere in the supplied Context files.");
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
     * @return the List of DocumentCompiler implementations that will plug various pieces of
     * transformed output together
     */
    public List getCompilers() {
        return compilers;
    }

    /**
     * @param list the List of DocumentCompiler implementations that will plug various pieces of
     * transformed output together
     */
    public void setCompilers(List list) {
        compilers = list;
    }

    /**
     * Permits selective merging of ProxyFactory beans and their targets where the targets are defined
     * as top level (referenceable) beans rather than inner beans.  This potentially keeps the 
     * documentation and graphing output cleaner by showing one logical entity instead of two for 
     * the proxy and its target.
     * <p>
     * The keys into the Map specify a RegEx expression denoting either the bean name or the class name
     * of the <b>proxy</b> bean (the wrapper).  The value associated with the key is the property name
     * that the target is referenced under.  Typically this will be 'target'.  Map values must be String
     * objects specifying a bean property and not RegEx expressions or other object.
     *  
     * @param map which cannot be null and will throw IllegalArgumentException if it is.
     */
    public void setMergeProxies(Map map) {
        Assert.notNull(map);
        mergeProxies = map;
    }

    /**
     * @return the Map denoting which Proxy wrappers and their targets to merge
     */
    public Map getMergeProxies() {
        return mergeProxies;
    }

}
