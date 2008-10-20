/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.context.config;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Arjen Poutsma
 * @since 2.5.6
 */
public class ContextNamespaceHandlerTests extends TestCase {

	private ApplicationContext applicationContext;

	protected void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("contextNamespaceHandlerTests.xml", getClass());
	}

	public void testPropertyPlaceholder() throws Exception {
		Map beans = applicationContext.getBeansOfType(PropertyPlaceholderConfigurer.class);
		assertFalse("No PropertyPlaceHolderConfigurer found", beans.isEmpty());
		String s = (String) applicationContext.getBean("string");
		assertEquals("No properties replaced", "bar", s);
	}

	public void testPropertyOverride() throws Exception {
		Map beans = applicationContext.getBeansOfType(PropertyOverrideConfigurer.class);
		assertFalse("No PropertyOverrideConfigurer found", beans.isEmpty());
		Date date = (Date) applicationContext.getBean("date");
		assertEquals("No properties overriden", 42, date.getMinutes());
	}
}
