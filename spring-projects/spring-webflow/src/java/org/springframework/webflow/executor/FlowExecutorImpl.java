/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.executor;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.ConversationLock;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.support.SimpleFlowExecutionRepositoryFactory;

/**
 * The default implementation of the central facade for <i>driving</i> the
 * execution of flows within an application.
 * <p>
 * This object is responsible creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event).
 * <p>
 * This object is a facade or entry point into the Spring Web Flow execution
 * system, and makes the overall system easier to use. The name <i>executor</i>
 * was chosen as <i>executors drive executions</i>.
 * <p>
 * <b>Commonly used configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>repositoryFactory</td>
 * <td>The strategy for accessing a flow execution repositorie that is used to
 * create, save, and store managed flow executions driven by this executor.</td>
 * <td>A {@link SimpleFlowExecutionRepositoryFactory simple}, stateful
 * server-side session-based repository factory</td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.ViewSelection
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutorImpl implements FlowExecutor {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factory, for obtaining repository instances
	 * to save paused executions that require user input and load resuming
	 * executions that will process user events.
	 * <p>
	 * The default value is the {@link SimpleFlowExecutionRepositoryFactory}
	 * repository factory that creates repositories within the user session map.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * Create a new flow executor that uses the repository factory to access a
	 * repository to create, save, and restore managed flow executions driven by
	 * this executor.
	 * @param repositoryFactory the repository factory
	 */
	public FlowExecutorImpl(FlowExecutionRepositoryFactory repositoryFactory) {
		Assert.notNull(repositoryFactory, "The repository factory is required");
		this.repositoryFactory = repositoryFactory;
	}

	public ResponseInstruction launch(String flowId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecution flowExecution = repository.createFlowExecution(flowId);
		ViewSelection selectedView = flowExecution.start(context);
		if (flowExecution.isActive()) {
			FlowExecutionKey flowExecutionKey = repository.generateKey(flowExecution);
			repository.putFlowExecution(flowExecutionKey, flowExecution);
			repository.setCurrentViewSelection(flowExecutionKey.getConversationId(), selectedView);
			return new ResponseInstruction(flowExecutionKey, flowExecution, selectedView);
		}
		else {
			return new ResponseInstruction(flowExecution, selectedView);
		}
	}

	public ResponseInstruction signalEvent(String eventId, FlowExecutionKey flowExecutionKey, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		ConversationLock lock = repository.getLock(flowExecutionKey.getConversationId());
		lock.lock();
		try {
			FlowExecution flowExecution = repository.getFlowExecution(flowExecutionKey);
			ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
			if (flowExecution.isActive()) {
				flowExecutionKey = repository.generateKey(flowExecution, flowExecutionKey.getConversationId());
				repository.putFlowExecution(flowExecutionKey, flowExecution);
				repository.setCurrentViewSelection(flowExecutionKey.getConversationId(), selectedView);
				return new ResponseInstruction(flowExecutionKey, flowExecution, selectedView);
			}
			else {
				repository.invalidateConversation(flowExecutionKey.getConversationId());
				return new ResponseInstruction(flowExecution, selectedView);
			}
		}
		finally {
			lock.unlock();
		}
	}

	public ResponseInstruction getCurrentResponseInstruction(Serializable conversationId, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionKey flowExecutionKey = repository.getCurrentFlowExecutionKey(conversationId);
		FlowExecution flowExecution = repository.getFlowExecution(flowExecutionKey);
		ViewSelection selectedView = repository.getCurrentViewSelection(conversationId);
		return new ResponseInstruction(flowExecutionKey, flowExecution, selectedView);
	}

	/**
	 * Returns the repository retrieved by the configured
	 * {@link FlowExecutionRepositoryFactory}.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}
}