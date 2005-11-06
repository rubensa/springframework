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
package org.springframework.webflow.execution;

import org.springframework.core.NestedRuntimeException;

/**
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionException extends NestedRuntimeException {

	/**
	 * The execution that could not be stored.
	 */
	private FlowExecution flowExecution;

	/**
	 * Create a new flow execution exception.
	 * @param flowExecution the execution
	 * @param message the message
	 * @param cause the cause
	 */
	public FlowExecutionException(FlowExecution flowExecution, String message) {
		this(flowExecution, message, null);
	}

	/**
	 * Create a new flow execution exception.
	 * @param flowExecutionId the storage id of the flow execution
	 * @param flowExecution the execution
	 * @param message the message
	 * @param cause the cause
	 */
	public FlowExecutionException(FlowExecution flowExecution, String message, Throwable cause) {
		super(message, cause);
		this.flowExecution = flowExecution;
	}

	/**
	 * Returns the flow execution involved. Could be <code>null</code>.
	 * @return the flow execution
	 */
	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}