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

import org.jdom.Element;

import junit.framework.TestCase;



/**
 * JavaDocDecoratorTests
 * 
 * @author Darren Davison
 */
public class JavaDocDecoratorTests extends TestCase {

    JavaDocDecorator jdd = new JavaDocDecorator();
    
    /*
     * test javadoc lookups
     */
    public void testJavaDocLocations() {
        try {
            jdd.addJavaDocLocation(
                "org.springframework.emptyvalue.",
                "");
                
            Element e = new Element("bean");
            e.setAttribute("class", "org.springframework.SomeClass");
            jdd.decorateElement(e);            
            assertEquals(
                "http://www.springframework.org/docs/api/org/springframework/SomeClass.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                
            e.setAttribute("class", "java.util.HashMap");
            jdd.decorateElement(e);            
            assertEquals(
                "http://java.sun.com/j2se/1.4/docs/api/java/util/HashMap.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                
            e.setAttribute("class", "javax.sql.DataSource");
            jdd.decorateElement(e);            
            assertEquals(
                "http://java.sun.com/j2se/1.4/docs/api/javax/sql/DataSource.html", 
                e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                
            e = new Element("bean");
            e.setAttribute("class", "org.springframework.samples.SomeClass");
            jdd.decorateElement(e);            
            assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
            
            e = new Element("bean");
            e.setAttribute("class", "org.springframework.emptyvalue.SomeClass");
            jdd.decorateElement(e);            
            assertNull(e.getAttributeValue(JavaDocDecorator.ATTRIBUTE_JAVADOC));
                                
        } catch (Exception e) {
            fail();
        }           
    }
}
