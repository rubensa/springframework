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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Configurable with a file extension value, this class generates an output file name using
 * the supplied extension to replace the input file extension for each document in
 * the transform list.  The same XSL template is used to transform each document.
 *
 * @author Darren Davison
 * @since 1.0
 */
public class SimpleTransformer extends AbstractXslTransformer {
    
    private String fileExtension = ".html";

    /**
     * Return a filename that switches the .xml extension for a .html one.
     * 
     * @see org.springframework.beandoc.output.AbstractXslTransformer#getOutputForDocument(java.lang.String)
     */
    protected String getOutputForDocument(String inputFileName) {
        String outputFileName = StringUtils.replace(inputFileName, ".xml", fileExtension);
        logger.debug("Returning [" + outputFileName + "] as output location for input file [" + inputFileName + "]");
        return outputFileName;
    }

    /**
     * @return the extension to use in the transformed file
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * For each document that is to be transformed by this class, specify a file extension to
     * use which will replace the file extension of the input file.  Defaults to ".html" such that
     * a context file input of foo.xml will be transformed and stored in foo.html in the 
     * configured output location.
     * 
     * @param fileExtension the extension to use in the transformed file
     */
    public void setFileExtension(String fileExtension) {
        Assert.hasLength(fileExtension);
        this.fileExtension = fileExtension;
    }

}
