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
package org.springframework.web.flow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ToStringCreator;

/**
 * A state that has one or more transitions. State transitions are triggered by
 * events, specifically, when execution of an event in this state is requested.
 * 
 * @author Keith Donald
 */
public abstract class TransitionableState extends AbstractState {
	private Set transitions = new LinkedHashSet();

	public TransitionableState(String id) {
		super(id);
	}

	public TransitionableState(String id, Transition transition) {
		super(id);
		add(transition);
	}

	public TransitionableState(String id, Transition[] transitions) {
		super(id);
		addAll(transitions);
	}

	public boolean isTransitionable() {
		return true;
	}

	public void add(Transition transition) {
		transitions.add(transition);
	}

	public void addAll(Transition[] transitions) {
		this.transitions.addAll(Arrays.asList(transitions));
	}

	public Collection getTransitions() {
		return Collections.unmodifiableSet(transitions);
	}

	protected Transition getTransition(String eventId, Flow flow) throws NoSuchTransitionInStateException {
		Iterator it = transitions.iterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.matches(eventId)) {
				return transition;
			}
		}
		throw new NoSuchTransitionInStateException(flow, this, eventId);
	}

	/**
	 * Execute the event identified by <code>eventId</code> in this state
	 * 
	 * @param eventId The id of the event to execute (e.g 'submit', 'next',
	 *        'back')
	 * @param flow The flow definition
	 * @param sessionExecutionStack A flow session execution stack, tracking any
	 *        suspended parent flows that spawned this flow (as a subflow)
	 * @param request the client http request
	 * @param response the server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 * @throws IllegalArgumentException if the <code>eventId</code> does not
	 *         map to a valid transition for this state.
	 */
	public ViewDescriptor execute(String eventId, FlowSessionExecutionStack sessionExecution,
			HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException {
		updateCurrentStateIfNeccessary(eventId, sessionExecution);
		String activeFlowId = null;
		if (logger.isDebugEnabled()) {
			activeFlowId = sessionExecution.getQualifiedActiveFlowId();
			logger.debug("Event '" + eventId + "' within state '" + getId() + "' for flow '" + activeFlowId
					+ "' signaled");
		}
		sessionExecution.setLastEventId(eventId);
		ViewDescriptor viewDescriptor = getTransition(eventId, sessionExecution.getActiveFlow()).execute(eventId, this,
				sessionExecution, request, response);
		if (logger.isDebugEnabled()) {
			if (sessionExecution.isActive()) {
				logger.debug("Event '" + eventId + "' within last state '" + this + "' for flow '" + activeFlowId
						+ "' was processed; as a result, the new state is '" + sessionExecution.getCurrentStateId()
						+ "' in flow '" + sessionExecution.getQualifiedActiveFlowId() + "'");
			}
			else {
				logger.debug("Event '" + eventId + "' within last state '" + this + "' for flow '" + activeFlowId
						+ "' was processed; as a result, flow session execution has ended");
			}
		}
		return viewDescriptor;
	}

	protected void updateCurrentStateIfNeccessary(String eventId, FlowSessionExecutionStack sessionExecution) {
		if (!this.equals(sessionExecution.getCurrentState())) {
			if (logger.isInfoEnabled()) {
				logger.info("Event '" + eventId + "' in state '" + getId()
						+ "' was signaled by client; however the current flow session execution state is '"
						+ sessionExecution.getCurrentStateId() + "'; updating current state to '" + getId() + "'");
			}
			sessionExecution.setCurrentState(this);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("id", getId()).append("transitions", transitions).toString();
	}

}