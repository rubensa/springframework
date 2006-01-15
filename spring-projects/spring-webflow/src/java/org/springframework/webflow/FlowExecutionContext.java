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
package org.springframework.webflow;

/**
 * Provides contextual information about an actively executing flow representing
 * exactly one web conversation. An object implementing this interface is
 * available from the request context (see {@link org.springframework.webflow.RequestContext#getFlowExecutionContext()}).
 * <p>
 * This is an immutable interface for accessing information about exactly one
 * FlowExecution. It extends FlowExecutionStatistics, adding in strongly typed
 * accessors for retrieving runtime objects such as the active flow session, as
 * well as definition objects such as the top-level flow definition, the
 * currently active flow definition, and the current state definition.
 * <p>
 * Note that this interface provides information about a single flow execution
 * and its scope is <b>not</b> local to a specific request (or thread). On the
 * other hand, the
 * {@link org.springframework.webflow.FlowExecutionControlContext} interface
 * defines a <i>request specific</i> control interface for manipulating exactly
 * one flow execution locally from exactly one request.
 * 
 * @see RequestContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionContext extends FlowExecutionStatistics {

	/**
	 * Returns the root flow definition associated with this executing flow.
	 * @return the root flow definition
	 */
	public Flow getRootFlow();

	/**
	 * Returns the definition of the flow that is currently executing.
	 * @return the flow definition for the active session
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all, or if this execution has ended and is no longer actively
	 * executing
	 */
	public Flow getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the current state of the executing flow. May return
	 * <code>null</code> if this flow execution is in the process of starting
	 * and has not yet entered its start state.
	 * @return the current state, or <code>null</code> if in the process of
	 * starting.
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all, or if this execution has ended and is no longer actively
	 * executing
	 */
	public State getCurrentState() throws IllegalStateException;

	/**
	 * Returns the active flow session of this flow execution.
	 * @return the active flow session
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all, or if this execution has ended and is no longer actively
	 * executing
	 */
	public FlowSession getActiveSession() throws IllegalStateException;
}