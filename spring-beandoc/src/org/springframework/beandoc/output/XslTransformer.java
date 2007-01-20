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

package org.springframework.beandoc.output;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.springframework.beandoc.BeanDocException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

/**
 * Base implementation of the <code>Transformer</code> interface that uses XSLT (via TRaX) in order
 * to generate output files.  If no XSL stylesheet resources are specified when the object is
 * constructed (or via setters before being used) then default XSL stylesheets will be used that
 * are stored in the same package as this class.
 * <p>
 * This class is therefore quite flexible as a Transformer implementation in allowing the user to 
 * specify different XSL stylesheets to have complete control over how the output is generated.
 * Subclasses are only responsible for providing a file name for each output file that needs to 
 * be generated by the Transformer.  The only requirement for subclasses is that they provide
 * an output file name for each relevant input file via the abstract {@link #getOutputForDocument}
 * and a stylesheet URI through the {@link #setTemplateName} method.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class XslTransformer implements Transformer {

    private static final String LABELS_BASENAME = 
        "org.springframework.beandoc.output.i18n.labels";

    protected final Log logger = LogFactory.getLog(getClass());

    protected FilenameStrategy filenameStrategy = new FilenameAppenderStrategy(".html");
    
    protected Map staticParameters;
    
    private ResourceBundle labels;
    
    private String templateName;
    
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();;

    private Templates templates;
    
    private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    
    /**
     * default constructor
     */
    public XslTransformer() {
        setLocale(Locale.getDefault());
    }
    
    /**
     * loads and compiles the stylesheets using specified values.  Both parameters
     * are interpreted as standard Spring <code>Resource</code>s and can contain any
     * value that the underlying resource loading classes can understand.  See the 
     * Spring JavaDocs for more information on resource handling.
     * 
     * @param templateName the XSL resource used to generate output
     */
    public XslTransformer(String templateName) {
        setTemplateName(templateName);
    }

    /**
     * Implements the <code>Transformer</code> API and sanity checks some configuration aspects
     * before beginning the transformation workflow.  The <code>initTransform</code> method is first
     * called, returning on any thrown Exception.  After successful initialisation and XSL processing, the
     * <code>postTransform()</code> method is called to perform cleanup or additional
     * tasks.
     * 
     * @see #initTransform
     */
    public final void transform(Document[] contextDocuments, File outputDir) {
        
        if (this.templates == null)
            throw new IllegalStateException("Transformer has not been initialized with a stylesheet");
            
        try {
            initTransform(contextDocuments, outputDir);
        } catch (Exception e) {
            logger.error("Unable to initialize transformer", e);
            return;
        }
        
        // delegate to internal tx method
        handleTransform(contextDocuments, outputDir);
        
        // offer cleanup opportunity
        postTransform();
    }

    /**
     * Perform any initialization or one-off tasks prior to the actual transformation of the
     * context documents with the configured stylesheet.  Default implementation does
     * nothing.
     * 
     * @param contextDocuments the array of DOM trees about to be transformed
     * @param outputDirectory the file handle for the output directory
     */
    protected void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
    }

    /**
     * Creates an output file in the specified location for each supplied document
     * re-using the same compiled stylesheet for each.   Subclasses can override this
     * behaviour if, for example, they don't want to have each document handled in 
     * sequence.  In this case, the subclass can still make use of the <code>doXslTransform</code>
     * method aas a library function.
     * 
     * @param contextDocuments the array of DOM trees about to be transformed
     * @param outputDirectory the file handle for the output directory
     */
    protected void handleTransform(Document[] contextDocuments, File outputDirectory) {
        logger.debug("Generating documentation to [" + outputDirectory.getAbsolutePath() + "]");
        for (int i = 0; i < contextDocuments.length; i++)
            doXslTransform(contextDocuments[i], outputDirectory);
    }

    /**
     * perform the actual tree transformation to the output directory for the given
     * Document
     * 
     * @param doc the document to transform
     * @param outputDir the file handle of the output location
     */
    protected void doXslTransform(Document doc, File outputDir) {        
        String inputFileName = null;
        try {
            inputFileName = doc.getRootElement().getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
            File outputFile = new File(outputDir, getOutputForDocument(inputFileName));
            
            // ensure subdirs exist if needed
            outputFile.getParentFile().mkdirs();
            
            logger.debug("Generating output [" + outputFile.getName() + "]");
            
            Result result = new StreamResult(
                new BufferedOutputStream(
                    new FileOutputStream(outputFile)
                )
            );
            Node node = convertJdomToW3C(doc);

            javax.xml.transform.Transformer trans = templates.newTransformer();
            
            // apply any subclass supplied parameters to the transformer
            Map parameters = getParameters(doc);
            if (parameters == null) parameters = new HashMap();
            
            // add in i18n text labels
            localizeLabels(parameters);
            
            for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                trans.setParameter(entry.getKey().toString(), entry.getValue());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Added parameters [" + parameters + "] to transformer object");
            }
            

            // trans.setOutputProperty(OutputKeys.ENCODING, encoding);
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // Xalan-specific, but won't do any harm in other XSLT engines
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            trans.transform(new DOMSource(node), result);
                
        } catch (Exception e) {
            logger.error("Unable to transform contextDocument with input file name [" + inputFileName + "]", e);
        }        
    }

    /**
     * Subclasses may optionally generate and return a Map of stylesheet
     * parameters that will be included in the transform.  The default 
     * implementation returns the Map of staticParameters (if any).
     * 
     * @param doc the Document about to be transformed
     * @return a Map of stylesheet parameter names and their corresponding values.  The 
     *      parameter names must be defined in the stylesheet itself.
     */
    protected Map getParameters(Document doc) {
        return staticParameters;
    }
    
    /**
     * Return the name of the output file (relative to the configured output directory) that
     * this transformer will use to generate output to.  Default impl. uses the filenameStrategy.
     * 
     * @param inputFileName the original file name (not including path) of the context file
     * @return the output file name to use
     */
    protected String getOutputForDocument(String inputFileName) {
        String output = filenameStrategy.getFileName(inputFileName);
        logger.debug("Returning output file name [" + output + 
            "] for input file name [" + inputFileName + "]");
        return output;
    }

    /**
     * Perform any finalization or one-off tasks after the actual transformation of the
     * context documents with the configured stylesheet.  Default implementation does
     * nothing.
     */
    protected void postTransform() {
    }

    /**
     * @param consolidatedDoc
     * @return
     */
    private Node convertJdomToW3C(Document jdomDoc) {
        // converter to W3C API
        DOMOutputter out = new DOMOutputter();
        try {
            return out.output(jdomDoc);
        } catch (JDOMException e) {
            throw new BeanDocException(
                "Unable to convert internal tree to W3C Node type", e
            );
        }
    }

    /**
     * add i18n labels from the supplied ResourceBundles
     * 
     * @param parameters the Map to add label params to
     */
    private void localizeLabels(Map parameters) {
        if (labels != null && parameters != null)
            for (Enumeration e = labels.getKeys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                parameters.put(key, labels.getString(key));
            }       
    }
    
    
    // ---------------------------------------------------------------------
    // bean props
    // ---------------------------------------------------------------------

    /**
     * The String value representing the resource pointing to an XSL stylesheet used
     * in generating documentation output.
     * 
     * @return a Spring resource identifier
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * loads and compile the stylesheet used in generating document output.  If the supplied
     * template name cannot be compiled to a binary template representation, a BeanDocException
     * will be thrown here.
     * 
     * @param templateName a standard Spring <code>Resource</code> that can contain any
     *      value that the underlying resource loading classes can understand.  See the 
     *      Spring JavaDocs for more information on resource handling.
     */
    public void setTemplateName(String templateName) throws InvalidTransformerException {
        this.templateName = templateName;
        Resource res = resourceLoader.getResource(templateName);
        try {
            URL url = res.getURL(); 
            String urlPath = url.toString(); 
            String systemId = urlPath.substring(0, urlPath.lastIndexOf('/') + 1); 
            this.templates = this.transformerFactory.newTemplates(
                new StreamSource(res.getInputStream(), systemId)
            );
        }
        catch (Exception ex) {
            throw new InvalidTransformerException (
                "Can't load stylesheet [" + templateName + "]" + ex.getMessage(), ex);                
        }   
    }

    /**
     * @return the Map of static XSL parameters that may or may not be returned as part of the
     *      getParameters(Document) method
     */
    public Map getStaticParameters() {
        return staticParameters;
    }

    /**
     * Provide a Map of XSL parameter values for this class that can be used to make up all or 
     * part of the Map of actual parameters used in the stylesheet.
     * 
     * @param staticParameters a Map of parameters that will probably be used as part of a
     *      Map returned from the getParameters(Document) method
     */
    public void setStaticParameters(Map staticParameters) {
        this.staticParameters = staticParameters;
    }

    /**
     * set the output filename strategy to use.
     * 
     * @param filenameStrategy
     */
    public void setFilenameStrategy(FilenameStrategy filenameStrategy) {
        this.filenameStrategy = filenameStrategy;
    }

    /**
     * Override the system default locale and specify which locale output should
     * be generated for.
     * 
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.labels = ResourceBundle.getBundle(LABELS_BASENAME, locale);
    }

}