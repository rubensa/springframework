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
package org.springframework.webflow.executor;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;

/**
 * The central facade or entry-point into the Spring Web Flow system. This
 * inteface defines a coarse-grained system boundary suitable for invocation by
 * most clients.
 * <p>
 * Implementations of this interface abstract away much of the the internal
 * complexity of the webflow execution subsystem, which consists of launching
 * and resuming managed flow executions from repositories.
 * 
 * @author Keith Donald
 */
public interface FlowExecutor {

	/**
	 * Launch a new execution of the flow provided in the context of the current
	 * external request.
	 * @param flowId the unique id of the flow definition to launch
	 * @param context the external context representing the state of a request
	 * into Spring Web Flow from an external system.
	 * @return the starting view selection, or <code>null</code> if no view
	 * selection was made.
	 * @throws FlowException if an exception occured launching the new flow
	 * execution.
	 */
	public ViewSelection launch(String flowId, ExternalContext context) throws FlowException;

	/**
	 * Signal an occurrence of an event in the current state of an existing,
	 * paused flow execution continuation. The flow execution will resume to
	 * process the event.
	 * @param eventId the user event that occured
	 * @param flowExecutionId the unique id of a paused flow execution
	 * continuation that is waiting to resume on the occurrence of an user
	 * event.
	 * @param context the external context representing the state of a request
	 * into Spring Web Flow from an external system.
	 * @return the next view selection, or <code>null</code> if no view
	 * selection was made.
	 * @throws FlowException if an exception occured launching the new flow
	 * execution.
	 */
	public ViewSelection signalEvent(String eventId, String flowExecutionId, ExternalContext context)
			throws FlowException;
	
	/**
	 * Returns the current view selection for the specified conversation, or
	 * <code>null</code> if no such view selection exists.
	 * @param conversationId the id of an existing conversation
	 * @param context the external context representing the state of a request
	 * into Spring Web Flow from an external system.
	 * @return the current view selection
	 * @throws FlowException if an exception occured retrieving the current view
	 * selection
	 */
	public ViewSelection getCurrentViewSelection(String conversationId, ExternalContext context) throws FlowException;

}