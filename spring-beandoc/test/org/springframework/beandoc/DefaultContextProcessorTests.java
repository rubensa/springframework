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

package org.springframework.beandoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.springframework.beandoc.output.Transformer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;



/**
 * DefaultContextProcessorTests
 * 
 * @author Darren Davison
 */
public class DefaultContextProcessorTests extends TestCase {
    
    DefaultContextProcessor dcp;

    static Resource[] testInputs = {
        new ClassPathResource("org/springframework/beandoc/context1.xml"),
        new ClassPathResource("org/springframework/beandoc/context2.xml")
    };
    
    static File testOutputDir = new File(System.getProperty("user.home"));
    
    public void setUp() {
        try {
            dcp = new DefaultContextProcessor(testInputs, testOutputDir);
            dcp.setValidateFiles(false);
        } catch (IOException e) {
            fail();
        }
    }
    
    public void testNoOutputDirectoryThrowsIOException() {
        try {
            dcp = new DefaultContextProcessor(testInputs, new File("/hopefully/no/such/directory/exists/"));
            fail();
        } catch (IOException e) {
            // ok
        }
    }
    
    public void testRefMarkup() {
        Transformer t = new Transformer() {
            public void transform(Document[] contextDocuments, File outputDir) {
                assertEquals(2, contextDocuments.length);                
                /*
                 * requires Jaxen
                 * TODO  
                try {
                    Element root = contextDocuments[0].getRootElement();                                       
                    String refFile = ((Element) XPath.selectSingleNode(root, ".//bean[@id='foo']/property[@name='bar']/ref/@bean")).getText();
                    assertEquals("context2.xml", refFile);                     
                } catch (JDOMException e) {
                    fail();                    
                }
                 */
            }            
        };
        
        List tx = new ArrayList();
        tx.add(t);
        dcp.setTransformers(tx);
        try {
            dcp.process();
        } catch (Exception e) {
            fail();
        }
    }

}
