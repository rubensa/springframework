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
package org.springframework.webflow.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.access.BeanFactoryFlowServiceLocator;
import org.springframework.webflow.access.FlowServiceLocator;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations.
 * <p>
 * The builder will use a <code>FlowServiceLocator</code> to locate and create
 * any required flow related artifacts.
 * 
 * @see org.springframework.webflow.access.FlowServiceLocator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder implements FlowBuilder, BeanFactoryAware {

	/**
	 * A logger instance that can be used in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The service locator that locates and creates flow related artifacts by
	 * identifier or implementation class, as needed by this builder.
	 */
	private FlowServiceLocator flowServiceLocator = new BeanFactoryFlowServiceLocator();

	/**
	 * The <code>Flow</code> produced by this builder.
	 */
	private Flow flow;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}

	/**
	 * Create a base flow builder which will pull services (other flow defs,
	 * actions, model mappers, etc.) from given service locator.
	 * @param flowServiceLocator the locator
	 */
	protected BaseFlowBuilder(FlowServiceLocator flowServiceLocator) {
		setFlowServiceLocator(flowServiceLocator);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (flowServiceLocator instanceof BeanFactoryFlowServiceLocator) {
			((BeanFactoryFlowServiceLocator)flowServiceLocator).setBeanFactory(beanFactory);
		}
	}
	
	/**
	 * Returns the flow service location strategy in use.
	 */
	protected FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	/**
	 * Set the flow service location strategy to use.
	 */
	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		Assert.notNull(flowServiceLocator, "The flow service locator is required");
		this.flowServiceLocator = flowServiceLocator;
	}

	/**
	 * Get the flow (result) built by this builder.
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow being built by this builder.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getResult() {
		return getFlow();
	}
	
	/**
	 * Returns the type conversion service used by this builder.
	 */
	protected ConversionService getConversionService() {
		return getFlowServiceLocator().getConversionService();
	}

	/**
	 * Returns a conversion executor capable of converting string objects to the 
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable converter exists
	 *         for given alias
	 */
	protected ConversionExecutor fromStringToAliased(String targetAlias) {
		return getConversionService().getConversionExecutorByTargetAlias(String.class, targetAlias);
	}

	/**
	 * Returns a converter capable of converting a string value to the given type.
	 * @param targetType the type you wish to convert to (from a string)
	 * @return the converter
	 * @throws ConversionException when the converter cannot be found
	 */
	protected ConversionExecutor fromStringTo(Class targetType) throws ConversionException {
		return getConversionService().getConversionExecutor(String.class, targetType);
	}
}