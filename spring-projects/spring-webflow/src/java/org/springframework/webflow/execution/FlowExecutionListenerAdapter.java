/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.execution;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewSelection;

/**
 * An abstract adapter class for listeners (observers) of flow execution
 * lifecycle events. The methods in this class are empty. This class exists as
 * convenience for creating listener objects; subclass it and override what you
 * need.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public abstract class FlowExecutionListenerAdapter implements FlowExecutionListener {

	public void requestSubmitted(RequestContext context) {
	}

	public void requestProcessed(RequestContext context) {
	}

	public void sessionStarting(RequestContext context, Flow flow, AttributeMap input) {
	}

	public void sessionStarted(RequestContext context, FlowSession session) {
	}

	public void eventSignaled(RequestContext context, Event event) {
	}

	public void stateEntering(RequestContext context, State state) throws EnterStateVetoException {
	}

	public void stateEntered(RequestContext context, State previousState, State newState) {
	}

	public void resumed(RequestContext context) {
	}

	public void paused(RequestContext context, ViewSelection selectedView) {
	}

	public void sessionEnding(RequestContext context, FlowSession session, AttributeMap output) {
	}

	public void sessionEnded(RequestContext context, FlowSession session, UnmodifiableAttributeMap output) {
	}
}