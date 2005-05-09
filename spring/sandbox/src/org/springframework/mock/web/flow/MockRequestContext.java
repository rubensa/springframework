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
package org.springframework.mock.web.flow;

import java.util.Map;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.util.Assert;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;
import org.springframework.web.flow.TransactionSynchronizer;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.execution.FlowExecutionListenerList;

/**
 * Mock implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone action unit tests. This
 * is a simple state holder, a stub implementation.
 * <p>
 * Note that this is really a <i>stub</i> implementation of the RequestContext
 * interface, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>RequestContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * TODO - belongs in the spring-mock.jar
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext, TransactionSynchronizer {

	private Flow rootFlow;

	private MockFlowSession activeSession = new MockFlowSession();
	
	private Event originatingEvent;

	private Event lastEvent;

	private Transition lastTransition;
	
	private MutableAttributeSource actionExecutionProperties = new MapAttributeSource();

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	private boolean inTransaction;

	/**
	 * Create a new stub request context.
	 */
	public MockRequestContext() {
	}

	/**
	 * Create a new stub request context.
	 * 
	 * @param activeFlow the active flow
	 * @param currentState the current state
	 * @param originatingEvent the event originating this request context
	 */
	public MockRequestContext(MockFlowSession session, Event originatingEvent) {
		setActiveSession(session);
		setOriginatingEvent(originatingEvent);
		setLastEvent(originatingEvent);
	}

	/**
	 * Set the root flow of this request context.
	 * 
	 * @param rootFlow the rootFlow to set
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
		if (this.activeSession.getFlow() == null) {
			this.activeSession.setFlow(rootFlow);
			this.activeSession.setState(rootFlow.getStartState());
		}
	}

	/**
	 * Set the active flow of this request context.
	 * 
	 * @param activeFlow the activeFlow to set
	 */
	public void setActiveSession(MockFlowSession session) {
		this.activeSession = session;
		if (this.rootFlow == null) {
			this.rootFlow = session.getFlow();
		}
	}

	/**
	 * Set the current state of this request context.
	 * 
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(State state) {
		Assert.state(state.getFlow() == getActiveSession().getFlow(), "The current state must be in the active flow");
		this.activeSession.setState(state);
	}

	/**
	 * Set the event originating this request context.
	 * 
	 * @param originatingEvent the originatingEvent to set
	 */
	public void setOriginatingEvent(Event originatingEvent) {
		this.originatingEvent = originatingEvent;
	}

	/**
	 * Set the last event that occured in this request context.
	 * 
	 * @param lastEvent the lastEvent to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * 
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	/**
	 * Set an action execution attribute that may be used by the action to effect 
	 * its behavior during execution.
	 * 
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void setActionExecutionAttribute(String attributeName, Object attributeValue) {
		this.actionExecutionProperties.setAttribute(attributeName, attributeValue);
	}
	
	public Flow getRootFlow() {
		return rootFlow;
	}

	public boolean isRootFlowActive() {
		return rootFlow != null && rootFlow == activeSession.getFlow();
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		if (activeSession == null) {
			throw new IllegalStateException("No flow session active");
		}
		return activeSession;
	}

	public boolean isActive() {
		return activeSession != null;
	}

	public Event getOriginatingEvent() {
		return originatingEvent;
	}

	public Event getLastEvent() {
		return lastEvent;
	}
	
	public Transition getLastTransition() {
		return lastTransition;
	}

	public AttributeSource getExecutionProperties() {
		return actionExecutionProperties;
	}

	public Scope getFlowScope() {
		return activeSession.getScope();
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public TransactionSynchronizer getTransactionSynchronizer() {
		return this;
	}

	/*
	 * Not supported, actions should really never call this
	 */
	public Map getModel() {
		throw new UnsupportedOperationException();
	}

	// transaction synchronizer stub methods

	public void assertInTransaction(boolean end) throws IllegalStateException {
		Assert.state(inTransaction, "Not in application transaction but is expected to be");
		if (end) {
			inTransaction = false;
		}
	}

	public void beginTransaction() {
		inTransaction = true;
	}

	public void endTransaction() {
		inTransaction = false;
	}

	public boolean inTransaction(boolean end) {
		boolean inTransaction = this.inTransaction;
		if (end) {
			this.inTransaction = false;
		}
		return inTransaction;
	}
}