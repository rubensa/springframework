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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beandoc.Tags;

/**
 * Generates graphs from the context files, firstly transforming the XML to an 
 * intermediate .dot file that GraphViz uses to generate the actual images.  
 * In addition, it will generate a consolidated image from all supplied
 * context documents.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class DotFileTransformer extends XslTransformer {
    
    private static final String DEFAULT_XSL_RESOURCE = 
        "/org/springframework/beandoc/output/stylesheets/dot.xsl";    
    
    private static final String CONSOLIDATED_XML_FILENAME = "consolidated.xml";

    private Document[] contextDocuments;

    private File outputDirectory;
    
    private List dotFileList = new ArrayList();
    
    /**
     * constructs the Transformer with a default stylesheet included in the 
     * beandoc tool.
     */
    public DotFileTransformer() {
        this(DEFAULT_XSL_RESOURCE);
    }
    
    /**
     * constructs the Transformer with the stylesheet provided.  The templateName
     * can be any resolvable Spring Resource.
     * 
     * @param templateName
     */
    public DotFileTransformer(String templateName) {
        super(templateName);
    }

    /**
     * Stores references to the context documents and output directory which it
     * later uses to build a consolidated graph.
     * 
     * @see org.springframework.beandoc.output.XslTransformer#initTransform
     */
    protected void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
        // store values - we need them to post process the context
        this.outputDirectory = outputDirectory;
        this.contextDocuments = contextDocuments;
    }

    /**
     * Adds input files to a list for post-processing prior to returning the
     * name of the output file.
     * 
     * @see org.springframework.beandoc.output.XslTransformer#getOutputForDocument
     */
    protected String getOutputForDocument(String inputFileName) {
        String dotFile = filenameStrategy.getFileName(inputFileName);
        dotFileList.add(dotFile);
        return dotFile;
    }

    /**
     * Generate a consolidated graph of the entire context using the same 
     * stylesheet reference provided on construction.
     * 
     * @see org.springframework.beandoc.output.XslTransformer#postTransform
     */
    protected void postTransform() {
        Document context = new Document();
        // copy attribs from root element of first input
        Element beans = new Element(Tags.TAGNAME_BEANS);
        //Element beans = (Element) contextDocuments[0].getRootElement().clone();
        for (Iterator iter = contextDocuments[0].getRootElement().getAttributes().iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            beans.setAttribute((Attribute) attr.clone());
        }
        beans.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, CONSOLIDATED_XML_FILENAME);        
        context.setRootElement(beans);
        
        for (int i = 0; i < contextDocuments.length; i++) 
            beans.addContent(contextDocuments[i].getRootElement().cloneContent());        
        
        // transform it
        doXslTransform(context, outputDirectory);
    }

}
