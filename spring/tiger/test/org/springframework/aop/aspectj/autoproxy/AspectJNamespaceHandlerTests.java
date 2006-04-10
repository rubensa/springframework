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

package org.springframework.aop.aspectj.autoproxy;

import junit.framework.TestCase;

import org.springframework.aop.config.NamespaceHandlerUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author Rob Harrop
 */
public class AspectJNamespaceHandlerTests extends TestCase {

	public void testRegisterAutoProxyCreator() throws Exception {
		BeanDefinitionRegistry registry = new DefaultListableBeanFactory();

		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());
	}

	public void testRegisterAspectJAutoProxyCreator() throws Exception {
		BeanDefinitionRegistry registry = new DefaultListableBeanFactory();

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("Incorrect APC class", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

	public void testRegisterAspectJAutoProxyCreatorWithExistingAutoProxyCreator() throws Exception {
		BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);
		assertEquals(1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect definition count", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("APC class not swicthed", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

	public void testRegisterAutoProxyCreatorWhenAspectJAutoProxyCreatorAlreadyExists() throws Exception {
		BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(registry);
		assertEquals(1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);
		assertEquals("Incorrect definition count", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("Incorrect APC class", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

}
