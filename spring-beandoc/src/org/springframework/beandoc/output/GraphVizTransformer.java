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
import java.io.IOException;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.util.StringUtils;

/**
 * Generates graphs from the context files, firstly transforming the XML to an 
 * intermediate .dot file that GraphViz uses to generate the actual images.  
 * In addition, it will generate a consolidated image from all supplied
 * context documents.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class GraphVizTransformer extends BaseXslTransformer {

    private static final String XSLPARAM_GRAPHTYPE = "beandocXslGraphType";
    
    private static final String DEFAULT_XSL_RESOURCE = 
        "/org/springframework/beandoc/output/stylesheets/dot.xsl";    
    
    private static final String CONSOLIDATED_XML_FILENAME = "consolidated.xml"; 

    private String graphOutputType = "png";
    
    private String dotExe;
    
    private boolean removeDotFiles = true;

    private Document[] contextDocuments;

    private File outputDirectory;
    
    private List dotFileList = new ArrayList();
    
    public GraphVizTransformer() {
        this(DEFAULT_XSL_RESOURCE);
    }
    
    /**
     * @param templateName
     */
    public GraphVizTransformer(String templateName) {
        super(templateName);
    }

    /**
     * @see org.springframework.beandoc.output.BaseXslTransformer#initTransform
     */
    protected void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
        // store values - we need them to post process the context
        this.outputDirectory = outputDirectory;
        this.contextDocuments = contextDocuments;
        String consolidatedImage = contextDocuments[0].getRootElement().getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_CONSOLIDATED);
        this.graphOutputType = StringUtils.unqualify(consolidatedImage);
    }

    /**
     * @see org.springframework.beandoc.output.BaseXslTransformer#getOutputForDocument
     */
    protected String getOutputForDocument(String inputFileName) {
        String dotFile = StringUtils.replace(inputFileName, ".xml", ".dot");
        dotFileList.add(dotFile);
        return dotFile;
    }

    /**
     * Generate a consolidated graph of the entire context
     * 
     * @see org.springframework.beandoc.output.BaseXslTransformer#postTransform
     */
    protected void postTransform() {
        // consolidate documents
        generateConsolidatedGraph();
        
        // generate graphs from all .dot files in output location
        for (Iterator i = dotFileList.iterator(); i.hasNext();) {
            File dotFile = new File(outputDirectory, (String) i.next());
            String dotArgs = 
                " -T" + graphOutputType +
                " -o" + StringUtils.replace(dotFile.getAbsolutePath(), ".dot", "." + graphOutputType) +
                " " + dotFile.getAbsolutePath();
       
            try {
                Process dot = Runtime.getRuntime().exec(dotExe + dotArgs);
                dot.waitFor();
                if (removeDotFiles) dotFile.delete();
                
            } catch (IOException ioe) {
                logger.warn("Problem attempting to draw graph from dot file [" + dotFile.getAbsolutePath() + "]", ioe);
            } catch (InterruptedException e) {
                // ok
            }
        }
    }
    
    /**
     * complete context image from all input files
     */
    private void generateConsolidatedGraph() {
        Document context = new Document();
        // use root element from first input
        Element beans = (Element) contextDocuments[0].getRootElement().clone();
        beans.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, CONSOLIDATED_XML_FILENAME);
        context.setRootElement(beans);
        for (int i = 0; i < contextDocuments.length; i++) 
            beans.addContent(contextDocuments[i].getRootElement().cloneContent());        
        
        // transform it
        doXslTransform(context, outputDirectory);
    }
    
    /**
     * @see org.springframework.beandoc.output.BaseXslTransformer#getParameters
     */
    protected Map getParameters(Document doc) {
        Map params = new HashMap();
        params.put(XSLPARAM_GRAPHTYPE, graphOutputType);
        return params;
    }
    
    // ---------------------------------------------------------------------
    // bean props
    // ---------------------------------------------------------------------

    /**
     * Set the location of the 'dot' executable file from the Graphviz installation.  This file
     * will be called with appropriate parameters if graphing output is required using a 
     * <code>Runtime.getRuntime().exec(...)</code> call.  If this value is not set, graphing
     * output will be disabled.
     * 
     * @param dotExe the platform dependent location of the binary, ie "/usr/local/bin/dot" or
     * "C:/graphviz/dot.exe"
     */
    public void setDotExe(String dotExe) {
        this.dotExe = dotExe;
    }

    /**
     * A series of intermediate files (.dot files) are
     * created which is what GraphViz uses to actually generate the graphs.  Usually
     * these wil not be needed after the graphs are generated and so by default are
     * discarded.  If you need to keep them for any reason, set this value to <code>false</code>
     * 
     * @param removeDotFiles set to false to prevent intermediate .dot files being discarded.  True
     *      by default.
     */
    public void setRemoveDotFiles(boolean removeDotFiles) {
        this.removeDotFiles = removeDotFiles;
    }

    /**
     * Location of the GraphViz 'dot' executable program on the local machine
     * 
     * @return the platform-dependent location of the GraphViz 'dot' executable file
     */
    public String getDotExe() {
        return dotExe;
    }

    /**
     * Should intermediate .dot files be removed?
     * 
     * @return true if intermediate .dot files will be removed after graphing output has
     *      completed, or false if they will be kept in the output directory.  True by default.
     */
    public boolean isRemoveDotFiles() {
        return removeDotFiles;
    }

}
