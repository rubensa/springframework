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
package org.springframework.webflow.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;

/**
 * Mock implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This
 * is a simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>RequestContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.webflow.Action
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext, FlowExecutionContext {
	
	private String key = "Mock Flow Execution";

	private Event sourceEvent;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	private MockFlowSession activeSession = new MockFlowSession();

	private Event lastEvent;

	private Transition lastTransition;

	private AttributeSource properties = new MapAttributeSource();

	private boolean inTransaction;
	
	private long creationTimestamp = System.currentTimeMillis();
	
	private Flow rootFlow;
	
	private long lastRequestTimestamp = System.currentTimeMillis();

	/**
	 * Create a new stub request context.
	 */
	public MockRequestContext() {
		setSourceEvent(new Event(this, "start"));
		setActiveSession(new MockFlowSession());
	}
	
	/**
	 * Create a new stub request context.
	 * @param sourceEvent the event originating this request context
	 */
	public MockRequestContext(Event sourceEvent) {
		setSourceEvent(sourceEvent);
	}

	/**
	 * Create a new stub request context.
	 * @param session the active flow session
	 * @param sourceEvent the event originating this request context
	 */
	public MockRequestContext(MockFlowSession session, Event sourceEvent) {
		setActiveSession(session);
		setSourceEvent(sourceEvent);
	}
	
	// implementing RequestContext
	
	public Event getSourceEvent() {
		return sourceEvent;
	}

	/**
	 * Set the event originating this request context.
	 * @param sourceEvent the source event to set
	 */
	public void setSourceEvent(Event sourceEvent) {
		this.sourceEvent = sourceEvent;
		if (getLastEvent() == null) {
			setLastEvent(sourceEvent);
		}
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return this;
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public Scope getFlowScope() {
		return activeSession.getScope();
	}

	public Event getResultEvent(String stateId) {
		throw new UnsupportedOperationException();
	}
	
	public Event getLastEvent() {
		return lastEvent;
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the event to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastRequestTimestamp = System.currentTimeMillis();
		this.lastEvent = lastEvent;
	}

	public Transition getLastTransition() {
		return lastTransition;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	public AttributeSource getProperties() {
		return properties;
	}

	public void setProperties(AttributeSource properties) {
		this.properties = properties;
	}

	/**
	 * Set an execution property.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void setProperty(String attributeName, Object attributeValue) {
		((MutableAttributeSource)this.properties).setAttribute(attributeName, attributeValue);
	}

	public Map getModel() {
		// merge request and flow scope
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size());
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		return model;
	}

	public boolean inTransaction(boolean end) {
		boolean res = this.inTransaction;
		if (end) {
			endTransaction();
		}
		return res;
	}

	public void assertInTransaction(boolean end) throws IllegalStateException {
		Assert.state(inTransaction(end), "Not in application transaction but is expected to be");
	}

	public void beginTransaction() {
		inTransaction = true;
	}

	public void endTransaction() {
		inTransaction = false;
	}
	
	// implementing FlowExecutionContext

	public Serializable getKey() {
		return key;
	}

	public String getCaption() {
		return getActiveFlow().getId();
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public long getUptime() {
		return System.currentTimeMillis() - getCreationTimestamp();
	}
	
	public long getLastRequestTimestamp() {
		return lastRequestTimestamp;
	}
	
	public String getLastEventId() {
		return lastEvent.getId();
	}

	public boolean isActive() {
		return activeSession != null;
	}

	public boolean isRootFlowActive() {
		return activeSession != null && activeSession.isRoot();
	}

	public Flow getRootFlow() {
		return rootFlow;
	}
	
	/**
	 * Set the root flow of this request context.
	 * @param rootFlow the root flow to set
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
		if (this.activeSession.getFlow() == null) {
			this.activeSession.setFlow(rootFlow);
			this.activeSession.setCurrentState(rootFlow.getStartState());
		}
	}

	public Flow getActiveFlow() {
		return activeSession.getFlow();
	}
	
	public State getCurrentState() {
		State state = activeSession.getCurrentState();
		if (state == null) {
			throw new IllegalStateException("Active flow session 'currentState' not set");
		}
		return state;
	}

	/**
	 * Set the current state of this request context.
	 * @param state the current state to set
	 */
	public void setCurrentState(State state) {
		Assert.state(state.getFlow() == getActiveSession().getFlow(), "The current state to set must be a state in the active flow");
		this.activeSession.setCurrentState(state);
	}
	
	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session active");
		}
		return activeSession;
	}

	/**
	 * Set the active flow session of this request context.
	 * @param session the active flow session to set
	 */
	public void setActiveSession(MockFlowSession session) {
		Assert.notNull(session, "The session is required");
		this.activeSession = session;
		if (this.rootFlow == null) {
			this.rootFlow = session.getFlow();
		}
	}
}