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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class DefaultDocumentCompiler implements DocumentCompiler { 
    
    private static final String MEDIA_RESOURCES = 
        "classpath:/org/springframework/beandoc/output/media/*";
    
    private static final String DOT_FILE_EXT = ".dot";
    
    private String dotFileMapFormat = "cmapx";

    protected final Log logger = LogFactory.getLog(getClass());

    private String graphOutputType = "png";
    
    private String dotExe;
    
    private boolean removeDotFiles = true;

    /**
     * Generates actual images and HTML image maps (as required) from the dot files created 
     * by DotFileTransformer.  Subsequently plugs the image maps into placeholders in the
     * graph html files.
     * 
     * @see org.springframework.beandoc.output.DocumentCompiler#compile()
     */
    public void compile(File outputDir) {
        /*
        String consolidatedImage = 
            contextDocuments[0].getRootElement().getAttributeValue(
                GraphVizDecorator.ATTRIBUTE_GRAPH_CONSOLIDATED);
        this.graphOutputType = StringUtils.unqualify(consolidatedImage);
        */
        
        File[] dotFileList = outputDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(DOT_FILE_EXT);
            }
        });
        
        // generate graphs and image maps from all .dot files in output location
        for (int i = 0; i < dotFileList.length; i++) {
            runDot(dotFileList[i], graphOutputType, graphOutputType);
            File mapFile = runDot(dotFileList[i], dotFileMapFormat, "map");
            
            // insert map into -graph.html file
            plugMap(mapFile);
            
            if (removeDotFiles) dotFileList[i].delete();
        }
        
        copyMediaResources(outputDir);

    }

    /**
     * @param dotFile the .dot format file to compile
     * @param outputType the output parameter for the Dot binary to determine
     *      output format
     * @param fileExt the filename extension of the output file that Dot should generate
     * @return the output file created by Dot
     */
    private File runDot(File dotFile, String outputType, String fileExt) {
        String dotFileName = dotFile.getAbsolutePath();
        File outputFile = new File(StringUtils.replace(dotFileName, DOT_FILE_EXT, "." + fileExt));
        String dotArgs = 
            " -T" + outputType +
            " -o" + outputFile.getAbsolutePath() +
            " " + dotFile.getAbsolutePath();
       
        try {
            logger.info("Generating graph from file [" + dotFileName + "]");
            Process dot = Runtime.getRuntime().exec(dotExe + dotArgs);
            dot.waitFor();
            logger.debug("Process exited with value [" + dot.exitValue() + "]");
                
        } catch (IOException ioe) {
            logger.warn(
                "Problem attempting to create [" + outputFile + "] from dot file [" + 
                dotFileName + "]; " + ioe.getMessage()
            );
        } catch (InterruptedException e) {
            // ok
        }
        
        return outputFile;
    }

    /**
     * @param mapFile
     */
    private void plugMap(File mapFile) {
        File graphFile = new File(StringUtils.replace(mapFile.getAbsolutePath(), ".map", "-graph.html"));
        
        StringBuffer map = new StringBuffer(256);
        StringBuffer doc = new StringBuffer(512);
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(mapFile));
            while ((line = reader.readLine()) != null)
                map.append(line).append("\n");
                
            reader = new BufferedReader(new FileReader(graphFile));
            while ((line = reader.readLine()) != null)
                if (line.indexOf("## imagemap ##") > -1)
                    doc.append(map).append("\n");
                else
                    doc.append(line).append("\n");
                    
            // write out new doc
            FileWriter writer = new FileWriter(graphFile);
            writer.write(doc.toString());
            writer.flush();
            writer.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Writes the media files to the output location.
     */
    private void copyMediaResources(File outputDir) {        
        try {            
            ResourcePatternResolver resolver = 
                new PathMatchingResourcePatternResolver(new DefaultResourceLoader());
            Resource[] media = resolver.getResources(MEDIA_RESOURCES);
            
            for (int i = 0; i < media.length; i++) {
                File target = new File(outputDir, media[i].getFilename());
                logger.info("copying media resource [" + target.getAbsolutePath() + "]");
                FileOutputStream fos = new FileOutputStream(target);            
                InputStream is = media[i].getInputStream();
                byte[] buff = new byte[1];
                while (is.read(buff) != -1) fos.write(buff);        
                fos.flush();
                fos.close();
                is.close();
            }
            
        } catch (Exception e) {
            logger.error("Failed to move media resources to output directory", e);
        }
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
    
    /**
     * @return the format string to denote output type of the image map
     */
    public String getDotFileMapFormat() {
        return dotFileMapFormat;
    }

    /**
     * The image map format that Dot should use to generate an image map for the
     * context graphs.  Most likely to be "cmap" or "cmapx".  See GraphViz documentation
     * for more information.
     * 
     * @param dotFileMapFormat the format string to denote output type of the image map
     */
    public void setDotFileMapFormat(String dotFileMapFormat) {
        this.dotFileMapFormat = dotFileMapFormat;
    }

}
