/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.xml.support;

import junit.framework.TestCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.io.IOException;

/**
 * @author Rob Harrop
 */
public class CustomNamespaceHandlerTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.setParserClass(TestParser.class);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		reader.setEntityResolver(new PluggableSchemaResolver(getClass().getClassLoader()) {
			public InputSource resolveEntity(String publicId, String systemId) throws IOException {
				InputSource source = super.resolveEntity(publicId, systemId);

				if (source == null) {
					Resource resource = new ClassPathResource("org/springframework/beans/factory/xml/support/spring-test.xsd");
					source = new InputSource(resource.getInputStream());
					source.setPublicId(publicId);
					source.setSystemId(systemId);
				}

				return source;
			}
		});
		reader.loadBeanDefinitions(getResource());
	}

	public void testSimpleParser() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		assetTestBean(bean);
	}

	public void testSimpleDecorator() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("customisedTestBean");
		assetTestBean(bean);
	}

	public void testProxyingDecorator() throws Exception {
		ITestBean bean = (ITestBean) this.beanFactory.getBean("debuggingTestBean");
		assetTestBean(bean);
		assertTrue(AopUtils.isAopProxy(bean));
		Advisor[] advisors = ((Advised) bean).getAdvisors();
		assertEquals("Incorrect number of advisors", 1, advisors.length);
		assertEquals("Incorrect advice class.", DebugInterceptor.class, advisors[0].getAdvice().getClass());
	}

	public void testChainedDecorators() throws Exception {
		ITestBean bean = (ITestBean) this.beanFactory.getBean("chainedTestBean");
		assetTestBean(bean);
		assertTrue(AopUtils.isAopProxy(bean));
		Advisor[] advisors = ((Advised) bean).getAdvisors();
		assertEquals("Incorrect number of advisors", 2, advisors.length);
		assertEquals("Incorrect advice class.", DebugInterceptor.class, advisors[0].getAdvice().getClass());
		assertEquals("Incorrect advice class.", NopInterceptor.class, advisors[1].getAdvice().getClass());
	}

	private void assetTestBean(ITestBean bean) {
		assertEquals("Invalid name", "Rob Harrop", bean.getName());
		assertEquals("Invalid age", 23, bean.getAge());
	}

	private Resource getResource() {
		return new ClassPathResource("customNamespace.xml", getClass());
	}

	public static class TestParser extends DefaultXmlBeanDefinitionParser {

		protected NamespaceHandlerResolver createNamespaceHandlerResolver() {
			String location = "org/springframework/beans/factory/xml/support/customNamespace.properties";
			return new DefaultNamespaceHandlerResolver(location, getClass().getClassLoader());
		}
	}
}
