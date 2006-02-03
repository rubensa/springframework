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
package org.springframework.webflow.execution.repository;

/**
 * Thrown if the conversation continuation with the key provided has been
 * invalidated. This might occur if the continuation expired or was otherwise
 * invalidated, but a client view still references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class InvalidConversationContinuationException extends FlowExecutionRepositoryException {

	/**
	 * The conversation continuation key that was invalid.
	 */
	private FlowExecutionContinuationKey continuationKey;

	/**
	 * Creates a new invalid conversation continuation exception.
	 * @param repository the repository
	 * @param key the continuation key
	 */
	public InvalidConversationContinuationException(FlowExecutionRepository repository, FlowExecutionContinuationKey key) {
		super(repository, "The continuation id '" + key.getContinuationId() + "' associated with conversation '"
				+ key.getConversationId()
				+ "' is invalid.  This could happen if your users are relying on browser history "
				+ "(typically via the back button) that reference obsoleted or expired continuations.");
	}

	/**
	 * Creates a new invalid conversation continuation exception.
	 * @param repository the repository
	 * @param key the continuation key
	 * @param message a custom message
	 */
	public InvalidConversationContinuationException(FlowExecutionRepository repository, FlowExecutionContinuationKey key, String message) {
		super(repository, message);
	}

	/**
	 * Returns the invalid continuation key.
	 */
	public FlowExecutionContinuationKey getContinuationKey() {
		return continuationKey;
	}
}