/*
 * Copyright 2002-2004 the original author or authors.
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

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;

/**
 * @author Keith Donald
 */
public class FlowStartStateMarker implements Serializable {
	private static final Log logger = LogFactory.getLog(FlowStartStateMarker.class);

	private TransitionableState state;

	public FlowStartStateMarker(TransitionableState state) {
		Assert.notNull(state, "The start state is required");
		this.state = state;
	}

	protected TransitionableState getState() {
		return state;
	}

	public FlowSessionExecutionStartResult enter(Flow flow, HttpServletRequest request, HttpServletResponse response,
			Map inputAttributes) {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting new session for flow '" + flow.getId() + "' in start state '" + getState()
					+ "' with input attributes '" + DefaultObjectStyler.call(inputAttributes) + "'");
		}
		FlowSessionExecutionStack sessionExecution = createFlowSessionExecutionStack();
		ViewDescriptor startingView = enter(flow, sessionExecution, request, response, inputAttributes);
		return new FlowSessionExecutionStartResult(sessionExecution, startingView);
	}

	public ViewDescriptor enter(Flow flow, FlowSessionExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) {
		sessionExecution.activate(flow.createSession(inputAttributes));
		return getState().enter(sessionExecution, request, response);
	}

	protected FlowSessionExecutionStack createFlowSessionExecutionStack() {
		return new FlowSessionExecutionStack();
	}

}