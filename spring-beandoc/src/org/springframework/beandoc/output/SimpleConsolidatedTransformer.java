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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;


/**
 * Uses a stylesheet which generates the frameset document and adds the custom title.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class SimpleConsolidatedTransformer extends ConsolidatedTransformer {
    
    private static final String DEFAULT_XSL_RESOURCE = 
        "/org/springframework/beandoc/output/stylesheets/index.xsl";   

    private static final String FILENAME_INDEX = "index.html";
    
    /**
     * default constructor uses the included stylesheet to generate HTML
     * output.
     */
    public SimpleConsolidatedTransformer() {
        this(DEFAULT_XSL_RESOURCE);
    }
    
    /**
     * @param templateName
     */
    public SimpleConsolidatedTransformer(String templateName) {
        super(templateName);
    }

    /**
     * @see org.springframework.beandoc.output.AbstractXslTransformer#getOutputForDocument
     */
    protected String getOutputForDocument(String inputFileName) {
        return FILENAME_INDEX;
    }

}
