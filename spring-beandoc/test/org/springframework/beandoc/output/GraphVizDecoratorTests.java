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
        gvd.setDefaultFillColour("BLACK");
        
        gvd.addBeanColours(".*Validator", "RED");
        gvd.addBeanColours("^org.springframework.samples.*", "BLUE");
        gvd.addBeanColours("^simpleForm.*", "GREEN");
        gvd.addBeanColours(".*.foo.bar", "YELLOW");
        
        gvd.addIgnoreBeans(".*Validator");
        gvd.addIgnoreBeans("org.springframework.samples.*");
        gvd.addIgnoreBeans("^simpleForm.*");
        gvd.addIgnoreBeans(".*.foo.bar");
        
        gvd.addRankBeans(".*Dao");
        gvd.addRankBeans(".*DataSource");
        
        gvd.init();
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
    
    /*
     * test colour matches
     */
    public void testColourMatches() {
        
        Element e1 = new Element("bean");
        e1.setAttribute("id", "MyValidator");
        gvd.decorateElement(e1);
        assertEquals("RED", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("class", "org.springframework.samples.foo.Bar");
        gvd.decorateElement(e1);
        assertEquals("BLUE", e1.getAttributeValue(GraphVizDecorator.ATTRIBUTE_COLOUR));
        
        e1 = new Element("bean");
        e1.setAttribute("id", "simpleFormAnythingAtAll");
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
            gvd.addBeanColours(null, "#ffffff");
        } catch (Exception ex) {
            fail();
        }
        
        try {
            // null colour illegal
            gvd.addBeanColours(".*", null);
            fail();
        } catch (BeanDocException e) {
            // ok
        }
    }
    
    /*
     * add some patterns to the ignore list and verify their status
     */
    public void testIgnoreBeans() {
        
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
