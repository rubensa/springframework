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

import junit.framework.TestCase;

import org.jdom.Element;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class CsvDecoratorTests extends TestCase {
    
    public void testCounter() {
        CsvDecorator cd = new CsvDecorator();
        Element bean = new Element("bean");
        cd.decorateElement(bean);
        assertEquals(1, Integer.parseInt(bean.getAttributeValue("beandocCSVCount")));
    }
}
