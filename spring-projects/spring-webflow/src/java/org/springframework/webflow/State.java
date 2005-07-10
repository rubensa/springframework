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

import java.util.Map;

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
 * Specializations of this class capture all the configuration information needed for
 * a specific type of state. 
 * <p>
 * Subclasses should implement the <code>doEnter</code> method to execute
 * the processing that should occur when this state is entered, acting on its
 * configuration information. The ability to plugin custom state types that
 * execute different behaviour polymorphically is the classic GoF state pattern.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice.
 * It is expected that specializations of this base class be "States" and not
 * part of some other inheritence hierarchy.
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
	 * Default constructor for bean style usage.
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
	 *         flow
	 */
	protected State(Flow flow, String id) throws IllegalArgumentException {
		setId(id);
		setFlow(flow);
	}

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>. The id must be locally unique to the owning
	 * flow. The flow state will be automatically added to the flow.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException if this state cannot be added to the
	 *         flow
	 */
	protected State(Flow flow, String id, Map properties) throws IllegalArgumentException {
		setId(id);
		setFlow(flow);
		setProperties(properties);
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
	 *         flow
	 */
	public void setFlow(Flow flow) throws IllegalArgumentException {
		Assert.hasText(getId(), "The id of the state should be set before adding the state to a flow");
		Assert.notNull(flow, "The owning flow is required");
		flow.add(this);
		this.flow = flow;
	}

	/**
	 * Returns the state identifier, unique to the owning flow.
	 * @return the state identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the state identifier, unique to the owning flow.
	 * @param id the state identifier.
	 */
	public void setId(String id) {
		Assert.hasText(id, "The state must have a valid identifier");
		Assert.isTrue(getFlow() == null, "You cannot change the id of a state which has been added to a flow");
		this.id = id;
	}

	/**
	 * Checks if this state is transitionable. That is, is this state capable of executing
	 * a transition to another state on the occurence of an event? All
	 * subclasses of <code>TransitionableState</code> are transitionable.
	 * @return true when this state is a <code>TransitionableState</code>, false
	 *         otherwise
	 */
	public boolean isTransitionable() {
		return this instanceof TransitionableState;
	}

	/**
	 * Enter this state in the provided flow execution request context. This implementation
	 * just calls the {@link #doEnter(StateContext)} hook method, which should be
	 * implemented by subclasses.
	 * @param context the request context in an executing flow (a client instance of a flow)
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state processing
	 */
	public final ViewDescriptor enter(StateContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + getId() + "' in flow '" + getFlow().getId() + "'");
		}
		context.setCurrentState(this);
		return doEnter(context);
	}

	/**
	 * Hook method to execute custom behaviour as a result of entering this state.
	 * @param context the state context in an executing flow (a client instance of a flow)
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state processing
	 */
	protected abstract ViewDescriptor doEnter(StateContext context);
	
	public String toString() {
		ToStringCreator creator =
			new ToStringCreator(this).append("id", getId()).append("flow", flow == null ? "" : flow.getId());
		createToString(creator);
		return creator.toString();
	}

	/**
	 * Subclasses may override this hook method to stringify their internal
	 * state. This default implementation does nothing.
	 * @param creator the toString creator, to stringify properties.
	 */
	protected void createToString(ToStringCreator creator) {
	}
}