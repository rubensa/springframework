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
import java.util.*;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beandoc.output.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * DefaultContextProcessorTests
 * 
 * @author Darren Davison
 */
public class DefaultContextProcessorTests extends TestCase {
    
    DefaultContextProcessor dcp;
    int called;
    
    static Resource[] testInputs = {
        new ClassPathResource("org/springframework/beandoc/context1.xml"),
        new ClassPathResource("org/springframework/beandoc/context2.xml")
    };
    
    static File testOutputDir = new File(System.getProperty("user.home"));
    
    public void setUp() {
        try {
            dcp = new DefaultContextProcessor(testInputs, testOutputDir);
            dcp.setValidateFiles(false);
            this.called = 0;
            
        } catch (IOException e) {
            fail();
        }
    }
    
    public void testImportTags() throws Exception {
        DefaultContextProcessor dcpImport = new DefaultContextProcessor(
            new String[] {"classpath:org/springframework/beandoc/importer.xml"}, 
            testOutputDir
        );

        dcpImport.process();
        
    }
    
    public void testNullProxyMapThrowsException() {
        try {
            dcp.setMergeProxies(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Exception e) {
            fail("Method should throw IllegalArgumentException");
        }
    }
    
    public void testMiscMarkup() {        
        try {
            DefaultContextProcessor dcpMisc = new DefaultContextProcessor(
                new String[] {"classpath:org/springframework/beandoc/misc.xml"}, 
                testOutputDir
            );

            Map m = new HashMap();
            m.put("^foo99$", "target");
            dcpMisc.setValidateFiles(false);
            dcpMisc.setMergeProxies(m);

            dcpMisc.process();
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testInvalidXml() {        
        try {
            DefaultContextProcessor dcpInvalid = new DefaultContextProcessor(
                new String[] {"classpath:org/springframework/beandoc/invalid.xml"}, 
                testOutputDir
            );
            dcpInvalid.setValidateFiles(true);
            assertTrue(dcpInvalid.isValidateFiles());
            dcpInvalid.process();
            fail();
            
        } catch (Exception e) {
            assertTrue(e instanceof BeanDocException);
            assertTrue(e.getMessage().indexOf("Unable to parse or validate") > -1);
        }
    }
    
    public void testNoValidationOfInvalidXml() {        
        try {
            DefaultContextProcessor dcpInvalid = new DefaultContextProcessor(
                new String[] {"classpath:org/springframework/beandoc/invalid.xml"}, 
                testOutputDir
            );
            dcpInvalid.setValidateFiles(false);
            assertFalse(dcpInvalid.isValidateFiles());
            dcpInvalid.process();
            
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testStringInputs() {
        String[] testStringInputs = {
            "classpath:org/springframework/beandoc/context1.xml",
            "classpath:org/springframework/beandoc/context2.xml"
        };
        try {
            dcp = new DefaultContextProcessor(testStringInputs, testOutputDir);
            dcp = new DefaultContextProcessor(testStringInputs, System.getProperty("user.home"));
            Resource[] rezzas = dcp.getInputFiles();
            assertTrue(rezzas[0] instanceof ClassPathResource);
            assertEquals(dcp.getOutputDir(), testOutputDir);
            
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
                try {
                    Element beans = contextDocuments[0].getRootElement();  
                    assertEquals("context1.xml", beans.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME));
                    Element ref = beans.getChild("bean").getChild("property").getChild("ref");
                    assertEquals("context2.xml", ref.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME));                     
                } catch (Exception e) {
                    fail();                    
                }
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
    
    /**
     * @param b
     */
    protected void called() {
        called++;
    }
    
    public void testDecoratorsAreApplied() {
        assertNull(dcp.getDecorators());
        Decorator d = new Decorator() {
            public void decorate(Document[] contextDocuments) {
                called();
            }            
        };
        List dlist = new ArrayList(1);
        dlist.add(d);
        dcp.setDecorators(dlist);
        try {
            dcp.process();
        } catch (Exception e) {
            fail();
        }
        assertEquals(1, called);
    }

    public void testTransformersAreApplied() {
        assertNull(dcp.getTransformers());
        Transformer t = new Transformer() {
            public void transform(Document[] contextDocuments, File outputDir) {
                called();
            }            
        };
        List tlist = new ArrayList(2);
        tlist.add(t);
        tlist.add(t);
        dcp.setTransformers(tlist);
        try {
            dcp.process();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(2, called);
    }
    
    public void testCompilersAreApplied() {
        assertNull(dcp.getCompilers());
        DocumentCompiler c = new DocumentCompiler() {
            public void compile(Document[] contextDocuments, File outputDir) {
                called();
            }            
        };
        List clist = new ArrayList(2);
        clist.add(c);
        clist.add(c);
        dcp.setCompilers(clist);
        try {
            dcp.process();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(2, called);
    }
    
    public void testProxiesAreMerged() {
        assertEquals(0, dcp.getMergeProxies().size());
        Resource[] proxyInputs = {new ClassPathResource("org/springframework/beandoc/proxymerge.xml")};
        try {
            DefaultContextProcessor dcp2 = new DefaultContextProcessor(
                proxyInputs,
                testOutputDir
            );
            Map m = new HashMap();
            m.put("^myProxy$", "target");
            dcp2.setValidateFiles(false);
            dcp2.setMergeProxies(m);
            
            Decorator testMerge = new SimpleDecorator() {
                protected void decorateElement(Element element) {
                    if (Tags.TAGNAME_BEAN.equals(element.getName()) && 
                        ("myProxy".equals(element.getAttributeValue(Tags.ATTRIBUTE_ID)) ||
                         "myProxy".equals(element.getAttributeValue(Tags.ATTRIBUTE_NAME)))) {
                        called();
                        try {
                            Element target = element.getChild(Tags.TAGNAME_PROPERTY);                        
                            Element targetBean = target.getChild(Tags.TAGNAME_BEAN);
                        
                            // ref tag should have been replaced with bean tag
                            assertEquals(Tags.TAGNAME_BEAN, targetBean.getName());
                            assertEquals("myTarget", targetBean.getAttributeValue(Tags.ATTRIBUTE_NAME));
                        } catch (Exception e) {
                            fail();
                        }
                    }                        
                }                
            };
            
            List dlist = new ArrayList(1);
            dlist.add(testMerge);
            dcp2.setDecorators(dlist);
            try {
                dcp2.process();
            } catch (Exception e) {
                fail(e.getMessage());
            }
            
            assertEquals(1, called);
            
        } catch (IOException e) {
            fail();
        }
    }
    
    public void testDescriptionElementIsAddedToFilesWithoutOne() {
        Decorator testDesc = new SimpleDecorator() {
            protected void decorateElement(Element element) {
                if (Tags.TAGNAME_DESCRIPTION.equals(element.getName()))
                    called();                                  
            }                
        };
        
        List dlist = new ArrayList(1);
        dlist.add(testDesc);
        dcp.setDecorators(dlist);
        try {
            dcp.process();
        } catch (Exception e) {
            fail();
        }
        
        assertEquals(2, called);
    }

}
