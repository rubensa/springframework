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

import java.io.*;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.*;
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
 * be generated by the Transformer.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public abstract class BaseXslTransformer implements Transformer {

    protected final Log logger = LogFactory.getLog(getClass());
    
    private String templateName;
    
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();;

    private Templates templates;
    
    private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    
    /**
     * default constructor
     */
    public BaseXslTransformer() {
        
    }
    
    /**
     * loads and compiles the stylesheets using specified values.  Both parameters
     * are interpreted as standard Spring <code>Resource</code>s and can contain any
     * value that the underlying resource loading classes can understand.  See the 
     * Spring JavaDocs for more information on resource handling.
     * 
     * @param templateName the XSL resource used to generate output
     */
    public BaseXslTransformer(String templateName) {
        try {
            setTemplateName(templateName);
            
        } catch (IOException e) {
            throw new InvalidTransformerException(e);
        }
    }

    /**
     * Implements the <code>Transformer</code> API and sanity checks some configuration aspects
     * before beginning the transformation workflow.  The <code>initTransform</code> method is first
     * called, returning on any thrown Exception.  After successful initialisation and XSL processing, the
     * <code>postTransform()</code> method is called to perform cleanup or additional
     * tasks.
     * 
     * @see org.springframework.beandoc.output.Transformer#dotFile
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
     * @param File the file handle for the output directory
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
     * @param File the file handle for the output directory
     */
    protected void handleTransform(Document[] contextDocuments, File outputDir) {
        for (int i = 0; i < contextDocuments.length; i++)
            doXslTransform(contextDocuments[i], outputDir);
    }

    /**
     * perform the actual tree transformation to the output directory for the given
     * Document
     * 
     * @param doc the document to transform
     * @param outputDir the file handle of the output location
     */
    protected final void doXslTransform(Document doc, File outputDir) {
        String inputFileName = null;
        try {
            inputFileName = doc.getRootElement().getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
            File outputFile = new File(outputDir, getOutputForDocument(inputFileName));
            logger.info("Generating output [" + outputFile.getName() + "]");
            
            Result result = new StreamResult(
                new BufferedOutputStream(
                    new FileOutputStream(outputFile)
                )
            );
            Node node = convertJdomToW3C(doc);

            javax.xml.transform.Transformer trans = templates.newTransformer();
            
            // apply any subclass supplied parameters to the transformer
            Map parameters = getParameters(doc);
            if (parameters != null) {
                for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    trans.setParameter(entry.getKey().toString(), entry.getValue());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Added parameters [" + parameters + "] to transformer object");
                }
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
     * implementation returns null.
     * 
     * @param doc the Document about to be transformed
     * @return a Map of stylesheet parameter names and their corresponding values.  The 
     *      parameter names must be defined in the stylesheet itself.
     */
    protected Map getParameters(Document doc) {
        return null;
    }
    
    /**
     * Return the name of the output file (relative to the configured output directory) that
     * this transformer will use to generate output to.  Subclasses should implement this
     * method according to their output needs
     * 
     * @param inputFileName the original file name (not including path) of the context file
     * @return the output file name to use
     */
    protected abstract String getOutputForDocument(String inputFileName);

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
    
    // ---------------------------------------------------------------------
    // bean props
    // ---------------------------------------------------------------------

    /**
     * The String value representing the resource pointing to an XSL stylesheet used
     * in generating documentation output.
     * 
     * @return a Spring resource identifier
     */
    public String gettemplateName() {
        return templateName;
    }

    /**
     * loads and compile the stylesheet used in generating document output.
     * 
     * @param templateName a standard Spring <code>Resource</code> that can contain any
     *      value that the underlying resource loading classes can understand.  See the 
     *      Spring JavaDocs for more information on resource handling.
     */
    public void setTemplateName(String templateName) throws IOException {
        this.templateName = templateName;
        Resource res = resourceLoader.getResource(templateName);
        try {
            this.templates = this.transformerFactory.newTemplates(
                new StreamSource(res.getInputStream())
            );
        }
        catch (TransformerConfigurationException ex) {
            throw new BeanDocException(
                "Can't load stylesheet [" + templateName + "]" + ex.getMessage(), ex);
        }    
    }

}
