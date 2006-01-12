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
package org.springframework.webflow.manager;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.format.Formatter;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.repository.ExternalMapFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKeyFormatter;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * A central facade for the execution of flows within an application. This
 * object is responsible for creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event). This object is a facade or entry point into the flow execution
 * subsystem, and makes the overall subsystem easier to use.
 * <p>
 * The {@link #handleFlowRequest(ExternalContext)} method is the central facade
 * operation and implements the following algorithm:
 * <ol>
 * <li>Search for a flow execution id in the external context (in a request
 * parameter named {@link #getFlowExecutionIdParameterName()).</li>
 * <li>If no flow execution id was submitted, create a new flow execution. The
 * top-level flow definition for which an execution is created for is determined
 * by the value of the {@link #getFlowIdParameterName()} request parameter. If
 * this parameter parameter is not present, an exception is thrown.</li>
 * <li>If a flow execution id <em>was</em> submitted, load the previously
 * saved FlowExecution with that id from a repository ({@link #getRepository(ExternalContext)}).</li>
 * <li>If a new flow execution was created in the previous steps, start that
 * execution.</li>
 * <li>If an existing flow execution was loaded from a repository, extract the
 * value of the event id ({@link #getEventIdParameterName()). Signal the occurence of the user event, resuming the flow
 * execution in the current state.</li>
 * <li>If the flow execution is still active after event processing, save it
 * out to the repository. This process generates a unique flow execution id that
 * will be exposed to the caller for identifying the same FlowExecution
 * (conversation) on subsequent requests. The caller will also be given access
 * to the flow execution context and any data placed in request or flow scope.</li>
 * </ol>
 * <p>
 * By default, this class will use the flow execution implementation provided by
 * the <code>FlowExecutionImpl</code> class. If you would like to use a
 * different implementation, override the {@link #createFlowExecution(Flow)}
 * method in a subclass.
 * 
 * <p>
 * <b>Typical FlowExecutionManager configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowLocator (required)</td>
 * <td>None</td>
 * <td>The locator that will load Flow definitions as needed for execution by
 * this manager</td>
 * </tr>
 * <tr>
 * <td>repositoryFactory</td>
 * <td>A server-side, stateful-session-based repository factory</td>
 * <td>The strategy for accessing managed flow execution repositories</td>
 * </tr>
 * <tr>
 * <td>listeners</td>
 * <td>None</td>
 * <td>The listeners to observe the lifecycle of managed flow executions</td>
 * </tr>
 * </table>
 * </p>
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory
 * @see org.springframework.webflow.execution.FlowExecutionListener
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutionManagerImpl implements FlowExecutionManager {

	/**
	 * The flow context itself will be exposed to the view in a model attribute
	 * with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client. Defaults to a bean factory based lookup
	 * strategy.
	 */
	private FlowLocator flowLocator;

	/**
	 * The flow execution repository factoring, for obtaining repository
	 * instances to save paused executions that require user input and load
	 * resuming executions that will process user events.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory = new ExternalMapFlowExecutionRepositoryFactory();

	/**
	 * The formatter that will parse encoded _flowExecutionId strings into
	 * {@link FlowExecutionContinuationKey} objects.
	 */
	private Formatter continuationKeyFormatter = new FlowExecutionContinuationKeyFormatter();

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private FlowExecutionListenerLoader listenerLoader = new EmptyFlowExecutionListenerLoader();

	/**
	 * Create a new flow execution manager using the specified flow locator for
	 * loading Flow definitions.
	 * @param flowLocator the flow locator to use
	 * 
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setRepositoryFactory(FlowExecutionRepositoryFactory)
	 * @see #setListenerLoader(FlowExecutionListenerLoader)
	 */
	public FlowExecutionManagerImpl(FlowLocator flowLocator) {
		setFlowLocator(flowLocator);
	}

	/**
	 * Returns the flow locator to use for lookup of flow definitions to execute.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flow definitions to execute.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the repository factory used by this flow execution manager.
	 */
	public FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Set the repository factory used by this flow execution manager.
	 */
	public void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryLocator) {
		this.repositoryFactory = repositoryLocator;
	}

	/**
	 * Returns the repository instance to be used by this flow execution manager.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}

	/**
	 * Returns the listener loader instance to be used by this flow execution
	 * manager.
	 */
	public FlowExecutionListenerLoader getListenerLoader() {
		return listenerLoader;
	}

	/**
	 * Sets the listener loader instance to be used by this flow execution
	 * manager.
	 */
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		this.listenerLoader = listenerLoader;
	}

	/**
	 * Returns the continuation key formatting strategy.
	 */
	public Formatter getContinuationKeyFormatter() {
		return continuationKeyFormatter;
	}

	/**
	 * Sets the flow execution continuation key formatting strategy.
	 * @param continuationKeyFormatter the continuation key formatter
	 */
	public void setContinuationKeyFormatter(Formatter continuationKeyFormatter) {
		this.continuationKeyFormatter = continuationKeyFormatter;
	}

	public ViewSelection launch(String flowId, ExternalContext context) throws FlowException {
		Flow flow = getFlowLocator().getFlow(flowId);
		FlowExecution flowExecution = createFlowExecution(flow);
		ViewSelection selectedView = flowExecution.start(context);
		if (flowExecution.isActive()) {
			FlowExecutionRepository repository = getRepository(context);
			FlowExecutionContinuationKey continuationKey = repository.generateContinuationKey(flowExecution);
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, continuationKey, flowExecution);
		}
		else {
			return selectedView;
		}
	}

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine
	 * this if they wish to use a specialized FlowExecution implementation
	 * class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionImpl(flow, listenerLoader.getListeners(flow));
		if (logger.isDebugEnabled()) {
			logger.debug("Created a new flow execution for flow definition '" + flow.getId() + "'");
		}
		return flowExecution;
	}

	public ViewSelection signalEvent(String eventId, String flowExecutionId, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionContinuationKey continuationKey = parseContinuationKey(flowExecutionId);
		FlowExecution flowExecution = loadFlowExecution(repository, continuationKey);
		ViewSelection selectedView = flowExecution.signalEvent(eventId, context);
		if (flowExecution.isActive()) {
			continuationKey = repository.generateContinuationKey(flowExecution, continuationKey.getConversationId());
			repository.putFlowExecution(continuationKey, flowExecution);
			return prepareSelectedView(selectedView, continuationKey, flowExecution);
		}
		else {
			repository.invalidateConversation(continuationKey.getConversationId());
			return selectedView;
		}
	}

	/**
	 * Load an existing FlowExecution based on data in the specified source
	 * event.
	 * @param continuationKey the unique id of the flow execution
	 * @param context the context in which the external user event occured
	 */
	public FlowExecution loadFlowExecution(FlowExecutionRepository repository,
			FlowExecutionContinuationKey continuationKey) {
		// client is participating in an existing flow execution, retrieve
		// information about it
		FlowExecution flowExecution = repository.getFlowExecution(continuationKey);
		// rehydrate the execution if neccessary (if it had been serialized out)
		flowExecution.rehydrate(getFlowLocator(), getListenerLoader());
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded existing flow execution from repository with id '" + continuationKey + "'");
		}
		return flowExecution;
	}

	/**
	 * Helper to parse a flow execution continuation key from its
	 * string-encoding.
	 * @param flowExecutionId the string encoded key
	 * @return the parsed key
	 */
	protected FlowExecutionContinuationKey parseContinuationKey(String flowExecutionId) {
		return (FlowExecutionContinuationKey)continuationKeyFormatter.parseValue(flowExecutionId,
				FlowExecutionContinuationKey.class);
	}

	/**
	 * Perform any processing necessary before the view selection made is
	 * returned to the client of the flow execution manager and rendered out.
	 * This implementation adds a number of <i>infrastructure attributes</i> to
	 * the model that will be exposed to the view so clients may record
	 * information about the flow to support participation in the flow on a
	 * subsequent request. More specifically, this method will add the
	 * {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE},
	 * {@link #FLOW_EXECUTION_ID_ATTRIBUTE}.
	 * @param selectedView the view selection to be prepared
	 * @param continuationKey the assigned repository continuation key
	 * @param continuationKey the unique id of the flow execution
	 * @param flowExecutionContext the flow context providing info about the
	 * flow execution
	 * @return the prepped view selection
	 */
	protected ViewSelection prepareSelectedView(ViewSelection selectedView,
			FlowExecutionContinuationKey continuationKey, FlowExecutionContext flowExecutionContext) {
		if (flowExecutionContext.isActive() && selectedView != null) {
			String id = formatContinuationKey(continuationKey);
			if (selectedView.isRedirect()) {
				throw new UnsupportedOperationException("Redirect from a view state is not yet supported");
			}
			else {
				exposeFlowExecutionAttributes(selectedView.getModel(), id, flowExecutionContext);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view to client " + selectedView);
		}
		return selectedView;
	}

	/**
	 * Convert the continuation key to encoded string form.
	 * @param key the continuation key
	 * @return the string-encoded key
	 */
	protected String formatContinuationKey(FlowExecutionContinuationKey key) {
		return continuationKeyFormatter.formatValue(key);
	}

	/**
	 * Stores contextual info in the view model, including the flow execution
	 * context, flow execution id
	 * @param model the view model
	 * @param flowExecutionId the flow execution id
	 * @param flowExecutionContext the flow execution context
	 */
	protected void exposeFlowExecutionAttributes(Map model, String flowExecutionId,
			FlowExecutionContext flowExecutionContext) {
		// make the entire flow execution context available in the model
		model.put(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, flowExecutionContext);
		// make the unique flow execution id and current state id
		// available in the model as convenience to views
		model.put(FLOW_EXECUTION_ID_ATTRIBUTE, flowExecutionId);
	}
}