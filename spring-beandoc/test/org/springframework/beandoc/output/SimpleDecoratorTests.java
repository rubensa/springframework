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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.core.io.ClassPathResource;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class SimpleDecoratorTests extends TestCase {
    
    Document[] docs = new Document[1];
    List elementNames;
    SimpleDecorator sd;
    
    public void setUp() {
        elementNames = new ArrayList();
        
        sd = new SimpleDecorator() {
            protected void decorateElement(Element element) {
                elementNames.add(element.getName());
            }            
        };
        
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new BeansDtdResolver());
        builder.setValidation(false);
        try {
            docs[0] = builder.build(new ClassPathResource("org/springframework/beandoc/context1.xml").getInputStream());
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testDefaultDecorationUsesAllElements() {
        sd.decorate(docs);   
        assertEquals(4, elementNames.size());
        assertEquals("beans", elementNames.get(0));
        assertEquals("bean", elementNames.get(1));
        assertEquals("property", elementNames.get(2));
        assertEquals("ref", elementNames.get(3));
    }
    
    public void testFilteredDecorationUsingFilter() {
        sd.setFilter(new ElementFilter("bean"));
        sd.decorate(docs);   
        assertEquals(1, elementNames.size());
        assertEquals("bean", elementNames.get(0));
    }
    
    public void testFilteredDecorationUsingNullFilterThrowsException() {
        try {
            sd.setFilter(null);
            fail("Null filter should not be allowed");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }
    
    public void testValidFilterMatchingNothing() {
        sd.setFilter(new ElementFilter("noSuchElementInTheContextFiles"));
        sd.decorate(docs);   
        assertEquals(0, elementNames.size());     
    }
    
    public void testFilteredDecorationUsingNullOrEmptyStringArrayThrowsException() {
        try {
            sd.setFilterNames(null);
            fail("Null filterNames should not be allowed");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        
        String[] noNames = new String[0];
        try {
            sd.setFilterNames(noNames);
            fail("Empty filterNames should not be allowed");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }
    
    public void testFilteredDecorationUsingSingleName() {
        sd.setFilterNames(new String[] {"bean"});
        sd.decorate(docs);   
        assertEquals(1, elementNames.size());
        assertEquals("bean", elementNames.get(0));
    }
    
    public void testFilteredDecorationUsingMultipleNames() {
        sd.setFilterNames(new String[] {"bean", "ref"});
        sd.decorate(docs);   
        assertEquals(2, elementNames.size());
        assertEquals("bean", elementNames.get(0));
        assertEquals("ref", elementNames.get(1));
    }
    
    public void testRootElementInFilteredIterator() {
        SimpleDecorator sd2 = new SimpleDecorator() {
            protected void decorateElement(Element element) {
                assertTrue(element.isRootElement());
                assertEquals("beans", element.getName());
            }            
        };
        
        sd2.setFilterNames(new String[] {"beans"});
        sd2.decorate(docs);   
    }
}
