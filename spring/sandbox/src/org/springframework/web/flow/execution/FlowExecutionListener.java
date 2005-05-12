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
package org.springframework.web.flow.execution;

import java.util.Map;

import org.springframework.web.flow.FlowContext;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;

/**
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of a FlowExecution.
 * <p>
 * An 'observer' that is very aspect like, allowing you to insert 'cross
 * cutting' behavior at well-defined points within a flow execution lifecycle.
 * 
 * @see org.springframework.web.flow.FlowExecutionManager
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

	/**
	 * Called when any client request is submitted to manipulate this
	 * flow execution.
	 * @param context the source of the event, with an 'orignatingEvent'
	 *        property for access to the request event
	 */
	public void requestSubmitted(RequestContext context);

	/**
	 * Called immediately after a start event is signaled - indicating the flow
	 * is starting but hasn't yet entered its start state.
	 * @param context source of the event
	 * @param startState the start state that will be entered
	 * @throws EnterStateVetoException the start state transition was not allowed
	 */
	public void starting(RequestContext context, State startState, Map input) throws EnterStateVetoException;

	/**
	 * Called when a new flow execution was started -- the start state has been
	 * entered.
	 * @param context source of the event
	 */
	public void started(RequestContext context);

	/**
	 * Called when a client request has completed processing.
	 * @param context the source of the event, with an 'orignatingEvent'
	 *        property for access to the request event
	 */
	public void requestProcessed(RequestContext context);

	/**
	 * Called when an event is signaled in a state, but prior to a state
	 * transition.
	 * @param context the source of the event, with a 'lastEvent'
	 *        property for accessing the signaled event
	 */
	public void eventSignaled(RequestContext context);

	/**
	 * Called when a state transitions, after the transition is matched
	 * but before the transition occurs.
	 * @param context the source of the event
	 * @param nextState the proposed state to transition to
	 * @throws EnterStateVetoException the state transition was not allowed
	 */
	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException;

	/**
	 * Called when a state transitions, after the transition occured.
	 * @param context the source of the event
	 * @param previousState <i>from</i> state of the transition
	 * @param state <i>to</i> state of the transition
	 */
	public void stateEntered(RequestContext context, State previousState, State state);

	/**
	 * Called when a flow session is activated.
	 * @param context the source of the event
	 */
	public void resumed(RequestContext context);

	/**
	 * Called when a flow session is paused.
	 * @param context the source of the event
	 */
	public void paused(RequestContext context);

	/**
	 * Called when a flow session ends.
	 * @param context the source of the event
	 * @param endedRootFlowSession ending root flow session
	 */
	public void ended(RequestContext context, FlowSession endedSession);

	/**
	 * Called when an executing flow expires and is cleaned up.
	 * @param information about the expired flow.
	 */
	public void expired(FlowContext flowContext);

}