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

import java.util.Stack;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.MultiAction;

/**
 * Searches flow-local registries first before querying the global, externally
 * managed flow service locator.
 * @author Keith Donald
 */
class LocalFlowServiceLocator implements FlowServiceLocator {

	/**
	 * The stack of registries.
	 */
	private Stack localRegistries = new Stack();

	/**
	 * The parent factory.
	 */
	private FlowServiceLocator rootFactory;

	/**
	 * Creates a new local artifact factory.
	 * @param rootFactory the root parent factory
	 */
	public LocalFlowServiceLocator(FlowServiceLocator rootFactory) {
		this.rootFactory = rootFactory;
	}

	/**
	 * Push a new registry onto the stack
	 * @param registry the local registry
	 */
	public void push(LocalFlowServiceRegistry registry) {
		registry.init(this, rootFactory);
		localRegistries.push(registry);
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		Flow currentFlow = getCurrentFlow();
		// quick check for recursive subflow
		if (currentFlow.getId().equals(id)) {
			return currentFlow;
		}
		// check local inline flows
		if (currentFlow.containsInlineFlow(id)) {
			return currentFlow.getInlineFlow(id);
		}
		// check externally managed toplevel flows
		return rootFactory.getSubflow(id);
	}

	public Action getAction(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (Action)getBean(id, Action.class);
		}
		else {
			return rootFactory.getAction(id);
		}
	}

	public boolean isAction(String actionId) throws FlowArtifactException {
		if (containsBean(actionId)) {
			return Action.class.isAssignableFrom(getBeanFactory().getType(actionId));
		}
		else {
			return rootFactory.isAction(actionId);
		}
	}

	public boolean isMultiAction(String actionId) {
		if (containsBean(actionId)) {
			return MultiAction.class.isAssignableFrom(getBeanFactory().getType(actionId));
		}
		else {
			return rootFactory.isMultiAction(actionId);
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (FlowAttributeMapper)getBean(id, FlowAttributeMapper.class);
		}
		else {
			return rootFactory.getAttributeMapper(id);
		}
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (StateExceptionHandler)getBean(id, StateExceptionHandler.class);
		}
		else {
			return rootFactory.getExceptionHandler(id);
		}
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (TransitionCriteria)getBean(id, TransitionCriteria.class);
		}
		else {
			return rootFactory.getTransitionCriteria(id);
		}
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (ViewSelector)getBean(id, ViewSelector.class);
		}
		else {
			return rootFactory.getViewSelector(id);
		}
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		if (containsBean(id)) {
			return (TargetStateResolver)getBean(id, TargetStateResolver.class);
		}
		else {
			return rootFactory.getTargetStateResolver(id);
		}
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		return rootFactory.getFlowArtifactFactory();
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return rootFactory.getBeanInvokingActionFactory();
	}

	public ConversionService getConversionService() {
		return rootFactory.getConversionService();
	}

	public ExpressionParser getExpressionParser() {
		return rootFactory.getExpressionParser();
	}

	public ResourceLoader getResourceLoader() {
		return rootFactory.getResourceLoader();
	}

	public BeanFactory getBeanFactory() {
		return top().getContext();
	}

	/**
	 * Pop a registry off the stack
	 */
	public LocalFlowServiceRegistry pop() {
		return (LocalFlowServiceRegistry)localRegistries.pop();
	}

	/**
	 * Returns the top registry on the stack
	 */
	public LocalFlowServiceRegistry top() {
		return (LocalFlowServiceRegistry)localRegistries.peek();
	}

	/**
	 * Returns true if this locator has no local registries.
	 */
	public boolean isEmpty() {
		return localRegistries.isEmpty();
	}

	/**
	 * Returns the flow for the registry at the top of the stack.
	 */
	public Flow getCurrentFlow() {
		return top().getFlow();
	}

	protected boolean containsBean(String id) {
		if (localRegistries.isEmpty()) {
			return false;
		}
		else {
			return getBeanFactory().containsBean(id);
		}
	}

	protected Object getBean(String id, Class artifactType) {
		try {
			return getBeanFactory().getBean(id, artifactType);
		}
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}
}