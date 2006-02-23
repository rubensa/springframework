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

import java.util.Iterator;

import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;

/**
 * A transitionable state that executes one or more actions when entered. When
 * the action(s) are executed, this state responds to their result(s) to decide
 * what state to transition to next.
 * <p>
 * If more than one action is configured, they are executed in an ordered chain
 * until one returns a result event that matches a valid state transition out of
 * this state. This is a form of the Chain of Responsibility (CoR) pattern.
 * <p>
 * The result of an action's execution is typically treated as the contributing
 * criteria for a state transition. In addition, any state accessible via the
 * <code>RequestContext</code> may be tested as part of custom transitional
 * criteria, allowing for sophisticated transition expressions that reason on
 * contextual state.
 * <p>
 * Each action executed by this action state may be provisioned with a set of
 * arbitrary execution properties. These properties are made available to the
 * action at execution time and may be used to influence action execution
 * behavior.
 * <p>
 * Common action execution properties include:
 * <p>
 * <table border="1">
 * <th>Property</th>
 * <th>Description</th>
 * <tr>
 * <td valign="top">name</td>
 * <td>The 'name' property is used as a qualifier for an action's result event,
 * and is typically used to allow the flow to respond to a specific action's
 * outcome within a larger action execution chain. For example, if an action
 * named <code>myAction</code> returns a <code>success</code> result, a
 * transition that matches on event <code>myAction.success</code> will be
 * searched, and if found, executed. If this action is not assigned a name, a
 * transition for the base <code>success</code> event will be searched and if
 * found, executed. <br>
 * This is useful in situations where you want to execute actions in an ordered
 * chain as part of one action state, and wish to transition on the result of
 * the last one in the chain. For example:
 * 
 * <pre>
 *       &lt;action-state id=&quot;setupForm&quot;&gt; 
 *           &lt;action name=&quot;setup&quot; bean=&quot;myAction&quot; method=&quot;setupForm&quot;/&gt; 
 *           &lt;action name=&quot;referenceData&quot; bean=&quot;myAction&quot; method=&quot;setupReferenceData&quot;/&gt; 
 *           &lt;transition on=&quot;referenceData.success&quot; to=&quot;displayForm&quot;/&gt; 
 *       &lt;/action-state&gt;
 * </pre>
 * 
 * When the 'setupForm' state above is entered, the 'setup' action will execute,
 * followed by the 'referenceData' action. After 'referenceData' execution, the
 * flow will then respond to the 'referenceData.success' event by transitioning
 * to the 'displayForm' state. </td>
 * <tr>
 * <td valign="top">method</td>
 * <td>The 'method' property is the name of a specific method on a
 * <code>{@link org.springframework.webflow.action.MultiAction}</code> to
 * execute, or the name of a specific method on a arbitrary POJO (plain old
 * java.lang.Object). In the MultiAction scenario, the named method must have
 * the signature <code>public Event ${method}(RequestContext)</code>. As an
 * example of this scenario, a method property with value <code>setupForm</code>
 * would bind to a method on a MultiAction instance with the signature:
 * <code>public Event setupForm(RequestContext context)</code>. <br>
 * <br>
 * As an alternative to a MultiAction method binding, the 'method' property may
 * instead be the name of an method of an arbitrary POJO (plain old
 * java.lang.Object). In this case the named method must be public and may have
 * any signature. If the method signature does accept parameters, those
 * parameters may be specified by using the format:
 * <code>methodName(${param1}, ${param2}, ...)</code>. Parameter
 * ${expressions} are evaluated against the current <code>RequestContext</code>,
 * allowing for data stored in flow scope or request scope to be passed as
 * arguments to the POJO in an automatic fashion.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.action.MultiAction
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionState extends TransitionableState {

	/**
	 * The list of actions to be executed when this action state is entered.
	 */
	private ActionList actionList = new ActionList();

	/**
	 * Default constructor for bean style usage.
	 * @see TransitionableState#TransitionableState()
	 * @see #addAction(Action)
	 */
	public ActionState() {
	}

	/**
	 * Creates a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 * @see TransitionableState#TransitionableState(Flow, String)
	 * @see #addAction(Action)
	 */
	public ActionState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Convenience method to add a single action to this state's executable
	 * action list.
	 * @param action the action to add
	 */
	public void addAction(Action action) {
		getActionList().add(action);
	}

	/**
	 * Returns the list of actions executable by this action state.
	 * @return the state action list
	 */
	public ActionList getActionList() {
		return actionList;
	}

	/*
	 * Overrides getRequiredTransition(RequestContext) to throw a local
	 * NoMatchingActionResultTransitionException if a transition on the
	 * occurence of an action result event cannot be matched. Used to facilitate
	 * an action invocation chain.
	 * @see org.springframework.webflow.TransitionableState#getRequiredTransition(org.springframework.webflow.RequestContext)
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransitionSet().getTransition(context);
		if (transition == null) {
			throw new NoMatchingActionResultTransitionException(this, context.getLastEvent());
		}
		return transition;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * This implementation iterates over each configured <code>Action</code>
	 * instance and executes it. Execution continues until an
	 * <code>Action</code> returns a result event that matches a state
	 * transition in this request context, or the set of all actions is
	 * exhausted.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection signaling that control should be returned to the
	 * client and a view rendered
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		int executionCount = 0;
		String[] eventIds = new String[actionList.size()];
		Iterator it = actionList.iterator();
		while (it.hasNext()) {
			Action action = (Action)it.next();
			Event event = ActionExecutor.execute(action, context);
			if (event != null) {
				eventIds[executionCount] = event.getId();
				try {
					return context.signalEvent(event);
				}
				catch (NoMatchingActionResultTransitionException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Action execution ["
								+ (executionCount + 1)
								+ "] resulted in no matching transition on event '"
								+ event.getId()
								+ "'"
								+ (it.hasNext() ? ": proceeding to the next action in the list"
										: ": action list exhausted"));
					}
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger
							.debug("Action execution ["
									+ (executionCount + 1)
									+ "] returned a [null] event"
									+ (it.hasNext() ? ": proceeding to the next action in the list"
											: ": action list exhausted"));
				}
				eventIds[executionCount] = null;
			}
			executionCount++;
		}
		if (executionCount > 0) {
			throw new NoMatchingTransitionException(this, context.getLastEvent(),
					"No transition was matched on the event(s) signaled by the [" + executionCount
							+ "] action(s) that executed in this action state '" + getId() + "' of flow '"
							+ getFlow().getId() + "'; transitions must be defined to handle action result outcomes -- "
							+ "possible flow configuration error? Note: the eventIds signaled were: '"
							+ StylerUtils.style(eventIds)
							+ "', while the supported set of transitional criteria for this action state is '"
							+ StylerUtils.style(getTransitionSet().getTransitionCriterias()) + "'");
		}
		else {
			throw new IllegalStateException(
					"No actions were executed, thus I cannot execute any state transition "
							+ "-- programmer configuration error; make sure you add at least one action to this state's action list");
		}
	}

	protected void appendToString(ToStringCreator creator) {
		creator.append("actionList", actionList);
		super.appendToString(creator);
	}

	/**
	 * Local "no transition found" exception used to report that an action
	 * result could not be mapped to a state transition.
	 * 
	 * @author Keith Donald
	 */
	private static class NoMatchingActionResultTransitionException extends NoMatchingTransitionException {

		/**
		 * Creates a new exception.
		 * @param state the action state
		 * @param resultEvent the action result event
		 */
		public NoMatchingActionResultTransitionException(ActionState state, Event resultEvent) {
			super(state, resultEvent);
		}
	}
}