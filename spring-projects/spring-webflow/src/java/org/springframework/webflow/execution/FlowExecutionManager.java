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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.BeanFactoryFlowServiceLocator;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.support.FlowConversionService;

/**
 * A manager for the executing flows of the application. This object is responsible for
 * creating new flow executions as requested by the client, as well as signaling events
 * for processing by existing, paused executions (that are waiting to be resumed in response
 * to a user event).
 * <p>
 * The {@link #onEvent(Event)} method implements the following algorithm:
 * <ol>
 * <li>Look for a flow execution id in the event (in a parameter named
 * "_flowExecutionId").</li>
 * <li>If no flow execution id is found, a new flow execution is created.
 * The top-level flow for which the execution is created is determined
 * by first looking for a flow id specified in the event using the "_flowId"
 * parameter. If this parameter is present the specified flow will be
 * used, after lookup using a flow locator. If no "_flowId" parameter is
 * present, the default top-level flow configured for this manager is used.</li>
 * <li>If a flow execution id is found, the previously saved flow execution
 * with that id is loaded from the storage.</li>
 * <li>If a new flow execution was created in the previous steps, it is
 * started.</li>
 * <li>If an existing flow execution was loaded from storage, the current state id
 * ("_currentStateId") and event id ("_eventId") parameter values are
 * extracted from the event. The event is then signaled in that state, and 
 * the executing flow is resumed in that state.</li>
 * <li>If the flow execution is still active after event processing, it
 * is saved in storage. This process generates a unique flow execution
 * id that will be exposed to the caller for reference on subsequent events.
 * The caller will also be given access to the flow execution context and
 * any data placed in request or flow scope.</li>
 * </ol>
 * <p>
 * By default, this class will use the flow execution implementation provided
 * by the <code>FlowExecutionImpl</code> class. If you would like to use a 
 * different implementation, just override the {@link #createFlowExecution(Flow)}
 * method in a subclass.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowExecutionManager implements FlowExecutionListenerLoader, BeanFactoryAware {

	/**
	 * Clients can send the id (name) of the flow to be started
	 * using an event parameter with this name ("_flowId").
	 */
	public static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * Clients can send the flow execution id using an event
	 * parameter with this name ("_flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * The flow context itself will be exposed to the view in a model
	 * attribute with this name ("flowExecutionContext").
	 */
	public static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The current state of the flow execution will be exposed to the view in a
	 * model attribute with this name ("currentStateId").
	 */
	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	/**
	 * Event id value indicating that the event has not been set ("@NOT_SET@").
	 */
	public static final String NOT_SET_EVENT_ID = "@NOT_SET@";
	
	protected final Log logger = LogFactory.getLog(FlowExecutionManager.class);

	private Flow flow;

	private FlowLocator flowLocator = new BeanFactoryFlowServiceLocator();

	/**
	 * A map of all know flow execution listeners (the key) and their associated
	 * flow execution listener criteria objects (a list -- the value).
	 */
	private CachingMapDecorator flowExecutionListeners = new CachingMapDecorator() {
		protected Object create(Object key) {
			return new LinkedList();
		}
	};

	private FlowExecutionStorage storage;

	private TransactionSynchronizer transactionSynchronizer = new FlowScopeTokenTransactionSynchronizer();
	
	private ConversionService conversionService = new FlowConversionService();

	private BeanFactory beanFactory;

	/**
	 * Create a new flow execution manager. Before use, the manager should
	 * be appropriately configured using setter methods. At least the flow
	 * execution storage strategy should be set!
	 * 
	 * @see #setFlow(Flow)
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListener(FlowExecutionListener, FlowExecutionListenerCriteria)
	 * @see #setListenerMap(Map)
	 * @see #setListeners(Collection)
	 * @see #setListeners(Collection, FlowExecutionListenerCriteria)
	 * @see #setStorage(FlowExecutionStorage) 
	 * @see #setTransactionSynchronizer(TransactionSynchronizer)
	 * @see #setConversionService(ConversionService)
	 */
	public FlowExecutionManager() {
	}

	/**
	 * Returns the flow whose executions are managed by this manager.
	 * Could be <code>null</code> if there is no preconfigured flow and
	 * the id of the flow for which executions will be managed is sent
	 * in an event parameter "_flowId".
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow whose executions will be managed if there is no alternate
	 * flow id specified in a "_flowId" event parameter.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Returns the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the array of flow execution listeners for specified flow.
	 * @param flow the flow definition associated with the execution to be listened to
	 * @return the flow execution listeners
	 */
	public FlowExecutionListener[] getListeners(Flow flow) {
		Assert.notNull(flow, "The Flow to load listeners for cannot be null");
		List listeners = new LinkedList();
		for (Iterator entryIt = flowExecutionListeners.entrySet().iterator(); entryIt.hasNext(); ) {
			Map.Entry entry = (Map.Entry)entryIt.next();
			for (Iterator criteriaIt = ((List)entry.getValue()).iterator(); criteriaIt.hasNext(); ) {
				FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)criteriaIt.next();
				if (criteria.applies(flow)) {
					// the criteria 'guarding' this flow execution listener is
					// telling us that the listener applies to the flow
					listeners.add((FlowExecutionListener)entry.getKey());
					break;
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded " + listeners.size() + " of possible " + flowExecutionListeners.size() + " listeners to this execution request for flow: '" + flow.getId() 
					+ "', the listeners to attach are: " + StylerUtils.style(listeners));
		}
		return (FlowExecutionListener[])listeners.toArray(new FlowExecutionListener[listeners.size()]);
	}

	/**
	 * Set the flow execution listener that will be notified of managed
	 * flow executions.
	 */
	public void setListener(FlowExecutionListener listener) {
		setListeners(Collections.singleton(listener));
	}

	/**
	 * Set the flow execution listener that will be notified of managed
	 * flow executions for the flows that match given criteria.
	 */
	public void setListener(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		setListeners(Collections.singleton(listener), criteria);
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed
	 * flow executions.
	 */
	public void setListeners(Collection listeners) {
		setListeners(listeners, FlowExecutionListenerCriteriaFactory.allFlows());
	}
	
	/**
	 * Sets the flow execution listeners that will be notified of managed
	 * flow executions for flows that match given criteria.
	 */
	public void setListeners(Collection listeners, FlowExecutionListenerCriteria criteria) {
		for (Iterator it = listeners.iterator(); it.hasNext(); ) {
			FlowExecutionListener listener = (FlowExecutionListener)it.next();
			List registeredCriteria = (List)flowExecutionListeners.get(listener);
			registeredCriteria.add(criteria);
		}
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed
	 * flow executions. The map keys may be individual flow execution listener instances or 
	 * collections of execution listener instances. The map values can either
	 * be string encoded flow execution listener criteria or direct
	 * <code>FlowExecutionListenerCriteria</code> objects.
	 */
	public void setListenerMap(Map listenerCriteriaMap) {
		Iterator it = listenerCriteriaMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			FlowExecutionListenerCriteria criteria;
			if (entry.getValue() instanceof FlowExecutionListenerCriteria) {
				criteria = (FlowExecutionListenerCriteria)entry.getValue();
			}
			else {
				// string encoded
				criteria = 
					(FlowExecutionListenerCriteria)getConversionService().
						getConversionExecutor(String.class, FlowExecutionListenerCriteria.class).execute(entry.getValue());
			}
			if (entry.getKey() instanceof Collection) {
				setListeners((Collection)entry.getKey(), criteria);
			}
			else {
				setListener((FlowExecutionListener)entry.getKey(), criteria);
			}
		}
	}

	/**
	 * Add a listener that will listen to executions for all flows.
	 * @param listener the listener to add
	 */
	public void addListener(FlowExecutionListener listener) {
		addListener(listener, FlowExecutionListenerCriteriaFactory.allFlows());
	}
	
	/**
	 * Add a listener that wil listen to executions to flows matching the specified criteria
	 * @param listener the listener
	 * @param criteria the listener criteria
	 */
	public void addListener(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		List registeredCriteria = (List)this.flowExecutionListeners.get(listener);
		registeredCriteria.add(criteria);
	}

	/**
	 * Remove the flow execution listener from the listener list.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}
	
	/**
	 * Returns the storage strategy used by the flow execution manager.
	 */
	protected FlowExecutionStorage getStorage() {
		return storage;
	}

	/**
	 * Set the storage strategy used by the flow execution manager.
	 */
	public void setStorage(FlowExecutionStorage storage) {
		Assert.notNull(storage, "The flow execution storage strategy is required");
		this.storage = storage;
	}

	/**
	 * Return the application transaction synchronization strategy to use.
	 * This defaults to a <i>synchronizer token</i> based transaction management
	 * system, as implemented by {@link FlowScopeTokenTransactionSynchronizer}.
	 */
	protected TransactionSynchronizer getTransactionSynchronizer() {
		return transactionSynchronizer;
	}

	/**
	 * Set the application transaction synchronization strategy to use.
	 */
	public void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
		this.transactionSynchronizer = transactionSynchronizer;
	}

	/**
	 * Returns the conversion service used by this flow execution manager.
	 */
	public ConversionService getConversionService() {
		return conversionService;
	}
	
	/**
	 * Set the conversion service used by this flow execution manager.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Returns this flow execution manager's bean factory.
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		if (getFlowLocator() instanceof BeanFactoryAware) {
			// make the BeanFactoryFlowServiceLocator work
			((BeanFactoryAware)getFlowLocator()).setBeanFactory(beanFactory);
		}
	}

	// event processing

	/**
	 * Signal the occurence of the specified event - this is the entry point into the 
	 * webflow system for managing all executing flows.
	 * @param event the event that occured
	 * @return the view descriptor of the model and view to render
	 */
	public ViewDescriptor onEvent(Event event) {
		return onEvent(event, null);
	}

	/**
	 * Signal the occurence of the specified event - this is the entry point into the 
	 * webflow system for managing all executing flows.
	 * @param event the event that occured
	 * @param listener a listener interested in flow execution
	 *        lifecycle events that happen <i>while handling this event</i>
	 * @return the view descriptor of the model and view to render
	 */
	public ViewDescriptor onEvent(Event event, FlowExecutionListener listener) {
		if (logger.isDebugEnabled()) {
			logger.debug("New request received from client, source event is: "+ event);
		}
		FlowExecution flowExecution;
		ViewDescriptor selectedView;
		Serializable flowExecutionId = getFlowExecutionId(event);
		if (flowExecutionId == null) {
			// start a new flow execution
			Flow flow = getFlow(event);
			flowExecution = createFlowExecution(flow);
			if (listener != null) {
				flowExecution.getListeners().add(listener);
			}
			flowExecution.getListeners().fireCreated(flowExecution);
			if (logger.isDebugEnabled()) {
				logger.debug("Created a new flow execution for flow definition: '" + flow.getId() + "'");
			}
			selectedView = flowExecution.start(event);
		}
		else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getStorage().load(flowExecutionId, event);
			// rehydrate the execution if neccessary (if it had been serialized out)
			flowExecution.rehydrate(getFlowLocator(), this, getTransactionSynchronizer());
			if (listener != null) {
				flowExecution.getListeners().add(listener);
			}
			flowExecution.getListeners().fireLoaded(flowExecution, flowExecutionId);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded existing flow execution from storage with id: '" + flowExecutionId + "'");
			}
			// signal the event within the current state
			Assert.hasText(event.getId(), "No eventId could be obtained -- "
					+ "make sure the client provides the _eventId parameter as input; the parameters provided for this request were:" 
					+ StylerUtils.style(event.getParameters()));
			// see if the eventId was set to a static marker placeholder because
			// of a client configuration error
			if (event.getId().equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The received eventId was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' -- this is likely a client view (jsp, etc) configuration error --"
						+ "the _eventId parameter must be set to a valid event");
			}
			selectedView = flowExecution.signalEvent(event);
		}
		if (flowExecution.isActive()) {
			// save the flow execution for future use
			flowExecutionId = getStorage().save(flowExecutionId, flowExecution, event);
			flowExecution.getListeners().fireSaved(flowExecution, flowExecutionId);
			if (logger.isDebugEnabled()) {
				logger.debug("Saved flow execution out to storage with id: '" + flowExecutionId + "'");
			}
		}
		else {
			// event execution resulted in the entire flow execution ending, cleanup
			if (flowExecutionId != null) {
				getStorage().remove(flowExecutionId, event);
				flowExecution.getListeners().fireRemoved(flowExecution, flowExecutionId);
				if (logger.isDebugEnabled()) {
					logger.debug("Removed flow execution from storage with id: '" + flowExecutionId + "'");
				}
			}
		}
		if (listener != null) {
			flowExecution.getListeners().remove(listener);
		}
		selectedView = prepareViewDescriptor(selectedView, flowExecutionId, flowExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view to client: " + selectedView);
		}
		return selectedView;
	}

	// subclassing hooks

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine this
	 * if they wish to use a specialized FlowExecution implementation class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		return new FlowExecutionImpl(flow, getListeners(flow), getTransactionSynchronizer());
	}

	/**
	 * Obtain a flow to use from given event. If there is a "_flowId" parameter
	 * specified in the event, the flow with that id will be returend after
	 * lookup using the flow locator. If no "_flowId" parameter is present in the
	 * event, the default top-level flow will be returned.
	 */
	protected Flow getFlow(Event event) {
		String flowId = ExternalEvent.verifySingleStringInputParameter(getFlowIdParameterName(), event.getParameter(getFlowIdParameterName()));
		if (!StringUtils.hasText(flowId)) {
			Assert.notNull(getFlow(),
					"This flow execution manager is not configured with a default top-level flow--that means "
							+ "the flow to launch must be provided by the client via the '"
							+ getFlowIdParameterName() + "' parameter, yet no such parameter was provided in this event." +
							" Parameters provided were: " + StylerUtils.style(event.getParameters()));
			return getFlow();
		}
		else {
			Assert.notNull(getFlowLocator(), "The flow locator is required to lookup the requested flow with id '"
					+ flowId + "'; however, the flowLocator property is null");
			return getFlowLocator().getFlow(flowId);
		}
	}

	/**
	 * Returns the name of the flow id parameter in the event ("_flowId").
	 */
	protected String getFlowIdParameterName() {
		return FLOW_ID_PARAMETER;
	}

	/**
	 * Obtain a unique flow execution id from given event.
	 * @param event the event
	 * @return the obtained id or <code>null</code> if not found
	 */
	protected String getFlowExecutionId(Event event) {
		return ExternalEvent.verifySingleStringInputParameter(getFlowExecutionIdParameterName(), event.getParameter(getFlowExecutionIdParameterName()));
	}

	/**
	 * Returns the name of the flow execution id parameter in the event
	 * ("_flowExecutionId").
	 */
	protected String getFlowExecutionIdParameterName() {
		return FLOW_EXECUTION_ID_PARAMETER;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the event because of a view configuration error ("@NOT_SET@").
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * event parameter, for example, using javascript. This approach assumes
	 * the "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	protected String getNotSetEventIdParameterMarker() {
		return NOT_SET_EVENT_ID;
	}

	/**
	 * Do any processing necessary before given view descriptor can be returned
	 * to the client of the flow execution manager. This implementation adds
	 * a number of <i>infrastructure attributes</i> to the model that will be
	 * exposed to the view. More specifically, it will add the
	 * {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE}, {@link #FLOW_EXECUTION_ID_ATTRIBUTE}
	 * and {@link #CURRENT_STATE_ID_ATTRIBUTE}.
	 * @param viewDescriptor the view descriptor to be processed
	 * @param flowExecutionId the unique id of the flow execution
	 * @param flowExecutionContext the flow context providing info about the flow execution
	 * @return the processed view descriptor
	 */
	protected ViewDescriptor prepareViewDescriptor(ViewDescriptor viewDescriptor, Serializable flowExecutionId,
			FlowExecutionContext flowExecutionContext) {
		if (flowExecutionContext.isActive() && viewDescriptor != null) {
			if (viewDescriptor.isRedirect()) {
				viewDescriptor.addObject(getFlowExecutionIdParameterName(), flowExecutionId);
			}
			else {
				// make the entire flow execution context available in the model
				viewDescriptor.addObject(FLOW_EXECUTION_CONTEXT_ATTRIBUTE, flowExecutionContext);
				// make the unique flow execution id and current state id available in the model as convenience to views
				viewDescriptor.addObject(FLOW_EXECUTION_ID_ATTRIBUTE, flowExecutionId);
				viewDescriptor.addObject(CURRENT_STATE_ID_ATTRIBUTE, flowExecutionContext.getCurrentState().getId());
			}
		}
		return viewDescriptor;
	}
}