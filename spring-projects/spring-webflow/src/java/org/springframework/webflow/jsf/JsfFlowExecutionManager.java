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

package org.springframework.webflow.jsf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * A JSF-specific subclass of
 * {@link org.springframework.webflow.execution.FlowExecutionManager} which is
 * delegated to by Web Flow's
 * {@link org.springframework.webflow.jsf.FlowNavigationHandler} and
 * {@link org.springframework.webflow.jsf.FlowPhaseListener}. The latter
 * classes will expect to find a configured instance of this class in the web
 * application context accessible through
 * {@link FacesContextUtils#getWebApplicationContext(javax.faces.context.FacesContext)}
 * as the bean named {@link #BEAN_NAME}.
 * 
 * @author Colin Sampaleanu
 * @author Craig McClanahan
 * @author Keith Donald
 */
public class JsfFlowExecutionManager extends FlowExecutionManager {

	/**
	 * Bean name under which we will find the configured instance of the
	 * {@link JsfFlowExecutionManager} to be used for determining what logical
	 * actions to undertake.
	 */
	public static final String BEAN_NAME = "flowExecutionManager";

	/**
	 * Prefix on a logical outcome value that identifies a logical outcome as
	 * the identifier for a web flow that should be entered.
	 */
	protected static final String WEBFLOW_PREFIX = "webflow:";

	/**
	 * Create a new flow execution manager using the specified flow locator for
	 * loading Flow definitions.
	 * @param flowLocator the flow locator to use
	 */
	public JsfFlowExecutionManager(FlowLocator flowLocator) {
		super(flowLocator);
	}

	/**
	 * Return <code>true</code> if the current request is asking for the
	 * creation of a new flow. The default implementation examines the logical
	 * outcome to see if it starts with the prefix specified by
	 * <code>WebFlowNavigationStrategy.PREFIX</code>.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowLaunchRequest(FacesContext context, String fromAction, String outcome) {
		if (outcome == null) {
			return false;
		}
		return outcome.startsWith(WEBFLOW_PREFIX);
	}

	/**
	 * Create a new flow execution for the current request. Proceed until a
	 * <code>ViewDescriptor</code> is returned describing the next view that
	 * should be rendered.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @return the selected starting view
	 */
	public ViewDescriptor launchFlowExecution(FacesContext context, String fromAction, String outcome) {
		// strip off the webflow prefix, leaving the flowId to launch
		String flowId = getRequiredFlowId(outcome);
		JsfEvent sourceEvent = createEvent(context, fromAction, outcome, null);
		Serializable flowExecutionId = null;
		boolean flowExecutionSaved = false;
		FlowExecution flowExecution = createFlowExecution(getFlowLocator().getFlow(flowId));
		ViewDescriptor selectedView = flowExecution.start(sourceEvent);
		if (flowExecution.isActive()) {
			if (getStorage().supportsTwoPhaseSave()) {
				flowExecutionId = getStorage().generateId(null);
			}
			else {
				// save the flow execution for future use
				flowExecutionId = saveFlowExecution(null, flowExecution, sourceEvent);
				flowExecutionSaved = true;
			}
		}
		FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecution, sourceEvent, flowExecutionSaved);
		return prepareSelectedView(selectedView, flowExecutionId, flowExecution);
	}

	/**
	 * Returns the flow id in the submitted outcome string.
	 * @param outcome the jsf outcome
	 * @return the flow id
	 * @throws IllegalArgumentException if no flow id was found
	 */
	protected String getRequiredFlowId(String outcome) throws IllegalArgumentException {
		String flowId = outcome.substring(WEBFLOW_PREFIX.length());
		Assert.hasText(flowId, "The id of the flow to launch was not provided in the outcome string: programmer error");
		return flowId;
	}

	/**
	 * Creates a flow execution listener to attach to a managed FlowExecution.
	 * @param context the faces context
	 * @return the listener to attach
	 */
	protected FlowExecutionListener createFlowExecutionListener(FacesContext context) {
		return new JsfFlowExecutionListener(context);
	}

	/**
	 * Return <code>true</code> if there is an existing flow execution in
	 * progress for the current request. Implementors must ensure that the
	 * algorithm used to makes this determination matches the determination that
	 * will be made by the <code>FlowExecutionManager</code> that is being
	 * used. The default implementation looks for a request parameter named by
	 * <code>WebFlowNavigationStrategy.FLOW_EXECUTION_ID</code>.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 */
	public boolean isFlowExecutionParticipationRequest(FacesContext context, String fromAction, String outcome) {
		boolean executionBound = FlowExecutionHolder.getFlowExecution() == null ? false : true;
		boolean flowExecutionIdPresent = context.getExternalContext().getRequestParameterMap().containsKey(
				FlowExecutionManager.FLOW_EXECUTION_ID_PARAMETER);
		// assert an invariant, just to be safe
		Assert
				.isTrue(
						executionBound == flowExecutionIdPresent,
						"The flow execution bound to the current thread context must match the existence of a _flowExecutionId request attribute");
		return executionBound;
	}

	/**
	 * Resume an active flow execution for the current request. Proceed until a
	 * <code>ViewDescriptor</code> is returned describing the next view that
	 * should be rendered.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @return the selected next (or ending) view
	 */
	public ViewDescriptor resumeFlowExecution(FacesContext context, String fromAction, String outcome) {
		JsfEvent event = createEvent(context, fromAction, outcome, null);
		Serializable flowExecutionId = FlowExecutionHolder.getFlowExecutionId();
		FlowExecution flowExecution = FlowExecutionHolder.getFlowExecution();
		ViewDescriptor selectedView = signalEventIn(flowExecution, event);
		if (flowExecution.isActive()) {
			if (getStorage().supportsTwoPhaseSave()) {
				flowExecutionId = getStorage().generateId(null);
				FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecution, event, false);
			}
			else {
				// save the flow execution for future use
				flowExecutionId = saveFlowExecution(flowExecutionId, flowExecution, event);
				FlowExecutionHolder.setFlowExecution(flowExecutionId, flowExecution, event, true);
			}
		}
		else {
			removeFlowExecution(flowExecutionId, flowExecution, event);
		}
		return prepareSelectedView(selectedView, flowExecutionId, flowExecution);
	}

	/**
	 * Construct and return an <code>Event</code> reflecting the flow
	 * execution event that represents this request. The default implementation
	 * constructs and returns a <code>JsfEvent</code> reflecting the current
	 * request.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param parameters Optional additional event parameters
	 */
	public JsfEvent createEvent(FacesContext context, String fromAction, String outcome, Map parameters) {
		return new JsfEvent(outcome, context, fromAction, parameters);
	}

	/**
	 * Render the view specified by this <code>ViewDescriptor</code>, after
	 * exposing any model data it includes.
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param outcome The logical outcome returned by the specified action
	 * @param viewDescriptor <code>ViewDescriptor</code> for the view to
	 * render
	 */
	public void renderView(FacesContext context, String fromAction, String outcome, ViewDescriptor viewDescriptor) {
		// Expose model data specified in the descriptor
		try {
			context.getExternalContext().getRequestMap().putAll(viewDescriptor.getModel());
		}
		catch (UnsupportedOperationException e) {
			// work around nasty MyFaces bug where it's RequestMap doesn't
			// support putAll remove after it's fixed in MyFaces
			Map requestMap = context.getExternalContext().getRequestMap();
			Iterator it = viewDescriptor.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				requestMap.put(entry.getKey(), entry.getValue());
			}
		}
		// Stay on the same view if requested
		if (viewDescriptor.getViewName() == null) {
			return;
		}
		// Create the specified view so that it can be rendered
		ViewHandler vh = context.getApplication().getViewHandler();
		UIViewRoot view = vh.createView(context, viewDescriptor.getViewName());
		context.setViewRoot(view);
	}

	/**
	 * Responsible for restoring (loading) the flow execution if the appropriate
	 * flow execution id parameter is found in the faces context request map.
	 * Normally called by the phase listener.
	 * 
	 * @param context <code>FacesContext</code> for the current request
	 */
	public void restoreFlowExecution(FacesContext context) {
		Map parameters = context.getExternalContext().getRequestParameterMap();
		if (parameters.containsKey(getFlowExecutionIdParameterName())) {
			Serializable id = (Serializable)parameters.get(getFlowExecutionIdParameterName());
			Map map = new HashMap(1);
			map.put(getFlowExecutionIdParameterName(), id);
			JsfEvent sourceEvent = createEvent(context, null, null, map);
			FlowExecution flowExecution = loadFlowExecution(id, sourceEvent);
			// note that the event will be replaced in the case of actual
			// navigation
			FlowExecutionHolder.setFlowExecution(id, flowExecution, sourceEvent, false);
		}
	}

	/**
	 * Return the JsfFlowExecutionManager from a known location, as a bean
	 * called FlowNavigationHandlerStrategy.BEAN_NAME in the web application
	 * context accessible from FacesContextUtils.getWebApplicationContext.
	 * <p>
	 * This value should be cached.
	 * @param context <code>FacesContext</code> for the current request
	 */
	public static JsfFlowExecutionManager getFlowExecutionManager(FacesContext context) {
		WebApplicationContext wac = FacesContextUtils.getWebApplicationContext(context);
		return (JsfFlowExecutionManager)wac.getBean(JsfFlowExecutionManager.BEAN_NAME, JsfFlowExecutionManager.class);
	}
}