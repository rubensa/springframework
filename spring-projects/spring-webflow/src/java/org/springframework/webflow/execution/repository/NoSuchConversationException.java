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

import java.io.Serializable;

/**
 * Thrown when no logical conversation exists with the specified
 * <code>conversationId</code>. This might occur if the conversation ended,
 * expired, or was otherwise invalidated, but a client view still references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchConversationException extends FlowExecutionRepositoryException {

	/**
	 * The unique conversation identifier that was invalid.
	 */
	private Serializable conversationId;

	/**
	 * Create a new flow execution lookup exception.
	 * @param repository the repository
	 * @param conversationId the conversation id
	 */
	public NoSuchConversationException(FlowExecutionRepository repository, Serializable conversationId) {
		super(repository, "No conversation could be found with id '" + conversationId
				+ "' -- perhaps this executing flow has ended or expired? "
				+ "This could happen if your users are relying on browser history "
				+ "(typically via the back button) that reference ended flows.");
		this.conversationId = conversationId;
	}

	/**
	 * Returns the conversation id that was invalid.
	 */
	public Serializable getConversationId() {
		return conversationId;
	}
}
