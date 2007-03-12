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

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.springframework.beandoc.BeanDocException;
import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class EchoTransformerTests extends TestCase {
    
    Document[] docs = new Document[1];
    
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
        String[] expect = { 
            "default-autowire=\"no\"",
            "default-dependency-check=\"none\"",
            "default-lazy-init=\"false\"", 
            "<bean id=\"foo\" class=\"com.foo.Bar\"",
            "<property name=\"bar\"><ref local=\"foo2\" /></property></bean>", 
            "<bean id=\"foo2\" class=\"com.bar.Foo\"", 
            "</beans>"
        };
        
        EchoTransformer et = new EchoTransformer();
        StringWriter sw = new StringWriter();
        et.setWriter(sw);
        et.setPrettyPrint(false);
        
        et.transform(docs, new File(System.getProperty("user.home")));
        
        System.out.println(sw.toString());
        for (int i = 0; i < expect.length; i++)
            assertTrue("Failed to find " + expect[i] + " in the output",
                sw.toString().indexOf(expect[i]) > -1);
    }
    
    public void testNullWriter() {
        EchoTransformer et = new EchoTransformer();
        StringWriter sw = new StringWriter();
        et.setWriter(sw);
        try {
            et.setWriter(null);
            fail();
        } catch(IllegalArgumentException e) {
            // ok
            return;
        }
        fail();
    }
    
    public void testInvalidWriter() {
        EchoTransformer et = new EchoTransformer();
        et.setWriter(new Writer() {
            public void close() throws IOException { }
            public void flush() throws IOException { }
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Not a very useful Writer is it?");
            }
        });
        et.setPrettyPrint(true);

        try {
            et.transform(docs, new File(System.getProperty("user.home")));
            fail();
        } catch (BeanDocException bde) {
            // ok
        }

    }
}
