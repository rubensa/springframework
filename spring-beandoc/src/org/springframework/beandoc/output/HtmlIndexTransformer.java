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


/**
 * @author Darren Davison
 * @since 1.0
 */
public class HtmlIndexTransformer extends AbstractConsolidatedTransformer {
    
    private static final String DEFAULT_XSL_RESOURCE = 
        "/org/springframework/beandoc/output/stylesheets/main.xsl";   

    private static final String FILENAME_MAIN = "main.html";
    
    /**
     * default constructor uses the included stylesheet to generate HTML
     * output.
     */
    public HtmlIndexTransformer() {
        this(DEFAULT_XSL_RESOURCE);
    }
    
    /**
     * @param templateName
     */
    public HtmlIndexTransformer(String templateName) {
        super(templateName);
    }

    /**
     * @see org.springframework.beandoc.output.BaseXslTransformer#getOutputForDocument
     */
    protected String getOutputForDocument(String inputFileName) {
        return FILENAME_MAIN;
    }

}
