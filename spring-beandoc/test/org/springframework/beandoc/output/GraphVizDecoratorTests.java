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

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beandoc.BeanDocException;

import junit.framework.TestCase;



/**
 * GraphVizDecoratorTests
 * 
 * @author Darren Davison
 */
public class GraphVizDecoratorTests extends TestCase {

    GraphVizDecorator gvd;
    
    public void setUp() {
        gvd = new GraphVizDecorator();
        
        gvd.addBeanColours("*Validator", "RED");
        gvd.addBeanColours("org.springframework.samples*", "BLUE");
        gvd.addBeanColours("simpleForm*", "GREEN");
        gvd.addBeanColours("*.foo.bar", "YELLOW");       
        
        gvd.setDefaultFillColour("BLACK");
    }
    
    /*
     * test colour matches
     */
    public void testColourMatches() {
        try {
            assertEquals("RED", gvd.getColourForBean("myValidator", "anything"));
            assertEquals("BLUE", gvd.getColourForBean("anything", "org.springframework.samples.AnyClass"));
            assertEquals("GREEN", gvd.getColourForBean("simpleFormControllerTest", "anything"));
            assertEquals("YELLOW", gvd.getColourForBean("anything", "com.foo.bar"));
            assertEquals(gvd.getDefaultFillColour(), gvd.getColourForBean("anything", "anything"));
            
            try {
                // null pattern ignored
                gvd.addBeanColours(null, "#ffffff");
            } catch (Exception e) {
                fail();
            }
            
            try {
                // null colour illegal
                gvd.addBeanColours("*", null);
                fail();
            } catch (BeanDocException e) {
                // ok
            }
    
        } catch (Exception e) {
            fail();
        }   
    }
    
    public void testRootElementDecoration() {
        gvd.setBeanShape("ellipse");
        gvd.setFontName("Times");
        gvd.setFontSize(34);
        gvd.setRatio("auto");
        
        Document d = new Document();
        Element e = new Element("beans");
        d.setRootElement(e);
        
        gvd.decorateElement(e);
        
        assertEquals("ellipse", e.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_BEANSHAPE));
        assertEquals("Times", e.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_FONTNAME));
        assertEquals("34", e.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_FONTSIZE));
        assertEquals("auto", e.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RATIO));
        
    }
    
    public void testBeanElementDecoration() {
        Element e = new Element("bean");
        e.setAttribute("id", "MyValidator");
        gvd.decorateElement(e);
        assertEquals("RED", e.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        Element e1 = new Element("bean");
        e1.setAttribute("class", "org.springframework.samples.foo.Bar");
        gvd.decorateElement(e1);
        assertEquals("BLUE", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1.setAttribute("class", "something.else");
        gvd.decorateElement(e1);
        assertEquals("BLACK", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
    }
    
    /*
     * add some patterns to the ignore list and verify their status
     */
    public void testIgnoreBeans() {
        try {
            
            gvd.addIgnoreBeans("*Validator");
            gvd.addIgnoreBeans("org.springframework.samples*");
            gvd.addIgnoreBeans("simpleForm*");
            gvd.addIgnoreBeans("*.foo.bar");
            
            assertTrue(gvd.isBeanIgnored("myValidator", "anything"));
            assertTrue(gvd.isBeanIgnored("anything", "org.springframework.samples.IgnoreMe"));
            assertTrue(gvd.isBeanIgnored("simpleFormControllerTest", "anything"));
            assertTrue(gvd.isBeanIgnored("anything", "IgnoreMe.test.foo.bar"));
            
            assertFalse(gvd.isBeanIgnored("doNotIgnoreMe", "do.not.ignore"));
            
            
        } catch (Exception e) {
            fail();
        }        
    }
    
    public void testNullParamsForIgnoredLists() {
        try {
            // try first before adding anything to the ignored list (null list)
            assertFalse(gvd.isBeanIgnored("doNotIgnoreMe", "do.not.ignore"));
            
            // add patterns and throw null params at it
            gvd.addIgnoreBeans("*Validator");            
            assertFalse(gvd.isBeanIgnored(null, "any.old.Class"));            
            assertTrue(gvd.isBeanIgnored("someValidator", null));
            
        } catch (Exception e) {
            fail();
        }  
    }
    
}
