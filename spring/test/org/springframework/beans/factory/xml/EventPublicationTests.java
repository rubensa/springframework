/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MapBasedReaderEventListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class EventPublicationTests extends TestCase {

	public void testBeanEventReceived() throws Exception {
		MapBasedReaderEventListener eventListener = new MapBasedReaderEventListener();

		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		reader.setEventListener(eventListener);

		reader.loadBeanDefinitions(new ClassPathResource("beanEvents.xml", getClass()));

		ComponentDefinition componentDefinition = eventListener.getComponentDefinition("testBean");
		assertNotNull("Event not fired for bean", componentDefinition);
		assertTrue("ComponentDefinition for bean is of wrong type.", componentDefinition instanceof BeanComponentDefinition);

		BeanDefinition beanDefinition = componentDefinition.getBeanDefinitions()[0];
		assertNotNull("Underlying BeanDefinition is null", beanDefinition);
	}

}
