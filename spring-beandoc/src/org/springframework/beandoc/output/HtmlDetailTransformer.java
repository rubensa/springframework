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
import java.io.FileOutputStream;
import java.io.InputStream;

import org.jdom.Document;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * Specifies a default XSL stylesheet, included in the beandoc tool for generating output.  This value
 * can be overridden in configuration.  The class will provide output filenames that simply replace the
 * file extension of the input file (.xml) with .html
 *
 * @author Darren Davison
 * @since 1.0
 */
public class HtmlDetailTransformer extends AbstractXslTransformer {
    
    private static final String MEDIA_RESOURCES = 
        "classpath:/org/springframework/beandoc/output/media/*";
    
    private static final String DEFAULT_XSL_RESOURCE = 
        "/org/springframework/beandoc/output/stylesheets/doc.xsl";    
    
    /**
     * default constructor uses the included stylesheet to generate HTML
     * output.
     */
    public HtmlDetailTransformer() {
        this(DEFAULT_XSL_RESOURCE);
    }
    
    /**
     * @param templateName
     */
    public HtmlDetailTransformer(String templateName) {
        super(templateName);
    }

    /**
     * Writes the frameset and media files to the output location.
     * 
     * @see org.springframework.beandoc.output.AbstractXslTransformer#initTransform
     */
    protected void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
        
        ResourcePatternResolver resolver = 
            new PathMatchingResourcePatternResolver(new DefaultResourceLoader());
        Resource[] media = resolver.getResources(MEDIA_RESOURCES);
        
        for (int i = 0; i < media.length; i++) {
            File target = new File(outputDirectory, media[i].getFilename());
            logger.info("copying media resource [" + target.getAbsolutePath() + "]");
            FileOutputStream fos = new FileOutputStream(target);            
            InputStream is = media[i].getInputStream();
            byte[] buff = new byte[1];
            while (is.read(buff) != -1) fos.write(buff);        
            fos.flush();
            fos.close();
            is.close();
        }
    }

    /**
     * Return a filename that switches the .xml extension for a .html one.
     * 
     * @see org.springframework.beandoc.output.AbstractXslTransformer#getOutputForDocument(java.lang.String)
     */
    protected String getOutputForDocument(String inputFileName) {
        String outputFileName = StringUtils.replace(inputFileName, ".xml", ".html");
        logger.debug("Returning [" + outputFileName + "] as output location for input file [" + inputFileName + "]");
        return outputFileName;
    }

}
