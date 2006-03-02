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
package org.springframework.webflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A point in a flow where something happens. What happens is determined by a
 * state's type. Standard types of states include action states, view states,
 * subflow states, and end states.
 * <p>
 * Each state is associated with exactly one owning flow definition.
 * Specializations of this class capture all the configuration information
 * needed for a specific kind of state.
 * <p>
 * Subclasses should implement the <code>doEnter</code> method to execute the
 * processing that should occur when this state is entered, acting on its
 * configuration information. The ability to plugin custom state types that
 * execute different behaviour polymorphically is the classic GoF state pattern.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice. It
 * is expected that specializations of this base class be "States" and not part
 * of some other inheritance hierarchy.
 * 
 * @see org.springframework.webflow.TransitionableState
 * @see org.springframework.webflow.ActionState
 * @see org.springframework.webflow.ViewState
 * @see org.springframework.webflow.SubflowState
 * @see org.springframework.webflow.EndState
 * @see org.springframework.webflow.DecisionState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class State extends AnnotatedObject {

	/**
	 * Logger, for use in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The state's owning flow.
	 */
	private Flow flow;

	/**
	 * The state identifier, unique to the owning flow.
	 */
	private String id;

	/**
	 * The list of actions to invoke when this state is entered.
	 */
	private ActionList entryActionList = new ActionList();

	/**
	 * The set of exception handlers for this state.
	 */
	private StateExceptionHandlerSet exceptionHandlerSet = new StateExceptionHandlerSet();

	/**
	 * Default constructor for bean style usage
	 * @see #setFlow(Flow)
	 * @see #setId(String)
	 * @see #addEntryAction(Action)
	 * @see #addExceptionHandler(StateExceptionHandler)
	 */
	protected State() {
	}

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>. The id must be locally unique to the owning
	 * flow. The flow state will be automatically added to the flow.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException if this state cannot be added to the
	 * flow
	 * @see #addEntryAction(Action)
	 * @see #addExceptionHandler(StateExceptionHandler)
	 */
	protected State(Flow flow, String id) throws IllegalArgumentException {
		setId(id);
		setFlow(flow);
	}

	/**
	 * Returns the owning flow.
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Set the owning flow.
	 * @throws IllegalArgumentException if this state cannot be added to the
	 * flow
	 */
	public void setFlow(Flow flow) throws IllegalArgumentException {
		Assert.hasText(getId(), "The id of the state should be set before adding the state to a flow");
		Assert.notNull(flow, "The owning flow is required");
		flow.add(this);
		this.flow = flow;
	}

	/**
	 * Returns the state identifier, unique to the owning flow.
	 * @return the state identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the state identifier, unique to the owning flow.
	 * @param id the state identifier
	 */
	public void setId(String id) {
		Assert.hasText(id, "This state must have a valid identifier");
		Assert.isTrue(getFlow() == null, "You cannot change the id of a state which has already been added to a flow");
		this.id = id;
	}

	/**
	 * Convenience method to add a single action to this state's entry action
	 * list. Entry actions are executed when this state is entered.
	 * @param action the action to add
	 */
	public void addEntryAction(Action action) {
		getEntryActionList().add(action);
	}

	/**
	 * Returns the list of actions executed by this state when it is entered.
	 * @return the state entry action list
	 */
	public ActionList getEntryActionList() {
		return entryActionList;
	}

	/**
	 * Adds a exception handler to this state.
	 * <p>
	 * State exception handlers are invoked when an unhandled
	 * {@link StateException} exception occurs while this state is entered. They
	 * can execute custom exception handling logic as well as select an error
	 * view to display.
	 * @param handler the exception handler
	 */
	public void addExceptionHandler(StateExceptionHandler handler) {
		exceptionHandlerSet.add(handler);
	}

	/**
	 * Returns a mutable set of exception handlers, allowing manipulation of how
	 * exceptions are handled when thrown within this state.
	 * <p>
	 * Exception handlers are invoked when an exception occurs when this state
	 * is entered, and can execute custom exception handling logic as well as
	 * select an error view to display.
	 * @return the state exception handler set
	 */
	public StateExceptionHandlerSet getExceptionHandlerSet() {
		return exceptionHandlerSet;
	}

	/**
	 * Enter this state in the provided flow control context. This
	 * implementation just calls the
	 * {@link #doEnter(FlowExecutionControlContext)} hook method, which should
	 * be implemented by subclasses, after executing the entry action.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state processing
	 * @throws StateException if an exception occurs in this state
	 */
	public ViewSelection enter(FlowExecutionControlContext context) throws StateException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + getId() + "' of flow '" + getFlow().getId() + "'");
		}
		context.setCurrentState(this);
		entryActionList.execute(context);
		return doEnter(context);
	}

	/**
	 * Hook method to execute custom behaviour as a result of entering this
	 * state. By implementing this method subclasses specialize the behaviour of
	 * the state.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state processing
	 * @throws StateException if an exception occurs in this state
	 */
	protected abstract ViewSelection doEnter(FlowExecutionControlContext context) throws StateException;

	/**
	 * Handle an exception that occured in this state during the context of the
	 * current flow execution request.
	 * @param exception the exception that occured
	 * @param context the flow execution control context
	 * @return the selected error view, or <code>null</code> if no handler
	 * matched or returned a non-null view selection
	 * @throws StateException passed in, if it was not handled
	 */
	public ViewSelection handleException(StateException exception, FlowExecutionControlContext context)
			throws StateException {
		return getExceptionHandlerSet().handleException(exception, context);
	}

	public String toString() {
		String flowName = (flow == null ? "<not set>" : flow.getId());
		ToStringCreator creator = new ToStringCreator(this).append("id", getId()).append("flow", flowName).append(
				"entryActionList", entryActionList).append("exceptionHandlerSet", exceptionHandlerSet);
		appendToString(creator);
		return creator.toString();
	}

	/**
	 * Subclasses may override this hook method to stringify their internal
	 * state. This default implementation does nothing.
	 * @param creator the toString creator, to stringify properties
	 */
	protected void appendToString(ToStringCreator creator) {
	}
}