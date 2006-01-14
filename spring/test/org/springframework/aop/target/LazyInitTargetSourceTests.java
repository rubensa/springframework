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

package org.springframework.aop.target;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 * @since 07.01.2005
 */
public class LazyInitTargetSourceTests extends TestCase {

	public void testLazyInitSingletonTargetSource() {
		XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("lazyInitSingletonTests.xml", getClass()));
		bf.preInstantiateSingletons();
		ITestBean tb = (ITestBean) bf.getBean("proxy");
		assertFalse(bf.containsSingleton("target"));
		assertEquals(10, tb.getAge());
		assertTrue(bf.containsSingleton("target"));
	}

	public void testCustomLazyInitSingletonTargetSource() {
		XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("customLazyInitTarget.xml", getClass()));
		bf.preInstantiateSingletons();
		ITestBean tb = (ITestBean) bf.getBean("proxy");
		assertFalse(bf.containsSingleton("target"));
		assertEquals("Rob Harrop", tb.getName());
		assertTrue(bf.containsSingleton("target"));
	}


	public static class CustomLazyInitTargetSource extends LazyInitTargetSource {

		protected void postProcessTargetObject(Object targetObject) {
			((ITestBean)targetObject).setName("Rob Harrop");
		}
	}
}
