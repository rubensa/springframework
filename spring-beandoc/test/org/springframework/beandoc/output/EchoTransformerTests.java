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

import java.io.File;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class EchoTransformerTests extends TestCase {
    
    String sep = System.getProperty("line.separator");
    Document[] docs = new Document[1];
    String expect = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sep + 
        "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">" + sep + 
        "<beans default-autowire=\"no\" default-dependency-check=\"none\" default-lazy-init=\"false\">" + sep + 
        "<bean id=\"foo\" class=\"com.foo.Bar\" abstract=\"false\" autowire=\"default\" lazy-init=\"default\" dependency-check=\"default\" singleton=\"true\"><property name=\"bar\"><ref local=\"foo2\" /></property></bean>" + sep + 
        "<bean id=\"foo2\" class=\"com.bar.Foo\" abstract=\"false\" autowire=\"default\" lazy-init=\"default\" dependency-check=\"default\" singleton=\"true\" />" + sep + 
        "</beans>" + sep;
    
    public void setUp() {
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new BeansDtdResolver());
        builder.setValidation(false);
        try {
            docs[0] = builder.build(new ClassPathResource("org/springframework/beandoc/echotest.xml").getInputStream());
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testTransform() {
        EchoTransformer et = new EchoTransformer();
        StringWriter sw = new StringWriter();
        et.setWriter(sw);
        et.setPrettyPrint(false);
        
        et.transform(docs, new File(System.getProperty("user.home")));
        
        assertEquals(expect, sw.toString());
    }
}
