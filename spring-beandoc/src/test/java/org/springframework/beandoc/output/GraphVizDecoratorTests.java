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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;



/**
 * GraphVizDecoratorTests
 * 
 * @author Darren Davison
 */
public class GraphVizDecoratorTests extends TestCase {

    GraphVizDecorator gvd;
    Element root;
    Document d;
        
        
    
    public void setUp() {
        d = new Document();
        root = new Element("beans");
        d.setRootElement(root);
       
        
        gvd = new GraphVizDecorator();               
        gvd.setDefaultFillColour("BLACK");
        
        gvd.addColourBeans(".*Validator", "RED");
        gvd.addColourBeans("^org.springframework.samples.*", "BLUE");
        gvd.addColourBeans("^simpleForm.*", "GREEN");
        gvd.addColourBeans(".*.foo.bar", "YELLOW");
        
        gvd.addIgnoreBeans(".*Validator");
        gvd.addIgnoreBeans("org.springframework.samples.*");
        gvd.addIgnoreBeans("^simpleForm.*");
        gvd.addIgnoreBeans(".*.foo.bar");
        
        gvd.addRankBeans(".*Dao");
        gvd.addRankBeans(".*DataSource");
        
        gvd.init();
    }
    
    public void testNulls() {
        try {
            gvd.setBeanShape(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }
        
        try {
            gvd.setColourBeans(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }        

        try {
            gvd.setDefaultFillColour(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }  

        try {
            gvd.setFontName(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }  

        try {
            gvd.setIgnoreBeans(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }
        
        assertEquals(4, gvd.getIgnoreBeans().size());
        gvd.addIgnoreBeans(null);
        assertEquals(4, gvd.getIgnoreBeans().size());
        gvd.setIgnoreBeans(new ArrayList());
        assertEquals(0, gvd.getIgnoreBeans().size());
        
        try {
            gvd.setOutputType(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }

        try {
            gvd.setOutputType(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }

        try {
            gvd.setRankBeans(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }
        
        assertEquals(2, gvd.getRankBeans().size());
        gvd.addRankBeans(null);
        assertEquals(2, gvd.getRankBeans().size());
        

        try {
            gvd.setRatio(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }
    }
    
    public void testDefaults() {
        assertEquals(9, gvd.getFontSize());
        assertEquals("sans", gvd.getFontName());
        assertEquals("auto", gvd.getRatio());
        assertEquals("ellipse", gvd.getBeanShape());
        assertEquals("png", gvd.getOutputType());
        assertEquals(2, gvd.getRankBeans().size());   
        assertEquals(-1f, gvd.getGraphXSize(), 0);    
        assertEquals(-1f, gvd.getGraphYSize(), 0);        
        
        gvd.setRankBeans(new ArrayList());
        assertEquals(0, gvd.getRankBeans().size());        
    }
    
    public void testRootElementDecoration() {
        gvd.setBeanShape("ellipse");
        gvd.setFontName("Times");
        gvd.setFontSize(34);
        gvd.setRatio("auto");        
        
        gvd.decorateElement(root);
        
        assertEquals("ellipse", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_BEANSHAPE));
        assertEquals("Times", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_FONTNAME));
        assertEquals("34", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_FONTSIZE));
        assertEquals("auto", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RATIO));
        
    }
    
    public void testGraphSize() {
        gvd.setGraphXSize(0.5f);
        gvd.decorateElement(root);
        assertNull(root.getAttribute(GraphVizDecorator.ATTRIBUTE_GRAPH_SIZE));
        
        gvd.setGraphYSize(0.6f);
        gvd.decorateElement(root);
        assertEquals("0.5, 0.6", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_SIZE));
    }
    
    public void testSetColourBeansMap() {
        Date date = new Date();
        Map m = new HashMap();
        m.put(".*Foo", "CRIMSON");
        m.put(date, "MARIGOLD"); // not a string key
        m.put(".*Bar", null); // not a string value
        m.put(".*Baz", "TURQUOISE"); // ok
        gvd.setColourBeans(m);
        
        Map m2 = gvd.getColourBeans();
        assertTrue(m2.containsKey(".*Foo"));
        assertFalse(m2.containsKey(date));
        assertFalse(m2.containsKey(".*Bar"));
        assertTrue(m2.containsKey(".*Baz"));
    }
    
    public void testSetLabelLocation() {
        gvd.decorateElement(root);
        assertEquals("t", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_LABELLOCATION));
        
        gvd.setLabelLocation("bottom");
        gvd.decorateElement(root);
        assertEquals("b", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_LABELLOCATION));
        
        gvd.setLabelLocation("invalid");
        assertEquals('b', gvd.getLabelLocation());
        
        try {
            gvd.setLabelLocation(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
    
    public void testSetOutputType() {
        gvd.decorateElement(root);
        assertEquals("png", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_TYPE));
        
        gvd.setOutputType("svg");
        gvd.decorateElement(root);
        assertEquals("svg", root.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_TYPE));
        
        gvd.setOutputType("JPG");
        assertEquals("jpg", gvd.getOutputType());
    }
    
    /*
     * test colour matches
     */
    public void testColourMatches() {
        
        Map m = gvd.getColourBeans();
        assertEquals(10, m.size());
        
        Element e1 = new Element("bean");
        e1.setAttribute("id", "MyValidator");
        gvd.decorateElement(e1);
        assertEquals("RED", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "org.springframework.samples.foo.Bar");
        gvd.decorateElement(e1);
        assertEquals("BLUE", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("name", "simpleFormAnythingAtAll");
        gvd.decorateElement(e1);
        assertEquals("GREEN", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "javax.foo.bar");
        gvd.decorateElement(e1);
        assertEquals("YELLOW", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "whatever");
        gvd.decorateElement(e1);
        assertEquals("BLACK", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
    }
    
    public void testNullParamsForColours() {
        try {
            // null pattern ignored
            gvd.addColourBeans(null, "#ffffff");
        } catch (Exception ex) {
            fail();
        }
        
        try {
            // null colour illegal
            gvd.addColourBeans(".*", null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
    
    /*
     * add some patterns to the ignore list and verify their status
     */
    public void testIgnoreBeans() {
        
        List l = gvd.getIgnoreBeans();
        assertEquals(4, l.size());
        
        Element e1 = new Element("bean");
        e1.setAttribute("id", "MyValidator");
        gvd.decorateElement(e1);
        assertEquals("true", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_IGNORE));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "org.springframework.samples.foo.Bar");
        gvd.decorateElement(e1);
        assertEquals("true", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_IGNORE));
        
        e1 = new Element("bean");
        e1.setAttribute("id", "simpleFormAnythingAtAll");
        gvd.decorateElement(e1);
        assertEquals("true", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_IGNORE));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "javax.foo.bar");
        gvd.decorateElement(e1);
        assertEquals("true", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_IGNORE));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "whatever");
        gvd.decorateElement(e1);
        assertNull(e1.getAttribute(GraphVizDecorator.ATTRIBUTE_GRAPH_IGNORE));    
    }
    
    public void testRankedBeans() {
        
        List l = gvd.getRankBeans();
        assertEquals(2, l.size());
        
        Element e1 = new Element("bean");
        e1.setAttribute("id", "MyDao");
        gvd.decorateElement(e1);
        assertNotNull(e1.getAttribute(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK));
        
        Element e2 = new Element("bean");
        e2.setAttribute("id", "MyOtherDao");
        gvd.decorateElement(e2);
        assertEquals(
            e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK), 
            e2.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK)
        );
        
        Element e3 = new Element("bean");
        e3.setAttribute("id", "MyDataSource");
        gvd.decorateElement(e3);
        assertNotNull(e3.getAttribute(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK));
        assertNotSame(
            e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK), 
            e3.getAttributeValue(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK)
        );

        e1 = new Element("bean");
        e1.setAttribute("id", "something");
        gvd.decorateElement(e1);
        assertNull(e1.getAttribute(GraphVizDecorator.ATTRIBUTE_GRAPH_RANK));
    }
}
