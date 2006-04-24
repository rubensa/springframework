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
package org.springframework.webflow.builder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.convert.support.TextToExpression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.method.TextToMethodSignature;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.support.DefaultExpressionParserFactory;

/**
 * Base implementation that implements a minimal set of the
 * <code>FlowArtifactFactory</code> interface, throwing unsupported operation
 * exceptions for some operations.
 * <p>
 * May be subclassed to offer additional factory/lookup support.
 * @author Keith Donald
 */
public class BaseFlowServiceLocator implements FlowServiceLocator {

	/**
	 * The factory encapsulating the creation of central Flow artifacts such as
	 * {@link Flow flows} and {@link State states}.
	 */
	private FlowArtifactFactory flowArtifactFactory = new FlowArtifactFactory();

	/**
	 * The factory for creating bean invoking actions, actions that adapt
	 * methods on objects to the {@link Action} interface.
	 */
	private BeanInvokingActionFactory beanInvokingActionFactory = new BeanInvokingActionFactory();

	/**
	 * The parser for parsing expression strings into evaluatable expression
	 * objects.
	 */
	private ExpressionParser expressionParser = new DefaultExpressionParserFactory().getExpressionParser();

	/**
	 * A conversion service that can convert between types.
	 */
	private ConversionService conversionService = createDefaultConversionService();

	/**
	 * A resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * Sets the factory encapsulating the creation of central Flow artifacts
	 * such as {@link Flow flows} and {@link State states}.
	 */
	public void setFlowArtifactFactory(FlowArtifactFactory flowEntityFactory) {
		this.flowArtifactFactory = flowEntityFactory;
	}

	/**
	 * Sets the factory for creating bean invoking actions, actions that adapt
	 * methods on objects to the {@link Action} interface.
	 */
	public void setBeanInvokingActionFactory(BeanInvokingActionFactory beanInvokingActionFactory) {
		this.beanInvokingActionFactory = beanInvokingActionFactory;
	}

	/**
	 * Set the expression parser responsible for parsing expression strings into
	 * evaluatable expression objects.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	/**
	 * Set the conversion service to use to convert between types; typically
	 * from string to a rich object type.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Set the resource loader to load file-based resources from string-encoded
	 * paths.
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Flow.class, "Subflow lookup is not supported by this artifact factory");
	}

	public Action getAction(String id) throws FlowArtifactException {
		return (Action)getService(id, Action.class);
	}

	public boolean isAction(String actionId) throws FlowArtifactException {
		return Action.class.isAssignableFrom(getServiceType(actionId, Action.class));
	}

	public boolean isMultiAction(String actionId) throws FlowArtifactException {
		if (containsService(actionId)) {
			return MultiAction.class.isAssignableFrom(getBeanFactory().getType(actionId));
		}
		else {
			return false;
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		return (FlowAttributeMapper)getService(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		return (TransitionCriteria)getService(id, TransitionCriteria.class);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		return (ViewSelector)getService(id, ViewSelector.class);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		return (TargetStateResolver)getService(id, TargetStateResolver.class);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		return (StateExceptionHandler)getService(id, StateExceptionHandler.class);
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return beanInvokingActionFactory;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public ResourceLoader getResourceLoader() throws UnsupportedOperationException {
		return resourceLoader;
	}

	public BeanFactory getBeanFactory() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Service registry lookup is not supported by this artifact factory");
	}

	private ConversionService createDefaultConversionService() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter(new TextToTransitionCriteria(this));
		service.addConverter(new TextToViewSelector(this));
		service.addConverter(new TextToTransitionTargetStateResolver(this));
		service.addConverter(new TextToExpression(getExpressionParser()));
		service.addConverter(new TextToMethodSignature(service));
		return service;
	}

	protected boolean isPrototype(String beanId) {
		if (containsService(beanId)) {
			return !getBeanFactory().isSingleton(beanId);
		}
		else {
			return false;
		}
	}

	protected boolean containsService(String id) {
		return getBeanFactory().containsBean(id);
	}

	protected Object getService(String id, Class artifactType) {
		try {
			return getBeanFactory().getBean(id, artifactType);
		}
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}

	protected Class getServiceType(String id, Class artifactType) {
		try {
			return getBeanFactory().getType(id);
		}
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}
}