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
package org.springframework.web.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A simple implementation of the <code>Event</code> base class. Mostly
 * used for signaling events from an internal source within the web flow
 * system, such as an <code>Action</code> or <code>State</code> definition.
 * This is the simplest possible <code>Event</code> implementation.
 * 
 * @see org.springframework.web.flow.Action
 * @see org.springframework.web.flow.State
 * @see org.springframework.web.flow.EndState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SimpleEvent extends Event {

	/**
	 * The event identifier.
	 */
	private String id;

	/**
	 * The event timestamp.
	 */
	private long timestamp = System.currentTimeMillis();

	/**
	 * The state in which this event was signaled (optional).
	 */
	private String stateId;

	/**
	 * Event parameters (optional).
	 */
	private Map parameters;

	/**
	 * Constructor for use in subclasses.
	 * @param source the source of the event
	 */
	public SimpleEvent(Object source) {
		super(source);
	}

	/**
	 * Create a simple event with the specified <code>id</code>.
	 * @param source the source of the event
	 * @param id the event identifier
	 */
	public SimpleEvent(Object source, String id) {
		super(source);
		setRequiredId(id);
	}

	/**
	 * Create a simple event with the specified <code>id</code> and the
	 * provided contextual parameters.
	 * @param source the source of the event
	 * @param id the event identifier
	 * @param parameters the event parameters
	 */
	public SimpleEvent(Object source, String id, Map parameters) {
		super(source);
		setRequiredId(id);
		setParameters(parameters);
	}

	/**
	 * Create a simple event with the specified <code>id</code> occuring in the
	 * state with the specified <code>stateId</code> and the provided
	 * contextual parameters.
	 * @param source the source of the event
	 * @param id the event identifier
	 * @param stateId the state in which this event occured
	 * @param parameters contextual parameters
	 */
	public SimpleEvent(Object source, String id, String stateId, Map parameters) {
		super(source);
		setRequiredId(id);
		setStateId(stateId);
		setParameters(parameters);
	}

	public String getId() {
		return id;
	}

	private void setRequiredId(String id) {
		Assert.hasText(id, "The event id is required for this use - please set to a non-blank string identifier");
		this.id = id;
	}

	/**
	 * Set the event identifier.
	 */
	protected void setId(String id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getStateId() {
		return stateId;
	}

	/**
	 * Set the state identifier.
	 */
	protected void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public Object getParameter(String parameterName) {
		if (parameters != null) {
			return parameters.get(parameterName);
		}
		else {
			return null;
		}
	}

	/**
	 * Set the contextual parameters.
	 */
	protected void setParameters(Map parameters) {
		if (parameters != null) {
			this.parameters = new HashMap(parameters);
		}
	}

	public Map getParameters() {
		if (parameters != null) {
			return Collections.unmodifiableMap(parameters);
		}
		else {
			return null;
		}
	}

	/**
	 * Add given parameters to the set of parameters of this event.
	 */
	protected void addParameters(Map parameters) {
		this.parameters.putAll(parameters);
	}
}