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
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

/**
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class DoAbstractTransformerTests extends TestCase {
    
    TestAbstractTransformer tat;
    TestBetterTransformer tbt;
    
    File outputDir = new File(System.getProperty("user.dir"));
    Document[] docs = new Document[2];
    
    public void setUp() {
        tat = new TestAbstractTransformer();
        tbt = new TestBetterTransformer();
        
        docs[0] = new Document();
        Element root1 = new Element("beans");
        docs[0].setRootElement(root1);
            
        docs[1] = new Document();
        Element root2 = new Element("beans");
        docs[1].setRootElement(root2);
            
            
    }
    
    public void testInitExceptionStopsTransform() {
        File outputDir = new File(System.getProperty("java.io.tmpdir"));
        try {
            tat.setTemplateName("classpath:org/springframework/beandoc/transtest.xsl");        
            tat.transform(docs, outputDir);
            
        } catch (Exception e) {
            fail();
        }        
    }
    
    public void testTransform() {
        File outputDir = new File(System.getProperty("java.io.tmpdir"));
        try {
            tbt.setTemplateName("classpath:org/springframework/beandoc/transtest.xsl");

            Map m = new HashMap();
            m.put("param1", "Hello World");
            
            assertNull(tbt.getStaticParameters());
            tbt.setStaticParameters(m);    
            assertEquals(m, tbt.getStaticParameters());
            
            tbt.transform(docs, outputDir);
            
        } catch (Exception e) {
            fail();
        }        
    }
        
    class TestAbstractTransformer extends AbstractXslTransformer {        
        protected void initTransform(Document[] contextDocuments, File outputDir) throws Exception {
            throw new Exception("Cannot init me!");
        }
        
        protected String getOutputForDocument(String inputFileName) {
            return "beandoc-transform-test.txt";
        }
        protected void handleTransform(Document[] contextDocuments, File outputDirectory) {
            // should never be called since init throws Exc.
            fail();
        }
    }
        
    class TestBetterTransformer extends AbstractXslTransformer {        
        protected String getOutputForDocument(String inputFileName) {
            return "beandoc-transform-test.txt";
        }

    }
}
