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

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class CsvTransformerTests extends TestCase {
    
    public void testCounter() {
        Document[] docs = new Document[1];
        docs[0] = new Document();
        Element root = new Element("beans");
        root.setAttribute("beandocFileName", "beandoctest");
        docs[0].setRootElement(root);
        
        Element bean = new Element("bean");
        bean.setAttribute("id", "myBean");
        root.addContent(bean);
        
        CsvTransformer ct = new CsvTransformer();
        ct.transform(docs, new File(System.getProperty("java.io.tmpdir")));
    }
}
