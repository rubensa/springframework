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

import junit.framework.TestCase;

/**
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class ConsolidatedTransformerTests extends TestCase {
    
    TestConsolidatedTransformer ct;
    File outputDir = new File(System.getProperty("user.dir"));
    
    public void setUp() {
        ct = new TestConsolidatedTransformer();
    }
    
    public void testConsolidation() {
        try {
            ct.setTemplateName("/org/springframework/beandoc/output/stylesheets/index.xsl");
            
            Document[] docs = new Document[2];
            docs[0] = new Document();
            Element root1 = new Element("beans");
            docs[0].setRootElement(root1);
            
            docs[1] = new Document();
            Element root2 = new Element("beans");
            docs[1].setRootElement(root2);
            
            ct.initTransform(docs, outputDir);
            ct.handleTransform(docs, outputDir);
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    class TestConsolidatedTransformer extends ConsolidatedTransformer {        
        protected void handleTransform(Document[] contextDocuments, File outputDir) {
            assertEquals("consolidated", consolidatedDocument.getRootElement().getName());
            assertEquals(2, consolidatedDocument.getRootElement().getChildren("beans").size());
        }
    }
}
