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

import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.jdom.Element;



/**
 * JavaDocDecoratorTests
 * 
 * @author Darren Davison
 */
public class JavaDocDecoratorTests extends TestCase {

    private static String javaVersion = System.getProperty("java.specification.version");
    
    JavaDocDecorator jdd = new JavaDocDecorator();
    Element e;
    
    public void setUp() {
        e = new Element("bean");
    }
    
    public void testSetLocationMap() {
        try {
            jdd.setLocations(null);
            fail();
        } catch (IllegalArgumentException e ) {
            // ok
        }
        
        try {
            jdd.setLocations(new TreeMap());
        } catch (Exception e) {
            fail();
        }
    }
    
    /*
     * test javadoc lookups
     */
    public void testKnownLocations() {
        try {
                
            e.setAttribute("class", "org.springframework.SomeClass");
            e.setAttribute("id", "aBean");
            jdd.decorateElement(e);            
            assertEquals(
                "http://static.springframework.org/spring/docs/2.0.x/api/org/springframework/SomeClass.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                
            e.setAttribute("class", "java.util.HashMap");
            e.setAttribute("name", "bBean");
            jdd.decorateElement(e);            
            assertEquals(
                "http://java.sun.com/j2se/" + javaVersion + "/docs/api/java/util/HashMap.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                
            e.setAttribute("class", "javax.sql.DataSource");
            jdd.decorateElement(e);            
            assertEquals(
                "http://java.sun.com/j2se/" + javaVersion + "/docs/api/javax/sql/DataSource.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                                       
        } catch (Exception e) {
            fail();
        }           
    }
        
    public void testNoConfiguredLocation() {
        e.setAttribute("class", "org.springframework.samples.SomeClass");
        jdd.decorateElement(e);            
        assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
    }
    
    public void testEmptyLocation() {   
        jdd.addLocation(
            "org.springframework.emptyvalue.",
            "");
        e.setAttribute("class", "org.springframework.emptyvalue.SomeClass");
        jdd.decorateElement(e);            
        assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
    }
    
    public void testNoTrailingSlashInLocation() {   
        jdd.addLocation(
            "com.foo.",
            "http://my.server/docs");
        e.setAttribute("class", "com.foo.SomeClass");
        jdd.decorateElement(e);            
        assertEquals("http://my.server/docs/com/foo/SomeClass.html", e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
    }
    
    public void testNullClass() {
        e.setAttribute("id", "aBean");
        jdd.decorateElement(e);
        assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
    }
    
    public void testUnknownClass() {
        e.setAttribute("class", "no.location.configured.for.ThisClass");
        jdd.decorateElement(e);
        assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
    }
    
    public void testNullClassPrefixForLocation() {
        try {
            jdd.addLocation(null, "http://nowhere.to.go");
            SortedMap m = jdd.getLocations();
            assertFalse(m.containsValue("http://nowhere.to.go/"));
            
        } catch (Exception e) {
            fail();
        }
    }
        
}
