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

import org.jdom.Document;
import org.jdom.Element;


/**
 * Generates a consolidated Document from the array of individual Document objects and
 * allows subclasses to transform this instead.  The generated DOM simply aggregates multiple 
 * &lt;beans&gt; tags inside a &lt;consolidated&gt; tag.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public abstract class AbstractConsolidatedTransformer extends BaseXslTransformer {

    private static final String TAG_CONSOLIDATED = "consolidated";
    
    protected Document consolidatedDocument;
    
    /**
     * @param templateName
     */
    public AbstractConsolidatedTransformer(String templateName) {
        super(templateName);
    }
    
    /**
     * @see org.springframework.beandoc.output.BaseXslTransformer#initTransform(org.jdom.Document[], java.io.File)
     */
    protected void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
        consolidatedDocument = new Document();
        Element root = new Element(TAG_CONSOLIDATED);
        consolidatedDocument.setRootElement(root);
        
        for (int i = 0; i < contextDocuments.length; i++) {
            Element inputRoot = (Element) contextDocuments[i].getRootElement().clone();
            root.addContent(inputRoot);
        }
    }

    /**
     * Override default behaviour to provide a single transformation of the consolidated 
     * DOM created.
     * 
     * @see org.springframework.beandoc.output.BaseXslTransformer#handleTransform
     */
    protected void handleTransform(Document[] contextDocuments, File outputDir) {
        doXslTransform(consolidatedDocument, outputDir);
    }

}
