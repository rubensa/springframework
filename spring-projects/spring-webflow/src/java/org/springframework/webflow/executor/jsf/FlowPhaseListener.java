/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.executor.jsf;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.executor.support.FlowExecutorArgumentExtractor;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ConversationRedirect;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowExecutionRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * JSF phase listener that is responsible for managing a {@link FlowExecution}
 * object representing an active user conversation so that other JSF artifacts
 * that execute in different phases of the JSF lifecycle may have access to it.
 * <p>
 * This phase listener implements the following algorithm:
 * <ul>
 * <li>On BEFORE_RESTORE_VIEW, restore the {@link FlowExecution} the user is
 * participating in if a call to
 * {@link FlowExecutorArgumentExtractor#extractFlowExecutionKey(ExternalContext)}
 * returns a submitted flow execution identifier. Place the restored flow
 * execution in a holder that other JSF artifacts such as VariableResolvers,
 * PropertyResolvers, and NavigationHandlers may access during the request
 * lifecycle.
 * <li>On BEFORE_RENDER_RESPONSE, if a flow execution was restored in the
 * RESTORE_VIEW phase generate a new key for identifying the updated execution
 * within a the selected {@link FlowExecutionRepository}. Expose managed flow
 * execution attributes to the views before rendering.
 * <li>On AFTER_RENDER_RESPONSE, if a flow execution was restored in the
 * RESTORE_VIEW phase <em>save</em> the updated execution to the repository
 * using the new key generated in the BEFORE_RENDER_RESPONSE phase.
 * </ul>
 * @author Colin Sampaleanu
 * @author Keith Donald
 */
public class FlowPhaseListener implements PhaseListener {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * A helper for extracting arguments needed by this flow executor.
	 */
	private FlowExecutorArgumentExtractor argumentExtractor = new FlowExecutorArgumentExtractor();

	/**
	 * Returns the repository factory used by this phase listener.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Set the repository factory used by this phase listener.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Returns the argument extractor used by this phase listener.
	 */
	public FlowExecutorArgumentExtractor getArgumentExtractor() {
		return argumentExtractor;
	}

	/**
	 * Sets the parameter extractor to use.
	 */
	public void setArgumentExtractor(FlowExecutorArgumentExtractor argumentExtractor) {
		this.argumentExtractor = argumentExtractor;
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			restoreFlowExecution(event.getFacesContext());
		}
		else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(event.getFacesContext());
			if (holder != null) {
				JsfExternalContext context = new JsfExternalContext(event.getFacesContext());
				setupView(context, holder);
			}
		}
	}

	public void afterPhase(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			FlowExecutionHolder holder = FlowExecutionHolderUtils.getFlowExecutionHolder(event.getFacesContext());
			if (holder != null) {
				JsfExternalContext context = new JsfExternalContext(event.getFacesContext());
				saveFlowExecution(context, holder);
			}
		}
	}

	protected void restoreFlowExecution(FacesContext facesContext) {
		JsfExternalContext context = new JsfExternalContext(facesContext);
		if (argumentExtractor.isFlowExecutionKeyPresent(context)) {
			// restore flow execution from repository so it will be
			// available to variable/property resolvers and the flow
			// navigation handler
			FlowExecutionKey flowExecutionKey = argumentExtractor.extractFlowExecutionKey(context);
			FlowExecutionRepository repository = getRepository(context);
			FlowExecution flowExecution = repository.getFlowExecution(flowExecutionKey);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded existing flow execution from repository with id '" + flowExecutionKey + "'");
			}
			FlowExecutionHolderUtils.setFlowExecutionHolder(new FlowExecutionHolder(flowExecutionKey, flowExecution),
					facesContext);
		}
		else if (argumentExtractor.isConversationIdPresent(context)) {
			Serializable conversationId = argumentExtractor.extractConversationId(context);
			FlowExecutionRepository repository = getRepository(context);
			FlowExecutionKey flowExecutionKey = repository.getCurrentFlowExecutionKey(conversationId);
			FlowExecution flowExecution = repository.getFlowExecution(flowExecutionKey);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded current flow execution from repository with id '" + flowExecutionKey + "'");
			}
			FlowExecutionHolderUtils.setFlowExecutionHolder(new FlowExecutionHolder(flowExecutionKey, flowExecution),
					facesContext);
		}
	}

	/**
	 * Returns the repository instance to be used by this phase listener.
	 */
	protected FlowExecutionRepository getRepository(JsfExternalContext context) {
		if (repositoryFactory == null) {
			repositoryFactory = FlowFacesUtils.getRepositoryFactory(context.getFacesContext());
		}
		return repositoryFactory.getRepository(context);
	}

	protected void setupView(JsfExternalContext context, FlowExecutionHolder holder) {
		generateKey(context, holder);
		ViewSelection selectedView = holder.getViewSelection();
		if (selectedView instanceof ApplicationView) {
			prepareApplicationView(context.getFacesContext(), holder);
		}
		else if (selectedView instanceof FlowExecutionRedirect) {
			saveFlowExecution(context, holder);
			String url = argumentExtractor.createFlowExecutionUrl(holder.getFlowExecutionKey(), holder
					.getFlowExecution(), context);
			sendRedirect(url, context);
		}
		else if (selectedView instanceof ConversationRedirect) {
			saveFlowExecution(context, holder);
			String url = argumentExtractor.createConversationUrl(holder.getFlowExecutionKey(), holder
					.getFlowExecution(), context);
			sendRedirect(url, context);
		}
		else if (selectedView instanceof ExternalRedirect) {
			saveFlowExecution(context, holder);
			String url = argumentExtractor.createExternalUrl((ExternalRedirect)holder.getViewSelection(), holder
					.getFlowExecutionKey(), context);
			sendRedirect(url, context);
		}
		else if (selectedView instanceof FlowRedirect) {
			saveFlowExecution(context, holder);
			String url = argumentExtractor.createFlowUrl((FlowRedirect)holder.getViewSelection(), context);
			sendRedirect(url, context);
		}
	}

	protected void prepareApplicationView(FacesContext facesContext, FlowExecutionHolder holder) {
		Map requestMap = facesContext.getExternalContext().getRequestMap();
		argumentExtractor.put(holder.getFlowExecutionKey(), requestMap);
		argumentExtractor.put(holder.getFlowExecution(), requestMap);
	}

	private void generateKey(JsfExternalContext context, FlowExecutionHolder holder) {
		FlowExecution flowExecution = holder.getFlowExecution();
		if (flowExecution.isActive()) {
			// generate new continuation key for the flow execution
			// before rendering the response
			FlowExecutionKey flowExecutionKey = holder.getFlowExecutionKey();
			FlowExecutionRepository repository = getRepository(context);
			if (flowExecutionKey == null) {
				// it is an new conversation, generate a brand new key
				flowExecutionKey = repository.generateKey(flowExecution);
			}
			else {
				// it is an existing conversaiton, use same conversation id,
				// generate a new continuation id
				flowExecutionKey = repository.generateKey(flowExecution, flowExecutionKey.getConversationId());
			}
			holder.setFlowExecutionKey(flowExecutionKey);
		}
	}

	protected void saveFlowExecution(JsfExternalContext context, FlowExecutionHolder holder) {
		FlowExecution flowExecution = holder.getFlowExecution();
		FlowExecutionRepository repository = getRepository(context);
		if (flowExecution.isActive()) {
			// save the flow execution out to the repository
			if (logger.isDebugEnabled()) {
				logger.debug("Saving continuation to repository with key " + holder.getFlowExecutionKey());
			}
			repository.putFlowExecution(holder.getFlowExecutionKey(), flowExecution);
		}
		else {
			if (holder.getFlowExecutionKey() != null) {
				// remove the conversation from the repository
				Serializable conversationId = holder.getFlowExecutionKey().getConversationId();
				if (logger.isDebugEnabled()) {
					logger.debug("Removing conversation in repository with id '" + conversationId + "'");
				}
				repository.invalidateConversation(conversationId);
			}
		}
	}

	private void sendRedirect(String url, JsfExternalContext context) {
		try {
			context.getFacesContext().getExternalContext().redirect(url);
			context.getFacesContext().responseComplete();
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Could not send redirect to " + url);
		}
	}
}