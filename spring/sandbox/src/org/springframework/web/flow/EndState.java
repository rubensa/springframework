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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Terminates a active web flow session when entered.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public class EndState extends AbstractState {

	private String viewName;

	public EndState(Flow flow, String id) {
		super(flow, id);
	}

	public EndState(Flow flow, String id, String viewName) {
		super(flow, id);
		setViewName(viewName);
	}

	protected void setViewName(String viewName) {
		this.viewName = viewName;
	}

	protected String getViewName() {
		return viewName;
	}

	public boolean isMarker() {
		return !StringUtils.hasText(viewName);
	}

	public boolean isEndState() {
		return true;
	}

	protected ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		FlowSession endingFlowSession = flowExecution.endActiveSession();
		if (logger.isDebugEnabled()) {
			logger.debug("Session for flow '" + getFlow().getId() + "' ended, session details = " + endingFlowSession);
		}
		if (flowExecution.isActive()) {
			// session execution is still active, resume in parent
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming parent flow '" + flowExecution.getActiveFlowId() + "' in state '"
						+ flowExecution.getCurrentStateId() + "'");
			}
			Assert.isInstanceOf(FlowAttributesMapper.class, flowExecution.getCurrentState());
			FlowAttributesMapper resumingState = (FlowAttributesMapper)flowExecution.getCurrentState();
			resumingState.mapToResumingParentFlow(endingFlowSession, flowExecution.getActiveFlowSession());
			// treat this end state id as a transitional event in the
			// resuming state, this is so cool!
			return ((TransitionableState)resumingState).signalEvent(getId(), flowExecution, request, response);
		}
		else {
			// entire flow execution has ended, return ending view if applicable
			if (logger.isDebugEnabled()) {
				logger.debug("Session execution for root flow '" + getFlow().getId() + "' has ended");
			}
			if (isMarker()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning a view descriptor null object; no view to render");
				}
				return null;
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning view name '" + viewName + "' to render");
				}
				return new ModelAndView(viewName, endingFlowSession.getAttributes());
			}
		}
	}
}