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
package org.springframework.web.flow.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowModelMapper;
import org.springframework.web.flow.NoSuchFlowDefinitionException;
import org.springframework.web.flow.ServiceLookupException;

/**
 * A flow service locator that uses a Spring bean factory to lookup services.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class BeanFactoryFlowServiceLocator implements FlowServiceLocator, BeanFactoryAware {

	private BeanFactory beanFactory;

	/**
	 * Create a new service locator locating services in the bean factory
	 * that will be passed in using the <code>setBeanFactory()</code> method. 
	 */
	public BeanFactoryFlowServiceLocator() {
	}

	/**
	 * Create a new service locator locating services in given bean factory. 
	 */
	public BeanFactoryFlowServiceLocator(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * @return The bean factory used to lookup services.
	 */
	protected BeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException(
					"The bean factory reference has not yet been set for this BeanFactoryServiceLocator - call setBeanFactory()");
		}
		return beanFactory;
	}

	/**
	 * @return The bean factory used to lookup services.
	 */
	protected ListableBeanFactory getListableBeanFactory() {
		return (ListableBeanFactory)getBeanFactory();
	}

	public Action getAction(String actionId) throws ServiceLookupException {
		try {
			return (Action)getBeanFactory().getBean(actionId, Action.class);
		}
		catch (BeansException e) {
			throw new NoSuchActionException(actionId, e);
		}
	}

	public Action getAction(Class actionImplementationClass) throws ServiceLookupException {
		if (!Action.class.isAssignableFrom(actionImplementationClass)) {
			throw new IllegalArgumentException("Your action implementation '" + actionImplementationClass
					+ "' must implement the '" + Action.class.getName() + "' interface");
		}
		try {
			return (Action)BeanFactoryUtils.beanOfType(getListableBeanFactory(), actionImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchActionException(actionImplementationClass, e);
		}
	}

	public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
		try {
			return (Flow)getBeanFactory().getBean(flowDefinitionId, Flow.class);
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionId, e);
		}
	}

	public Flow getFlow(String flowDefinitionId, Class requiredBuilderImplementationClass)
			throws ServiceLookupException {
		if (requiredBuilderImplementationClass == null) {
			return getFlow(flowDefinitionId);
		}
		try {
			String flowFactoryBeanId = BeanFactory.FACTORY_BEAN_PREFIX + flowDefinitionId;
			FlowFactoryBean factoryBean = (FlowFactoryBean)getBeanFactory().getBean(flowFactoryBeanId,
					FlowFactoryBean.class);
			if (factoryBean.buildsWith(requiredBuilderImplementationClass)) {
				return factoryBean.getFlow();
			}
			else {
				throw new NoSuchFlowDefinitionException(flowDefinitionId, new IllegalStateException(
						"The flow factory must produce flows using a FlowBuilder of type '"
								+ requiredBuilderImplementationClass + "' but it doesn't"));
			}
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionId, e);
		}
	}

	public Flow getFlow(Class flowDefinitionImplementationClass) throws ServiceLookupException {
		try {
			if (!Flow.class.isAssignableFrom(flowDefinitionImplementationClass)) {
				throw new IllegalArgumentException("The flow definition implementation  '"
						+ flowDefinitionImplementationClass + "' you wish to retrieve must be a subclass of '"
						+ Flow.class.getName() + "'");
			}
			return (Flow)BeanFactoryUtils.beanOfType(getListableBeanFactory(), flowDefinitionImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionImplementationClass, e);
		}
	}

	public FlowModelMapper getFlowModelMapper(String flowModelMapperId)
			throws ServiceLookupException {
		try {
			return (FlowModelMapper)getBeanFactory().getBean(flowModelMapperId, FlowModelMapper.class);
		}
		catch (BeansException e) {
			throw new NoSuchFlowModelMapperException(flowModelMapperId, e);
		}
	}

	public FlowModelMapper getFlowModelMapper(Class flowModelMapperImplementationClass)
			throws ServiceLookupException {
		if (!FlowModelMapper.class.isAssignableFrom(flowModelMapperImplementationClass)) {
			throw new IllegalArgumentException("Your model mapper implementation '"
					+ flowModelMapperImplementationClass + "' must implement the '"
					+ FlowModelMapper.class.getName() + "' interface");

		}
		try {
			return (FlowModelMapper)BeanFactoryUtils.beanOfType(getListableBeanFactory(),
					flowModelMapperImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchFlowModelMapperException(flowModelMapperImplementationClass, e);
		}
	}
}