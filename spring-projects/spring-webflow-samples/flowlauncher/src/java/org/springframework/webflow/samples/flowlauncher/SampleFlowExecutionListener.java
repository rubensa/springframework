/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.samples.flowlauncher;

import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.execution.EnterStateVetoException;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;

public class SampleFlowExecutionListener extends FlowExecutionListenerAdapter {
	
	public static final String INPUT_ATTRIBUTE = "input";
	
	public void sessionStarting(RequestContext context, State startState, Map input) throws EnterStateVetoException {
		/*
		 * Each time a flow is starting, check if there is input data in the
		 * request and if so, put it in flow scope.
		 * You could also do this in a "captureInput" action, but using a flow execution
		 * listener is more flexible.
		 */
		addInput(context.getSourceEvent(), input);
	}
	
	public void resumed(RequestContext context) {
		// the flow is up & running, map input in the request into it
		addInput(context.getSourceEvent(), context.getFlowScope());
	}
	
	private void addInput(Event sourceEvent, Map targetMap) {
		String inputParam = (String)sourceEvent.getParameter(INPUT_ATTRIBUTE);
		if (StringUtils.hasText(inputParam)) {
			targetMap.put(INPUT_ATTRIBUTE, inputParam);
		}		
	}
}