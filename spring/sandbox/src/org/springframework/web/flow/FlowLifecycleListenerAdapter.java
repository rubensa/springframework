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

public class FlowLifecycleListenerAdapter {

	public void flowStarted(Flow source, FlowSessionExecution sessionExecution, HttpServletRequest request) {
		
	}

	public void flowEventSignaled(Flow source, String eventId, AbstractState state,
			FlowSessionExecution sessionExecution, HttpServletRequest request) {
		
	}

	public void flowStateTransitioned(Flow source, AbstractState oldState, AbstractState newState,
			FlowSessionExecution sessionExecution, HttpServletRequest request) {
		
	}

	public void flowEventProcessed(Flow source, String eventId, AbstractState state,
			FlowSessionExecution sessionExecution, HttpServletRequest request) {
		
	}

	public void flowEnded(Flow source, FlowSession endedFlowSession, FlowSessionExecution sessionExecution,
			HttpServletRequest request) {
		
	}

}