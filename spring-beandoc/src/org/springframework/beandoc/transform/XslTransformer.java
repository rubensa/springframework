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

package org.springframework.beandoc.transform;

import java.io.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ResourceBundle;

import javax.xml.transform.*;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beandoc.BeanDocException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

/**
 * Implementation of the <code>Transformer</code> interface that uses XSLT (via TRaX) in order
 * to generate output files.  If no XSL stylesheet resources are specified when the object is
 * constructed (or via setters before being used) then default XSL stylesheets will be used that
 * are stored in the same package as this class.
 * <p>
 * This class is therefore flexible as a concrete implementation by allowing the user to 
 * specify different XSL stylesheets to have complete control over how the documentation and 
 * .dot files are generated.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class XslTransformer implements Transformer {

    private static final String DEFAULT_DOC_XSL_RESOURCE = 
		"/org/springframework/beandoc/transform/stylesheets/doc.xsl";
    
    private static final String DEFAULT_DOT_XSL_RESOURCE = 
		"/org/springframework/beandoc/transform/stylesheets/dot.xsl";
    
    private String dotTemplateName = DEFAULT_DOT_XSL_RESOURCE;
    
    private String docTemplateName = DEFAULT_DOC_XSL_RESOURCE;
    
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();;

    private Templates dotTemplates;
    
    private Templates docTemplates;
    
    private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
    
    /**
     * loads and compiles the stylesheets using default values.
     */
    public XslTransformer() {
        this(DEFAULT_DOT_XSL_RESOURCE, DEFAULT_DOC_XSL_RESOURCE);
    }
    
    /**
     * loads and compiles the stylesheets using specified values.  Both parameters
     * are interpreted as standard Spring <code>Resource</code>s and can contain any
     * value that the underlying resource loading classes can understand.  See the 
     * Spring JavaDocs for more information on resource handling.
     * 
     * @param dotTemplateName the XSL resource used to generate .dot files
     * @param docTemplateName the XSL resource used to generate HTML output
     */
    public XslTransformer(String dotTemplateName, String docTemplateName) {
        try {
            setDotTemplateName(dotTemplateName);
            setDocTemplateName(docTemplateName);
            
        } catch (IOException e) {
            throw new InvalidTransformerException(e);
        }
    }

    /**
     * Creates documentation in the specified location from the supplied Node.
     * 
     * @see org.springframework.beandoc.transform.Transformer#document
     */
    public void document(Node node, File outputDirectory) throws Exception {
    	// consolidated file
    	File consolidated = new File(outputDirectory, "consolidated.html");
        Result result = new StreamResult(
            new BufferedOutputStream(
                new FileOutputStream(consolidated)
            )
        );
        doTransform(node, docTemplates, result);
    }

    /**
     * Creates a .dot file in the specified location from the supplied Node.
     * 
     * @see org.springframework.beandoc.transform.Transformer#dotFile
     */
    public void dotFile(Node node, File outputFile) throws Exception {
        Result result = new StreamResult(
            new BufferedOutputStream(
                new FileOutputStream(outputFile)
            )
        );
        doTransform(node, dotTemplates, result);
    }
    
    private synchronized void doTransform(Node node, Templates xsl, Result result) throws Exception {
        javax.xml.transform.Transformer trans = xsl.newTransformer();

        // trans.setOutputProperty(OutputKeys.ENCODING, encoding);
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // Xalan-specific, but won't do any harm in other XSLT engines
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        trans.transform(new DOMSource(node), result);
    }

    /**
     * The String value representing the resource pointing to an XSL stylesheet used
     * in generating documentation output.
     * 
     * @return a Spring resource identifier
     */
    public String getDocTemplateName() {
        return docTemplateName;
    }

    /**
     * The String value representing the resource pointing to an XSL stylesheet used
     * in generating .dot file output.
     * 
     * @return a Spring resource identifier
     */
    public String getDotTemplateName() {
        return dotTemplateName;
    }

    /**
     * loads and compile the stylesheet used in generating document output.
     * 
     * @param docTemplateName a standard Spring <code>Resource</code> that can contain any
     *      value that the underlying resource loading classes can understand.  See the 
     *      Spring JavaDocs for more information on resource handling.
     */
    public void setDocTemplateName(String docTemplateName) throws IOException {
        this.docTemplateName = docTemplateName;
        Resource res = resourceLoader.getResource(docTemplateName);
        try {
            this.docTemplates = this.transformerFactory.newTemplates(
                new StreamSource(res.getInputStream())
            );
        }
        catch (TransformerConfigurationException ex) {
            throw new BeanDocException(
                "Can't load stylesheet [" + docTemplateName + "]" + ex.getMessage(), ex);
        }    
    }

    /**
     * loads and compile the stylesheet used in generating .dot file output.
     * 
     * @param dotTemplateName a standard Spring <code>Resource</code> that can contain any
     *      value that the underlying resource loading classes can understand.  See the 
     *      Spring JavaDocs for more information on resource handling.
     */
    public void setDotTemplateName(String dotTemplateName) throws IOException {
        this.dotTemplateName = dotTemplateName;
        Resource res = resourceLoader.getResource(dotTemplateName);
        try {
            this.dotTemplates = this.transformerFactory.newTemplates(
                new StreamSource(res.getInputStream())
            );
        }
        catch (TransformerConfigurationException ex) {
            throw new BeanDocException(
                "Can't load stylesheet [" + dotTemplateName + "]" + ex.getMessage(), ex);
        }    
    }

}
