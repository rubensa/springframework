/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.execution.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.attribute.AttributeCollection;
import org.springframework.binding.attribute.AttributeMap;
import org.springframework.binding.attribute.EmptyAttributeCollection;
import org.springframework.binding.attribute.UnmodifiableAttributeMap;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.State;
import org.springframework.webflow.StateException;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewSelection;

/**
 * Default flow execution control context implementation used internally by the
 * web flow system. This class is closely coupled with
 * <code>FlowExecutionImpl</code> and <code>FlowSessionImpl</code>. The
 * three classes work together to form a complete flow execution implementation.
 * 
 * @see org.springframework.webflow.execution.impl.FlowExecutionImpl
 * @see org.springframework.webflow.execution.impl.FlowSessionImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionControlContextImpl implements FlowExecutionControlContext {

	protected static final Log logger = LogFactory.getLog(FlowExecutionControlContextImpl.class);

	/**
	 * The owning flow execution.
	 */
	private FlowExecutionImpl flowExecution;

	/**
	 * The request scope data map.
	 */
	private AttributeMap requestScope = new AttributeMap();

	/**
	 * The original event that triggered the creation of this state context.
	 */
	private ExternalContext externalContext;

	/**
	 * The last event that occured in this context.
	 */
	private Event lastEvent;

	/**
	 * The last transition that executed in this context.
	 */
	private Transition lastTransition;

	/**
	 * Holder for contextual execution properties.
	 */
	private UnmodifiableAttributeMap attributes;

	/**
	 * Create a new request context.
	 * @param flowExecution the owning flow execution
	 * @param externalContext the external context that originated the flow
	 * execution request
	 */
	public FlowExecutionControlContextImpl(FlowExecutionImpl flowExecution, ExternalContext externalContext) {
		Assert.notNull(flowExecution, "The owning flow execution is required");
		this.externalContext = externalContext;
		this.flowExecution = flowExecution;
	}

	// implementing RequestContext

	public Flow getActiveFlow() {
		return flowExecution.getActiveSession().getFlow();
	}

	public State getCurrentState() {
		return flowExecution.getActiveSession().getState();
	}

	public AttributeMap getRequestScope() {
		return requestScope;
	}

	public AttributeMap getFlowScope() {
		return flowExecution.getActiveSession().getScope();
	}

	public AttributeMap getConversationScope() {
		return flowExecution.getScope();
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecution;
	}

	public UnmodifiableAttributeMap getRequestParameters() {
		return externalContext.getRequestParameterMap();
	}

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	public UnmodifiableAttributeMap getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeCollection attributes) {
		if (attributes == null) {
			attributes = EmptyAttributeCollection.INSTANCE;
		}
		this.attributes = attributes.unmodifiable();
	}

	public UnmodifiableAttributeMap getModel() {
		return getConversationScope().union(getFlowScope()).union(getRequestScope()).unmodifiable();
	}

	// implementing FlowExecutionControlContext

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	public void setCurrentState(State state) {
		flowExecution.getListeners().fireStateEntering(this, state);
		State previousState = getCurrentState();
		flowExecution.setCurrentState(state);
		if (previousState == null) {
			flowExecution.getActiveSessionInternal().setStatus(FlowSessionStatus.ACTIVE);
		}
		flowExecution.getListeners().fireStateEntered(this, previousState);
	}

	public ViewSelection start(Flow flow, State startState, AttributeMap input) throws StateException {
		if (input == null) {
			// create a mutable map so entries can be added by listeners!
			input = new AttributeMap();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Activating new session for flow '" + flow.getId() + "' in state '"
					+ (startState != null ? startState.getId() : flow.getStartState().getId()) + "' with input "
					+ input);
		}
		flowExecution.getListeners().fireSessionStarting(this, startState, input);
		flowExecution.activateSession(flow, input);
		ViewSelection selectedView = flow.start(startState, this);
		flowExecution.getListeners().fireSessionStarted(this);
		return selectedView;
	}

	public ViewSelection signalEvent(Event event) throws StateException {
		if (logger.isDebugEnabled()) {
			logger.debug("Signaling event '" + event.getId() + "' in state '" + getCurrentState().getId()
					+ "' of flow '" + getActiveFlow().getId() + "'");
		}
		setLastEvent(event);
		flowExecution.getListeners().fireEventSignaled(this);
		ViewSelection selectedView = getActiveFlow().onEvent(event, this);
		return selectedView;
	}

	public FlowSession endActiveFlowSession(AttributeMap sessionOutput) throws IllegalStateException {
		flowExecution.getListeners().fireSessionEnding(this, sessionOutput);
		if (logger.isDebugEnabled()) {
			logger.debug("Ending active session " + getFlowExecutionContext().getActiveSession());
		}
		getActiveFlow().end(this, sessionOutput);
		FlowSession endedSession = flowExecution.endActiveFlowSession();
		flowExecution.getListeners().fireSessionEnded(this, endedSession, sessionOutput.unmodifiable());
		return endedSession;
	}

	public String toString() {
		return new ToStringCreator(this).append("externalContext", externalContext)
				.append("requestScope", requestScope).append("executionProperties", attributes).append("flowExecution",
						flowExecution).toString();
	}
}