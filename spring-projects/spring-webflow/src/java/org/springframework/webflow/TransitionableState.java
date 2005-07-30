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
import java.util.Map;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.core.style.ToStringCreator;

/**
 * Abstract superclass for states that have one or more transitions. State
 * transitions are typically triggered by events.
 * 
 * @see org.springframework.webflow.Transition
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class TransitionableState extends State {

	/**
	 * The set of possible transitions out of this state.
	 */
	private Set transitions = CollectionFactory.createLinkedSetIfPossible(6);
	
	/**
	 * An action to execute when exiting this state. 
	 */
	private Action exitAction;
	
	/**
	 * Default constructor for bean style usage.
	 */
	protected TransitionableState() {
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	protected TransitionableState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	protected TransitionableState(Flow flow, String id, Transition transition, Map properties)
			throws IllegalArgumentException {
		super(flow, id, properties);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	protected TransitionableState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id);
		addAll(transitions);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	protected TransitionableState(Flow flow, String id, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, properties);
		addAll(transitions);
	}

	/**
	 * Returns the exit action for this state (may be null).
	 * @return the exit action
	 */
	public Action getExitAction() {
		return exitAction;
	}

	/**
	 * Sets the exit action for this state.
	 * @param exitAction the exit action (may be null)
	 */
	public void setExitAction(Action exitAction) {
		this.exitAction = exitAction;
	}
	
	/**
	 * Add a transition to this state.
	 * @param transition the transition to add
	 */
	public void add(Transition transition) {
		transition.setSourceState(this);
		this.transitions.add(transition);
	}

	/**
	 * Add given list of transitions to this state.
	 * @param transitions the transitions to add
	 */
	public void addAll(Transition[] transitions) {
		for (int i = 0; i < transitions.length; i++) {
			add(transitions[i]);
		}
	}

	/**
	 * Returns an iterator looping over all transitions in this state.
	 */
	public Iterator transitionsIterator() {
		return transitions.iterator();
	}

	/**
	 * Returns the list of transitions owned by this state.
	 */
	public Transition[] getTransitions() {
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Returns a list of the supported transitional criteria used to match
	 * transitions in this state.
	 * @return the list of transitional criteria
	 */
	public TransitionCriteria[] getTransitionCriterias() {
		TransitionCriteria[] res = new TransitionCriteria[transitions.size()];
		Iterator it = transitionsIterator();
		int i = 0;
		while (it.hasNext()) {
			res[i++] = ((Transition)it.next()).getMatchingCriteria();
		}
		return res;
	}

	/**
	 * Returns whether or not this state has a transition that will fire for
	 * given flow execution request context.
	 * @param context a flow execution context
	 */
	public boolean hasTransitionFor(RequestContext context) {
		return getTransition(context) != null;
	}

	/**
	 * Gets a transition for given flow execution request context.
	 * @param context a flow execution context
	 * @return the transition, or null if not found
	 */
	public Transition getTransition(RequestContext context) {
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.matches(context)) {
				return transition;
			}
		}
		return null;
	}

	/**
	 * Get a transition in this state for given flow execution request context.
	 * Throws and exception when there is no corresponding transition.
	 * @throws NoMatchingTransitionException when the transition cannot be found
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransition(context);
		if (transition == null) {
			throw new NoMatchingTransitionException(this, context);
		}
		return transition;
	}

	/**
	 * Notify this state that the specified Event was signaled within it. By
	 * default, receipt of the event will trigger a search for a matching state
	 * transition. If a valid transition is matched, its execution will be
	 * requested. If a transition could not be matched, or the transition
	 * execution failed, an exception will be thrown.
	 * @param event the event that occured
	 * @param context the context associated with this request
	 * @return the view descriptor
	 * @throws NoMatchingTransitionException when no matching transition can be found
	 * @throws CannotExecuteTransitionException when a transition could
	 *         not be executed on receipt of the event
	 */
	public ViewDescriptor onEvent(Event event, StateContext context)
			throws NoMatchingTransitionException, CannotExecuteTransitionException {
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + event.getId() + "' signaled in context: " + context);
		}
		context.setLastEvent(event);
		Transition transition = getRequiredTransition(context);
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + event.getId() + "' matched transition to state: '" + transition.getTargetStateId() + "'");
		}
		return transition.execute(context);
	}

	/**
	 * Re-enter this state. This is typically called when a transition out
	 * of this state is selected, but transition execution rolls back and
	 * as a result the flow reenters the source state.
	 * <p>
	 * By default, this just calls <code>enter()</code>.
	 * @param context the request context in an executing flow (a client instance of a flow)
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state processing
	 */
	public ViewDescriptor reenter(StateContext context) {
		// just re-enter this state
		return enter(context);
	}

	/**
	 * Exit this state. This is typically called when a transition takes the flow
	 * out of this state into another state. By default just executes the exit action, if
	 * one is registered.
	 * @param context the flow execution request context
	 */
	public void exit(StateContext context) {
		if (this.exitAction != null) {
			new ActionExecutor(exitAction).execute(context);
		}
	}
	
	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions).append("exitAction", exitAction);
	}

}