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

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.beandoc.transform.Transformer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * BeanDocEngine builds DOM trees from the input files specified in the Configuration object
 * (supplied to the constructor).  Each tree is 'decorated' with additional attributes that 
 * make it simpler for a {@link Transformer} to generate the desired output.  Details of how the
 * tree is decorated are shown below.  With the exception of these changes, the DOM tree
 * is a faithful representation of the context file as specified by Spring's DTD which means
 * that any existing investment in transforming Spring context files can be leveraged in
 * the bean doc tool.
 * <p>
 * Each decorated DOM tree is passed to a configurable {@link Transformer} implementation whose
 * responsibility it is to actually output the required documents.  Initially, individual
 * DOM trees are used to generate .dot files (if required by the {@link Configuration}) and these
 * in turn are used by the engine to later generate graphs in the desired ouput format.
 * <p>
 * Once any intermediate .dot files have been built, each individual tree is consolidated 
 * into a single DOM representing the entire application context.  This tree is used
 * to generate the actual beanDoc by calling the {@link Transformer}'s document() method.  The
 * consolidated tree is also passed back into the dotFile() method of the Transformer in
 * order to generate a consolidated .dot (where suitably configured in the {@link Configuration}
 * object).
 * <p>
 * Finally, the engine generates the actual graphs if applicable from the .dot files and
 * builds the index and CSS links for the beanDoc itself.
 * <p>
 * <h3>Node decoration added by the engine to standard DOM elements</h3>
 * The <code>&lt;beans&gt;</code> (root) tag is given additional attributes as follows:
 * <p>
 * <table border="1" cellpadding="3">
 * <tr><th>attribute name</th><th>description</th></tr>
 * <tr><td>beandocFileName</td><td>The name of the input file used to generate this
 * tree.</td></tr>
 * <tr><td>beandocGraphFontSize</td><td>The default font size for nodes on the 
 * graph.</td></tr>
 * <tr><td>beandocGraphRatio</td><td>Ratio used which has an effect on overall graph
 * size and node compressions.</td></tr>
 * </table>
 * <p>
 * The <code>[beans/description]</code> tag is also given a single attribute named 
 * <b>beandocFileName</b> for when the individual context files are consolidated.  Each
 * context's file <code>&lt;description&gt;</code> is therefore differentiated by this 
 * attribute.
 * <p>
 * Each <code>&lt;bean&gt;</code> tag is given additional attributes as follows:
 * <p>
 * <table border="1" cellpadding="3">
 * <tr><th>attribute name</th><th>description</th></tr>
 * <tr><td>beandocFileName</td><td>As above, the original file name that the bean was
 * defined in.  Only really useful when managing the combined application context as the
 * information will exist nowhere else.</td></tr>
 * <tr><td>beandocGraphName</td><td>The file name of the individual graph that the bean 
 * can be seen in.</td></tr>
 * <tr><td>beandocFillColour</td><td>The colour used to show this bean on graphs (where 
 * graphs are used).  Colours are determined from the {@link Configuration} object based
 * on either the bean name or class name.</td></tr>
 * <tr><td>beandocJavaDoc</td><td>A URL to the JavaDoc for the bean's 
 * class.  JavaDoc locations are specified by {@link Configuration}</td></tr>
 * </table>
 * 
 * @author Darren Davison
 * @since 1.0
 */
public final class BeanDocEngine {
    
    private static final String MEDIA_RESOURCES = "classpath:/org/springframework/beandoc/transform/media/*";

    private static final String NAME_ATTRIBUTE = "name";
	
	private static final String ID_ATTRIBUTE = "id";
	
	private static final String CLASSNAME_ATTRIBUTE = "class";
    
    private static final String TITLE_ATTRIBUTE = "beandocTitle";
	
	private static final String JAVADOC_ATTRIBUTE = "beandocJavaDoc";
	
	private static final String COLOUR_ATTRIBUTE = "beandocFillColour";
	
	private static final String FILENAME_ATTRIBUTE = "beandocFileName";
    
    private static final String CSS_ELEMENT_NAME = "beandocCssLocation";
	
    private static final String GRAPHNAME_ATTRIBUTE = "beandocGraphName";
    
    private static final String GRAPH_FONTNAME_ATTRIBUTE = "beandocGraphFontName";
	
	private static final String GRAPH_FONTSIZE_ATTRIBUTE = "beandocGraphFontSize";
	
    private static final String GRAPH_SIZE_ATTRIBUTE = "beandocGraphSize";
    
    private static final String GRAPH_RATIO_ATTRIBUTE = "beandocGraphRatio";
	
	private static final String GRAPH_BEANSHAPE_ATTRIBUTE = "beandocGraphBeanShape";
	
	private static final String GRAPH_LABELLOCATION_ATTRIBUTE = "beandocGraphLabelLocation";
    
	private static final String CONSOLIDATED_IMG_ATTRIBUTE = "beandocConsolidatedImage";    
    
    private static final String CONSOLIDATED_XML_FILENAME = "consolidated.xml"; 
    
    private static Log logger = LogFactory.getLog(BeanDocEngine.class);
    
    private Configuration cfg;
    
    private boolean isDoGraphs;
    
    
    /**
     * default ctor for the BeanDocEngine.  A valid {@link Configuration} must
     * be supplied
     * 
     * @param cfg the <code>Configuration</code> object used to parameterize this engine
     */
    public BeanDocEngine(Configuration cfg) {
        this.cfg = cfg;
        this.isDoGraphs = (cfg.getDotExe() != null);
        
        if (logger.isDebugEnabled()) 
            logger.debug("Constructed with configuration: [" + cfg + "]");
    }
    
    /**
     * Handles each input file in turn, parsing the XML (which must validate against
     * the DTD unless validation has been turned off in the <code>Configuration</code> -
     * see {@link Configuration#setValidateFiles}), decorating the beans and creating 
     * a consolidated in-memory DOM document of all beans from all input files.
     * <p>
     * The process method is threadsafe, synchronizing on the <code>Configuration</code>
     * instance during the execution of documentation output.  A configured BeanDocEngine
     * is therefore re-usable from client code, and the configuration properties can be 
     * modified between calls to the process method.
     * 
	 * @throws IOException if the input files cannot be read, if the output 
	 * 		directory does not exist or is not writable, or if media files cannot
     *      be copied from the classpath to the output directory.
	 * @throws BeanDocException of the input files do not validate against the
	 * 		DTD, if a problem occurs attempting to create graphs from the .dot
	 * 		files (such as the GraphViz program being unavailable) or other
	 * 		unknown problem occurs.
	 */
	public void process() throws IOException, BeanDocException {
        
        // debug logging will cause all of the config to be output upon construction,
        // there's no value in repeating any of it in here.
        
        logger.info("Processing input files..");

		// create shell for consolidated context DOM
		Document consolidatedDoc = new Document();
		Element consolidatedBeans = new Element("beans");
		consolidatedDoc.setRootElement(consolidatedBeans);
	
        synchronized (cfg) {
			consolidatedBeans.setAttribute(TITLE_ATTRIBUTE, cfg.getTitle());
			
            // add CSS file locations
            List cssElements = new LinkedList();
            String[] cssUrls = cfg.getContextCssUrls();
            for (int i = 0; i < cssUrls.length; i++) {
                Element css = new Element(CSS_ELEMENT_NAME);
                css.setText(cssUrls[i]);
                cssElements.add(css);
            }
            consolidatedBeans.addContent(cssElements);
            
    		if (isDoGraphs) {
                consolidatedBeans.setAttribute(FILENAME_ATTRIBUTE, CONSOLIDATED_XML_FILENAME);
				consolidatedBeans.setAttribute(
					CONSOLIDATED_IMG_ATTRIBUTE, 
					StringUtils.replace(CONSOLIDATED_XML_FILENAME, "xml", cfg.getGraphOutputType()) 
				);
                
    			consolidatedBeans.setAttribute(GRAPH_FONTNAME_ATTRIBUTE, cfg.getGraphFontName());
    			consolidatedBeans.setAttribute(GRAPH_FONTSIZE_ATTRIBUTE, String.valueOf(cfg.getGraphFontSize()));
    			if (cfg.getGraphXSize() != -1 && cfg.getGraphYSize() != -1)
                    consolidatedBeans.setAttribute(
                        GRAPH_SIZE_ATTRIBUTE, 
                        String.valueOf(cfg.getGraphXSize() + ", " + cfg.getGraphYSize())
                    );                     
                consolidatedBeans.setAttribute(GRAPH_RATIO_ATTRIBUTE, String.valueOf(cfg.getGraphRatio()));		
    			consolidatedBeans.setAttribute(GRAPH_BEANSHAPE_ATTRIBUTE, cfg.getGraphBeanShape());
    			consolidatedBeans.setAttribute(GRAPH_LABELLOCATION_ATTRIBUTE, String.valueOf(cfg.getGraphLabelLocation()));
    		}
        
            // build jdom docs from input files
            Document[] contextDocuments = buildDomsFromInputFiles();
            
            // consolidate bean definitions
            for (int i = 0; i < contextDocuments.length; i++)
                consolidatedBeans.addContent(
                    contextDocuments[i].getRootElement().cloneContent()
                ); 
            
			// optional graphing output, keep a list of .dot files
			List dotFiles = new LinkedList();
			if (cfg.getDotExe() != null) {
				for (int i = 0; i < contextDocuments.length; i++) 
					dotFiles.add(doGraphOutput(contextDocuments[i]));
                    
				doGraphOutput(consolidatedDoc);
			} 
			
			// write beandoc
            Node w3cNode = convertJdomToW3C(consolidatedDoc);
            
            try {
                cfg.getTransformer().document(w3cNode, cfg.getOutputDir());
            } catch (Exception e) {
                throw new BeanDocException("Problem attempting to write beandoc document using transformer[" + 
                    cfg.getTransformer().getClass().getName() + "]", e);
            }
            
            writeMedia(); 
            
            // we delay removing .dot files until here because of timing issues -
            // sometimes they get deleted before dot has a chance to use them, 
            // sometimes they don't get deleted because they were in use while
            // attempting to delete.
			if (cfg.isRemoveDotFiles() && dotFiles.size() > 0) 
				for (Iterator i = dotFiles.iterator(); i.hasNext();) {
					File dotFile = (File) i.next();
					dotFile.deleteOnExit();
				} 
        }
	}

    private Document[] buildDomsFromInputFiles() throws IOException {
        Document[] contextDocuments = new Document[cfg.getInputFiles().length];
        Resource[] inputFiles = cfg.getInputFiles();
        
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(cfg.isValidateFiles());
        
        // process each context file, decorating and consolidating.  
        for (int i = 0; i < inputFiles.length; i++) {
            logger.info("  starting [" + inputFiles[i].getFilename() + "]");
            
            try {
        		contextDocuments[i] = builder.build(inputFiles[i].getInputStream());
        	} catch (JDOMException e) {
        		throw new BeanDocException(
        			"Unable to parse or validate input resource [" + 
        			inputFiles[i].getFilename() + "]", e);
            }
            
            decorateBeans(contextDocuments[i], inputFiles[i].getFilename());
            
            // remove the DTD as our output no longer subscribes to it.
        	contextDocuments[i].removeContent(contextDocuments[i].getDocType());
        	
        	// kill comments for efficiency
        	Filter filter = new ContentFilter(ContentFilter.COMMENT);
        	contextDocuments[i].getRootElement().removeContent(filter);
        	contextDocuments[i].removeContent(filter);
        }
        return contextDocuments;
    }

    /**
     * copy media files from resource to output location
     * 
     * @throws IOException
     */
    private void writeMedia() throws IOException {
        ResourcePatternResolver resolver = 
            new PathMatchingResourcePatternResolver(new DefaultResourceLoader());
        Resource[] media = resolver.getResources(MEDIA_RESOURCES);
        
        for (int i = 0; i < media.length; i++) {
            File target = new File(cfg.getOutputDir(), media[i].getFilename());
            FileOutputStream fos = new FileOutputStream(target);
            
            InputStream is = media[i].getInputStream();
            byte[] buff = new byte[256];
            while (is.read(buff) != -1) fos.write(buff);        
        }
    }

    /**
     * @param consolidatedDoc
     * @return
     */
    private Node convertJdomToW3C(Document consolidatedDoc) {
        // converter to W3C API
        DOMOutputter out = new DOMOutputter();
        try {
            return out.output(consolidatedDoc);
        } catch (JDOMException e) {
            throw new BeanDocException(
                "Unable to convert internal tree to W3C Node type", e
            );
        }
    }

    /**
	 * @param document
	 * @param string
	 */
	private void decorateBeans(Document document, String fileName) {
		
		// add context file name to beans and description elements
		Element beans = document.getRootElement();
		
		beans.setAttribute(FILENAME_ATTRIBUTE, fileName);		
		if (isDoGraphs) {
            beans.setAttribute(GRAPH_FONTNAME_ATTRIBUTE, cfg.getGraphFontName());
			beans.setAttribute(GRAPH_FONTSIZE_ATTRIBUTE, String.valueOf(cfg.getGraphFontSize()));
			beans.setAttribute(GRAPH_RATIO_ATTRIBUTE, String.valueOf(cfg.getGraphRatio()));		
			beans.setAttribute(GRAPH_BEANSHAPE_ATTRIBUTE, cfg.getGraphBeanShape());
			beans.setAttribute(GRAPH_LABELLOCATION_ATTRIBUTE, String.valueOf(cfg.getGraphLabelLocation()));
		}
				
		// ensure description exists - add empty one if not
		if (beans.getChild("description") == null) {
			Element desc = new Element("description");
			desc.setText("[empty description]");
			beans.addContent(desc);
		}
			
		List beanList = beans.getChildren();
			
		String graphName = StringUtils.replace(fileName, ".xml", "." + cfg.getGraphOutputType());
		for (Iterator iter = beanList.iterator(); iter.hasNext();) {
			Element el = (Element) iter.next();
			el.setAttribute(FILENAME_ATTRIBUTE, fileName);			
            if (isDoGraphs) el.setAttribute(GRAPHNAME_ATTRIBUTE, graphName);       
            
			// add style attributes to bean elements
			if (el.getName().equals("bean")) {
				String idOrName = el.getAttributeValue(ID_ATTRIBUTE);
				if (idOrName == null) idOrName = el.getAttributeValue(NAME_ATTRIBUTE);
				String className = el.getAttributeValue(CLASSNAME_ATTRIBUTE);
				
				String colour = cfg.getColourForBean(idOrName, className);
				String javaDoc = null;
				if (className != null) 
					javaDoc = cfg.getJavaDocForClassName(className);
				
				el.setAttribute(COLOUR_ATTRIBUTE, colour);
				if (javaDoc != null) el.setAttribute(JAVADOC_ATTRIBUTE, javaDoc);
			}
			
			if (el.getName().equals("description")) {
				el.setAttribute(FILENAME_ATTRIBUTE, fileName);			
				if (isDoGraphs) el.setAttribute(GRAPHNAME_ATTRIBUTE, graphName);  
			}
				
		}		
	} 
    
    /**
     * 
     * @param contextDocument
     * @return a File handle of the intermediate .dot file which may later require deletion.
     * 		we don't delete it in here because the process executes asynchronously with 
     * 		dot and timing issues can prevent deletion, or cause deletion before the 
     * 		graph has been created
     */
    private File doGraphOutput(Document contextDocument) {
        
        String fileName = contextDocument.getRootElement().getAttributeValue("beandocFileName");
        
        // remove beans that are not required on the graphs (they will still be documented in beandoc)
        for (Iterator i = contextDocument.getRootElement().getChildren("bean").iterator(); i.hasNext();) {
            Element bean = (Element) i.next();
            String idOrName = 
                (bean.getAttributeValue(ID_ATTRIBUTE) != null) ? 
                bean.getAttributeValue(ID_ATTRIBUTE) : 
                bean.getAttributeValue(NAME_ATTRIBUTE);
            String className = bean.getAttributeValue(CLASSNAME_ATTRIBUTE);
            
            if (cfg.isBeanIgnored(idOrName, className)) 
                i.remove();            
            
        }
        
        // write dot file
        Node w3cNode = convertJdomToW3C(contextDocument);
        File outputFile = new File(
            cfg.getOutputDir(), 
            StringUtils.replace(
                fileName, ".xml", ".dot"
            )
        );
        
        try {
            cfg.getTransformer().dotFile(w3cNode, outputFile);        
        } catch (Exception e) {
            throw new BeanDocException("Problem attempting to write beandoc document using transformer[" + 
                cfg.getTransformer().getClass().getName() + "]", e);
        }
                
        // do graph
        String dotArgs = 
            " -T" + cfg.getGraphOutputType() +
            " -o" + StringUtils.replace(outputFile.getAbsolutePath(), ".dot", "." + cfg.getGraphOutputType()) +
            " " + outputFile.getAbsolutePath();
               
        try {
            Process p = Runtime.getRuntime().exec(cfg.getDotExe() + dotArgs);
        } catch (IOException ioe) {
            logger.warn("Problem attempting to draw graph from dot file [" + fileName + "]", ioe);
        }
        
        return outputFile;
    }
	
}
