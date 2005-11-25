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
package org.springframework.webflow.registry;

import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;

/**
 * Thrown when no flow definition was found during a lookup operation.
 * @author Keith Donald
 */
public class NoSuchFlowDefinitionException extends FlowArtifactLookupException {

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id.
	 */
	public NoSuchFlowDefinitionException(String flowId) {
		this(flowId, null);
	}

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id.
	 * @param cause the root cause
	 */
	public NoSuchFlowDefinitionException(String flowId, Throwable cause) {
		super(Flow.class, flowId, "No such flow definition with id '" + flowId + "' found", cause);
	}

	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow Id
	 * @param message a custom message
	 * @param cause the root cause
	 */
	public NoSuchFlowDefinitionException(String flowId, String message, Throwable cause) {
		super(Flow.class, flowId, message, cause);
	}

}