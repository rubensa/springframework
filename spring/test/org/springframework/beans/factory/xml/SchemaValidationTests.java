/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.xml;

import junit.framework.TestCase;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 */
public class SchemaValidationTests extends TestCase {

	public void testWithAutodetection() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
		try {
			reader.loadBeanDefinitions(new ClassPathResource("invalidPerSchema.xml", getClass()));
			fail("Should not be able to parse a file with errors");
		}
		catch (BeansException ex) {
			assertTrue(ex.getCause() instanceof SAXParseException);
		}
	}

	public void testWithExplicitValidationMode() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		try {
			reader.loadBeanDefinitions(new ClassPathResource("invalidPerSchema.xml", getClass()));
			fail("Should not be able to parse a file with errors");
		}
		catch (BeansException ex) {
			assertTrue(ex.getCause() instanceof SAXParseException);
		}
	}

	public void testLoadDefinitions() throws Exception {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		reader.loadBeanDefinitions(new ClassPathResource("schemaValidated.xml", getClass()));

		TestBean foo = (TestBean) bf.getBean("fooBean");
		assertNotNull("Spouse is null", foo.getSpouse());
		assertEquals("Incorrect number of friends", 2, foo.getFriends().size());
	}

}
