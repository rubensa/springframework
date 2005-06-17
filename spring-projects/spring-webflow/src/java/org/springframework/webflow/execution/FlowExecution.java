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
package org.springframework.webflow.execution;

import org.springframework.webflow.Event;
import org.springframework.webflow.FlowContext;
import org.springframework.webflow.FlowNavigationException;
import org.springframework.webflow.ViewDescriptor;

/**
 * Represents a <i>client instance</i> of an executing flow. This is the central facade
 * interface for managing an execution of a single flow.
 * <p>
 * Typically, when the browser requests to execute a new flow, an instance of an object
 * implementing this interface is created by a controlling FlowExecutionManager.  After creation,
 * the start operation is called, which causes this execution to activate the requested flow
 * as the "root flow" and enter that flow's start state. After starting, when control
 * is returned back to the caller, this execution is saved in some form of storage, for example
 * in the HttpSession or a client-side hidden form field for later restoration and manipulation. 
 * <p>
 * Subsequent requests into the web flow system to manipulate an existing executing flow
 * trigger restoration and rehydration of this object, followed by an invocation of the
 * signalEvent operation. This continues until an event causes this flow execution to end, at
 * which time it is removed from storage and discarded.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowContext {
	
	/**
	 * Start a flow execution, transitioning the flow to the start state and
	 * returning the starting model and view descriptor. Typically called by a
	 * flow controller, but also from test code.
	 * @param sourceEvent the event that occured that triggered flow
	 *        execution creation
	 * @return the starting view descriptor, which returns control to the client
	 *         and requests that a view be rendered with model data
	 * @throws IllegalStateException if this execution has already been started,
	 *         or no state is marked as the start state.
	 */
	public ViewDescriptor start(Event sourceEvent) throws IllegalStateException;

	/**
	 * Signal an occurence of the specified event in the current state of this
	 * executing flow.
	 * @param sourceEvent the event that occured within the current state of this flow
	 *        execution.
	 * @return the next model and view descriptor to display for this flow
	 *         execution, this returns control to the client and requests that a
	 *         view be rendered with model data
	 * @throws FlowNavigationException if the signaled event does not map
	 *         to any state transitions in the current state
	 * @throws IllegalStateException if the flow execution is not active and
	 *         thus is no longer (or not yet) processing events
	 */
	public ViewDescriptor signalEvent(Event sourceEvent) throws FlowNavigationException, IllegalStateException;
	
	/**
	 * Rehydrate this flow execution after deserialization.
	 * @param flowLocator the flow locator
	 * @param listeners the flow execution listeners
	 * @param transactionSynchronizer application transaction synchronization
	 *        strategy to use
	 */
	public void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners,
			TransactionSynchronizer transactionSynchronizer);

	/**
	 * Return a list of listeners monitoring the lifecycle of this flow execution.
	 * @return the flow execution listeners
	 */
	public FlowExecutionListenerList getListeners();
}
