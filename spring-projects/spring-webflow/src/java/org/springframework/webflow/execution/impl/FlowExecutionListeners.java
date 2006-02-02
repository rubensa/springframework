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
package org.springframework.webflow.execution.impl;

import java.util.Map;

import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecutionListener;

/**
 * A decorator that aids in publishing events to an array of
 * <code>FlowExecutionListener</code> objects.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionListener
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionListeners {

	/**
	 * The list of listeners that should receive event callbacks during managed
	 * flow executions (client sessions).
	 */
	private FlowExecutionListener[] listeners;

	/**
	 * Create a flow execution listener helper that wraps an empty listener
	 * array.
	 */
	public FlowExecutionListeners() {
		this(null);
	}

	/**
	 * Create a flow execution listener helper that wraps the specified listener
	 * array.
	 * @param listeners the listener array
	 */
	public FlowExecutionListeners(FlowExecutionListener[] listeners) {
		if (listeners != null) {
			this.listeners = listeners;
		}
		else {
			this.listeners = new FlowExecutionListener[0];
		}
	}

	/**
	 * Returns the wrapped listener array.
	 * @return the listener array
	 */
	public FlowExecutionListener[] getArray() {
		return listeners;
	}

	/**
	 * Returns the size of the listener array.
	 */
	public int size() {
		return listeners.length;
	}

	// methods to fire events to all listeners

	/**
	 * Notify all interested listeners that a request was submitted to the flow
	 * execution.
	 */
	public void fireRequestSubmitted(RequestContext context) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].requestSubmitted(context);
		}
	}

	/**
	 * Notify all interested listeners that the flow execution finished
	 * processing a request.
	 */
	public void fireRequestProcessed(RequestContext context) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].requestProcessed(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session is
	 * starting.
	 */
	public void fireSessionStarting(RequestContext context, State startState, Map input) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].sessionStarting(context, startState, input);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has
	 * started.
	 */
	public void fireSessionStarted(RequestContext context) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].sessionStarted(context);
		}
	}

	/**
	 * Notify all interested listeners that an event was signaled in the flow
	 * execution.
	 */
	public void fireEventSignaled(RequestContext context) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].eventSignaled(context, context.getFlowExecutionContext().getCurrentState());
		}
	}

	/**
	 * Notify all interested listeners that an event was signaled in the flow
	 * execution.
	 */
	public void fireEventSignaled(RequestContext context, State state) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].eventSignaled(context, state);
		}
	}

	/**
	 * Notify all interested listeners that a state is being entered in the flow
	 * execution.
	 */
	public void fireStateEntering(RequestContext context, State nextState) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].stateEntering(context, nextState);
		}
	}

	/**
	 * Notify all interested listeners that a state was entered in the flow
	 * execution.
	 */
	public void fireStateEntered(RequestContext context, State previousState) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].stateEntered(context, previousState, context.getFlowExecutionContext().getCurrentState());
		}
	}

	/**
	 * Notify all interested listeners that a flow session was activated in the
	 * flow execution.
	 */
	public void fireResumed(RequestContext context) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resumed(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow session was paused in the
	 * flow execution.
	 */
	public void firePaused(RequestContext context, ViewSelection selectedView) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].paused(context, selectedView);
		}
	}

	/**
	 * Notify all interested listeners that the active flow execution session is
	 * ending.
	 */
	public void fireSessionEnding(RequestContext context, Map sessionOutput) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].sessionEnding(context, sessionOutput);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has ended.
	 */
	public void fireSessionEnded(RequestContext context, FlowSession endedSession, Map sessionOutput) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].sessionEnded(context, endedSession, sessionOutput);
		}
	}
}