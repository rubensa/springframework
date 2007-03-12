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
import org.jdom.input.SAXBuilder;
import org.springframework.beandoc.Tags;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.core.io.ClassPathResource;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class HtmlDecoratorTests extends TestCase {
    
    Document[] docs = new Document[1];
    List elementNames;
    HtmlDecorator hd;
    Element root;
    
    public void setUp() {
        elementNames = new ArrayList();
        
        hd = new HtmlDecorator();
        assertEquals("Application Context", hd.getTitle());
        
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new BeansDtdResolver());
        builder.setValidation(false);
        try {
            docs[0] = builder.build(new ClassPathResource("org/springframework/beandoc/context1.xml").getInputStream());
            root = docs[0].getRootElement();
        
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testNulls() {
        try {
            hd.setCssUrl(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }
        
        try {
            hd.setFooter(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        }        

        try {
            hd.setTitle(null);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        } catch (Throwable t) {
            fail("IllegalArgumentException should be thrown instead");
        } 
    }
    
    public void testRootElementDecoration() {
        assertEquals("default.css", hd.getCssUrl());
        hd.setCssUrl("http://my.intranet.server/beandoc.css");
        hd.setTitle("My Decorator Test");
        hd.setFooter("(c) Me!");
        
        hd.decorateElement(root);
        
        assertEquals("http://my.intranet.server/beandoc.css", root.getAttributeValue("beandocCssLocation"));
        assertEquals("My Decorator Test", root.getAttributeValue("beandocContextTitle"));
        assertEquals("(c) Me!", root.getAttributeValue("beandocPageFooter"));
        
        assertNull(root.getAttribute("beandocNoGraphs"));
        
        hd.setIncludeGraphs(false);
        hd.decorateElement(root);
        assertNotNull(root.getAttribute("beandocNoGraphs"));        
    }
    
    public void testBeanDecoration() {
        root.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, "context1.xml");
        hd.decorateElement(root);
        Element bean = root.getChild("bean");
        hd.decorateElement(bean);
        assertEquals("context1.xml.html", bean.getAttributeValue("beandocHtmlFileName"));
    }
    
    public void testRefDecoration() {
        Element ref = root.getChild("bean").getChild("property").getChild("ref");
        ref.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, "anotherFile.xml");
        hd.decorateElement(ref);
        assertEquals("anotherFile.xml.html", ref.getAttributeValue("beandocHtmlFileName"));        
    }
    
    public void testFilenameStrategy() {
        hd.setFilenameStrategy(new FilenameAppenderStrategy(".junit"));Element ref = root.getChild("bean").getChild("property").getChild("ref");
        ref.setAttribute(Tags.ATTRIBUTE_BD_FILENAME, "anotherFile.xml");
        hd.decorateElement(ref);
        assertEquals("anotherFile.xml.junit", ref.getAttributeValue("beandocHtmlFileName"));     
    }
}
