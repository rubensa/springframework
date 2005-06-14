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

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beandoc.BeanDocException;

/**
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class ConsolidatedTransformerTests extends TestCase {
    
    TestConsolidatedTransformer ct;
    File outputDir = new File(System.getProperty("user.dir"));
    Document[] docs = new Document[2];
    
    public void setUp() {
        ct = new TestConsolidatedTransformer();
        
        docs[0] = new Document();
        Element root1 = new Element("beans");
        docs[0].setRootElement(root1);
            
        docs[1] = new Document();
        Element root2 = new Element("beans");
        docs[1].setRootElement(root2);
            
            
    }
    
    public void testValidFileName() {
        ct.setFilenameRoot("anything");
        
        try {
            ct.setFilenameRoot(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
            assertEquals("anything", ct.getFilenameRoot());
        }
    }
    
    public void testConsolidation() {
        try {
            ct.setTemplateName("/org/springframework/beandoc/output/stylesheets/index.xsl");
            
            ct.initTransform(docs, outputDir);
            ct.handleTransform(docs, outputDir);
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testTemplate() {
        TestConsolidatedTransformer ct2;
        try {
            ct2 = new TestConsolidatedTransformer("/path/to/my/template");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BeanDocException);
        }
        
        try {
            ct2 = new TestConsolidatedTransformer("classpath:org/springframework/beandoc/transtest.xsl");
            assertEquals("classpath:org/springframework/beandoc/transtest.xsl", ct2.getTemplateName());
        } catch (Exception e) {
            fail();
        }
        
    }
    
    public void testTransform() {
        File outputDir = new File(System.getProperty("java.io.tmpdir"));
        ConsolidatedTransformer ct2 = new ConsolidatedTransformer();
        try {
            ct2.setTemplateName("classpath:org/springframework/beandoc/transtest.xsl");
            ct2.transform(docs, outputDir);
            
        } catch (Exception e) {
            fail();
        }
        
    }
    
    public void testNullTemplates() {
        File outputDir = new File(System.getProperty("java.io.tmpdir"));
        ConsolidatedTransformer ct2 = new ConsolidatedTransformer();
        try {
            ct2.transform(docs, outputDir);
            fail();
            
        } catch (IllegalStateException e) {
            // ok;
        }        
    }
        
    class TestConsolidatedTransformer extends ConsolidatedTransformer {        
        public TestConsolidatedTransformer() { super(); }
        public TestConsolidatedTransformer(String templateName) { super(templateName); }
        protected void handleTransform(Document[] contextDocuments, File outputDir) {
            assertEquals("consolidated", consolidatedDocument.getRootElement().getName());
            assertEquals(2, consolidatedDocument.getRootElement().getChildren("beans").size());
        }
    }
}
