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

import org.springframework.core.io.Resource;

import junit.framework.TestCase;

/**
 * @author davison
 */
public class ConfigurationTests extends TestCase {
    
    static Resource[] testInputs;
    static File testOutputDir = new File(System.getProperty("user.home"));
    

    Configuration cfg;
    
    public void setUp() throws Exception {
        super.setUp();

        try {
            cfg = new Configuration(testInputs, testOutputDir);
        }
        catch (IOException e) {
            fail();
        }
    }
    
    /*
     * add some patterns to the ignore list and verify their status
     */
    public void testIgnoreBeans() {
        try {
            
            cfg.addIgnoreBeans("*Validator");
            cfg.addIgnoreBeans("org.springframework.samples*");
            cfg.addIgnoreBeans("simpleForm*");
            cfg.addIgnoreBeans("*.foo.bar");
            
            assertTrue(cfg.isBeanIgnored("myValidator", "anything"));
            assertTrue(cfg.isBeanIgnored("anything", "org.springframework.samples.IgnoreMe"));
            assertTrue(cfg.isBeanIgnored("simpleFormControllerTest", "anything"));
            assertTrue(cfg.isBeanIgnored("anything", "IgnoreMe.test.foo.bar"));
            
            assertFalse(cfg.isBeanIgnored("doNotIgnoreMe", "do.not.ignore"));
            
            
        } catch (Exception e) {
            fail();
        }        
    }
    
    public void testNullParamsForIgnoredLists() {
        try {
            // try first before adding anything to the ignored list (null list)
            assertFalse(cfg.isBeanIgnored("doNotIgnoreMe", "do.not.ignore"));
            
            // add patterns and throw null params at it
            cfg.addIgnoreBeans("*Validator");            
            assertFalse(cfg.isBeanIgnored(null, "any.old.Class"));            
            assertTrue(cfg.isBeanIgnored("someValidator", null));
            
        } catch (Exception e) {
            fail();
        }  
    }
    
    /*
     * test colour matches
     */
    public void testColourMatches() {
        try {
    
            cfg.addBeanColours("*Validator", "RED");
            cfg.addBeanColours("org.springframework.samples*", "BLUE");
            cfg.addBeanColours("simpleForm*", "GREEN");
            cfg.addBeanColours("*.foo.bar", "YELLOW");
            
            assertEquals("RED", cfg.getColourForBean("myValidator", "anything"));
            assertEquals("BLUE", cfg.getColourForBean("anything", "org.springframework.samples.AnyClass"));
            assertEquals("GREEN", cfg.getColourForBean("simpleFormControllerTest", "anything"));
            assertEquals("YELLOW", cfg.getColourForBean("anything", "com.foo.bar"));
            assertEquals(cfg.getDefaultFillColour(), cfg.getColourForBean("anything", "anything"));

    
        } catch (Exception e) {
            fail();
        }   
    }
    
    /*
     * test javadoc lookups
     */
    public void testJavaDocLocations() {
        try {
    
            cfg.addJavaDocLocation(
                "java.",
                "http://java.sun.com/j2se/1.4/docs/api/");
            cfg.addJavaDocLocation(
                "javax.",
                "http://java.sun.com/j2se/1.4/docs/api/");
            cfg.addJavaDocLocation(
                "org.springframework.",
                "http://www.springframework.org/docs/api/");
            cfg.addJavaDocLocation(
                "org.springframework.samples.",
                null);
                
            assertEquals(
                "http://www.springframework.org/docs/api/org/springframework/SomeClass.html", 
                cfg.getJavaDocForClassName("org.springframework.SomeClass"));
                
            assertEquals(
                "http://java.sun.com/j2se/1.4/docs/api/java/util/HashMap.html", 
                cfg.getJavaDocForClassName("java.util.HashMap"));
                
            assertEquals(
                "http://java.sun.com/j2se/1.4/docs/api/javax/sql/DataSource.html", 
                cfg.getJavaDocForClassName("javax.sql.DataSource"));
                
            assertNull(
                cfg.getJavaDocForClassName("org.springframework.samples.SomeClass"));
                                
        } catch (Exception e) {
            fail();
        }           
    }
}
