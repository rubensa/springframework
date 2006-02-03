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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * When executed, a transition takes a flow execution from its current state,
 * called the <i>source state</i>, to another state, called the </i>target
 * state</i>. A transition may become eligible for execution on the occurence
 * of {@link Event} from within a transitionable source state.
 * <p>
 * When an event occurs within this transition's source
 * <code>TransitionableState</code>, the determination of the eligibility of
 * this transition is made by a <code>TransitionCriteria</code> object called
 * the <i>matching criteria</i>. If the matching criteria returns
 * <code>true</code>, this transition is marked eligible for execution for
 * that event.
 * <p>
 * Determination as to whether an eligible transition should be allowed to
 * execute is made by a <code>TransitionCriteria</code> object called the
 * <i>execution criteria</i>. If the execution criteria test fails, this
 * transition will <i>roll back</i> and reenter its source state. If the
 * execution criteria test succeeds, this transition will execute and take the
 * flow to the transition's target state.
 * <p>
 * The target state of this transition is typically specified at configuration
 * time using the target state id. If the target state of this transition needs
 * to be calculated in a dynamic fashion at runtime, set a custom
 * {@link TransitionTargetStateResolver}
 * 
 * @see TransitionableState
 * @see TransitionCriteria
 * @see TransitionTargetStateResolver
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition extends AnnotatedObject {

	/**
	 * Logger, for use in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(Transition.class);

	/**
	 * The criteria that determine whether or not this transition matches as
	 * eligible for execution when an event occurs in the sourceState.
	 */
	private TransitionCriteria matchingCriteria = WildcardTransitionCriteria.INSTANCE;

	/**
	 * The criteria that determine whether or not this transition, once matched,
	 * should complete execution or should <i>roll back</i>.
	 */
	private TransitionCriteria executionCriteria = WildcardTransitionCriteria.INSTANCE;

	/**
	 * The resolver responsible for calculating the target state of this
	 * transition.
	 */
	private TransitionTargetStateResolver targetStateResolver;

	/**
	 * Default constructor for bean style usage.
	 * @see #setSourceState(TransitionableState)
	 * @see #setMatchingCriteria(TransitionCriteria)
	 * @see #setExecutionCriteria(TransitionCriteria)
	 * @see #setTargetStateResolver(TransitionTargetStateResolver)
	 */
	public Transition() {
	}

	/**
	 * Create a new transition that always matches and always executes,
	 * transitioning to the target state calculated by the provided
	 * targetStateResolver.
	 * @param targetStateResolver the resolver of the target state of this
	 * transition
	 * @see #setSourceState(TransitionableState)
	 * @see #setMatchingCriteria(TransitionCriteria)
	 * @see #setExecutionCriteria(TransitionCriteria)
	 */
	public Transition(TransitionTargetStateResolver targetStateResolver) {
		setTargetStateResolver(targetStateResolver);
	}

	/**
	 * Create a new transition that matches on the specified criteria,
	 * transitioning to the target state calculated by the provided
	 * targetStateResolver.
	 * @param targetStateResolver the resolver of the target state of this
	 * transition
	 * @see #setSourceState(TransitionableState)
	 * @see #setExecutionCriteria(TransitionCriteria)
	 */
	public Transition(TransitionCriteria matchingCriteria, TransitionTargetStateResolver targetStateResolver) {
		setMatchingCriteria(matchingCriteria);
		setTargetStateResolver(targetStateResolver);
	}

	/**
	 * Returns the criteria that determine whether or not this transition
	 * matches as eligible for execution.
	 * @return the transition matching criteria
	 */
	public TransitionCriteria getMatchingCriteria() {
		return matchingCriteria;
	}

	/**
	 * Set the criteria that determine whether or not this transition matches as
	 * eligible for execution.
	 * @param matchingCriteria the transition matching criteria
	 */
	public void setMatchingCriteria(TransitionCriteria matchingCriteria) {
		Assert.notNull(matchingCriteria, "The transition matching criteria is required");
		this.matchingCriteria = matchingCriteria;
	}

	/**
	 * Returns the criteria that determine whether or not this transition, once
	 * matched, should complete execution or should <i>roll back</i>.
	 * @return the transition execution criteria
	 */
	public TransitionCriteria getExecutionCriteria() {
		return executionCriteria;
	}

	/**
	 * Set the criteria that determine whether or not this transition, once
	 * matched, should complete execution or should <i>roll back</i>.
	 * @param executionCriteria the transition execution criteria
	 */
	public void setExecutionCriteria(TransitionCriteria executionCriteria) {
		this.executionCriteria = executionCriteria;
	}

	/**
	 * Returns this transition's target state resolver.
	 */
	public TransitionTargetStateResolver getTargetStateResolver() {
		return targetStateResolver;
	}

	/**
	 * Set this transition's target state resolver, to calculate what state to
	 * transition to when this transition is executed.
	 * @param targetStateResolver the target state resolver
	 */
	public void setTargetStateResolver(TransitionTargetStateResolver targetStateResolver) {
		this.targetStateResolver = targetStateResolver;
	}

	/**
	 * Checks if this transition is elligible for execution given the state of
	 * the provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean matches(RequestContext context) {
		return getMatchingCriteria().test(context);
	}

	/**
	 * Checks if this transition can complete its execution or should be rolled
	 * back, given the state of the flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition can complete execution, false if it
	 * should roll back
	 */
	public boolean canExecute(RequestContext context) {
		return getExecutionCriteria() != null ? getExecutionCriteria().test(context) : true;
	}

	/**
	 * Returns the target state of this transition, possibly taking the request
	 * context into account.
	 * @param context the flow execution request context
	 */
	protected State getTargetState(TransitionableState sourceState, RequestContext context) {
		return getTargetStateResolver().resolveTargetState(this, sourceState, context);
	}

	/**
	 * Execute this state transition. Will only be called if the
	 * {@link #matches(RequestContext)} method returns true for given context.
	 * @param context the flow execution control context
	 * @return a view selection containing model and view information needed to
	 * render the results of the transition execution
	 * @throws StateException when transition execution fails
	 */
	public ViewSelection execute(TransitionableState sourceState, FlowExecutionControlContext context)
			throws StateException {
		ViewSelection selectedView;
		if (canExecute(context)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing " + this + " out of state '" + sourceState.getId() + "'");
			}
			sourceState.exit(context);
			State targetState = getTargetState(sourceState, context);
			context.setLastTransition(this);
			// enter the target state (note: any exceptions are propagated)
			selectedView = targetState.enter(context);
		}
		else {
			// 'roll back' and re-enter the source state
			selectedView = sourceState.reenter(context);
		}
		if (logger.isDebugEnabled()) {
			if (context.getFlowExecutionContext().isActive()) {
				logger.debug("Completed execution of " + this + ", as a result the new state is '"
						+ context.getFlowExecutionContext().getCurrentState().getId() + "' in flow '"
						+ context.getFlowExecutionContext().getActiveFlow().getId() + "'");
			}
			else {
				logger.debug("Completed execution of " + this + ", as a result the flow execution has ended");
			}
		}
		return selectedView;
	}

	public String toString() {
		return new ToStringCreator(this).append("matchingCriteria", getMatchingCriteria()).append("executionCriteria",
				getExecutionCriteria()).append("targetStateResolver", getTargetStateResolver()).append("properties",
				getProperties()).toString();
	}
}