/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.web.flow.support;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Helper to manage flow execution and process requests coming into a flow
 * execution. This class provides numerous methods that can be extended in
 * subclasses to fine-tune the execution algorithm.
 * 
 * <p>
 * The <code>handleRequest()</code> method implements the following algorithm:
 * <ol>
 * <li>Look for a flow execution id in the request (in a parameter named
 * "_flowExecutionId").</li>
 * <li>If a flow execution id is not found, a new flow execution will be
 * created. The top-level flow for which the execution is created is determined
 * by first looking for a flow id specified in the request using the "_flowId"
 * request parameter. If this parameter is set, the specified flow will be used,
 * after lookup using a flow locator. If no "_flowId" parameter is present, the
 * default top-level flow configured for this manager is used.</li>
 * <li>If a flow execution id is found, the corresponding flow execution is
 * obtained from the HTTP session.</li>
 * <li>If a new flow execution was created in the previous steps, it will be
 * started.</li>
 * <li>If an existing flow execution is continued, current state id
 * ("_currentStateId") and event id ("_eventId") parameter values will be
 * obtained from the request and will be signaled in the flow execution.</li>
 * </ol>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class HttpFlowExecutionManager {

	protected final Log logger = LogFactory.getLog(HttpFlowExecutionManager.class);

	private Flow flow;

	private FlowLocator flowLocator;

	private Collection flowExecutionListeners;

	/**
	 * Create a new flow execution manager.
	 * @param flow the default flow for which executions will be managed
	 */
	public HttpFlowExecutionManager(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flow the default flow for which executions will be managed
	 * @param flowExecutionListeners the set of listeners that should be
	 *        notified of lifecycle events in the managed flow execution
	 */
	public HttpFlowExecutionManager(Flow flow, Collection flowExecutionListeners) {
		this.flow = flow;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flow the default flow for which executions will be managed
	 * @param flowLocator the flow locator to use for flow lookup of possible
	 *        other flows specified using the "_flowId" request parameter
	 */
	public HttpFlowExecutionManager(Flow flow, FlowLocator flowLocator) {
		this.flow = flow;
		this.flowLocator = flowLocator;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flowId id of the default flow for which executions will be managed
	 * @param flowLocator the flow locator to use for flow lookup
	 */
	public HttpFlowExecutionManager(String flowId, FlowLocator flowLocator) {
		if (StringUtils.hasText(flowId)) {
			this.flow = flowLocator.getFlow(flowId);
		}
		this.flowLocator = flowLocator;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flow the default flow for which executions will be managed
	 * @param flowLocator the flow locator to use for flow lookup of possible
	 *        other flows specified using the "_flowId" request parameter
	 * @param flowExecutionListeners the set of listeners that should be
	 *        notified of lifecycle events in the managed flow execution
	 */
	public HttpFlowExecutionManager(Flow flow, FlowLocator flowLocator, Collection flowExecutionListeners) {
		this.flow = flow;
		this.flowLocator = flowLocator;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	/**
	 * Create a new flow execution manager. Since no default flow is specified,
	 * the id of the flow for which executions will be managed is expected in
	 * the request parameter "_flowId".
	 * @param flowLocator the flow locator to use for flow lookup
	 */
	public HttpFlowExecutionManager(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Create a new flow execution manager. Since no default flow is specified,
	 * the id of the flow for which executions will be managed is expected in
	 * the request parameter "_flowId".
	 * @param flowLocator the flow locator to use for flow lookup
	 * @param flowExecutionListeners the set of listeners that should be
	 *        notified of lifecycle events in the managed flow execution
	 */
	public HttpFlowExecutionManager(FlowLocator flowLocator, Collection flowExecutionListeners) {
		this.flowLocator = flowLocator;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	//subclassing hooks

	/**
	 * Returns the name of the flow id parameter in the request ("_flowId").
	 */
	protected String getFlowIdParameterName() {
		return FlowConstants.FLOW_ID_PARAMETER;
	}

	/**
	 * Returns the name of the flow execution id parameter in the request
	 * ("_flowExecutionId").
	 */
	protected String getFlowExecutionIdParameterName() {
		return FlowConstants.FLOW_EXECUTION_ID_PARAMETER;
	}

	/**
	 * Returns the name of the current state id parameter in the request
	 * ("_currentStateId").
	 */
	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

	/**
	 * Returns the name of the event id parameter in the request ("_eventId").
	 */
	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	/**
	 * Returns the name of the event id attribute in the request
	 * ("_mapped_eventId")
	 * <p>
	 * This is useful when working with image buttons and javscript
	 * restrictions. For example, a intercepting servlet filter can process a
	 * image button with a name in the format "_pname__eventId_pvalue_submit"
	 * and set the proper "mapped' eventId attribute in the request.
	 */
	protected String getEventIdRequestAttributeName() {
		return FlowConstants.EVENT_ID_REQUEST_ATTRIBUTE;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the request because of view configuration error (
	 * {@link FlowConstants.NOT_SET_EVENT_ID)
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * request parameter, for example, using javascript. This approach assumes
	 * the "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	protected String getNotSetEventIdParameterMarker() {
		return FlowConstants.NOT_SET_EVENT_ID;
	}

	/**
	 * Returns the default delimiter used to separate a request parameter name
	 * and value when both are embedded in the name of the request parameter
	 * (e.g. when using an HTML submit button)
	 */
	protected String getParameterValueDelimiter() {
		return "_";
	}

	/**
	 * The main entry point into managed HTTP-based flow executions.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @return the model and view to render
	 * @throws Exception in case of errors
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		FlowExecution flowExecution;
		ModelAndView modelAndView;
		if (isNewFlowExecutionRequest(request)) {
			// start a new flow execution
			flowExecution = createFlowExecution(getFlow(request));
			modelAndView = flowExecution.start(getFlowExecutionInput(request), request, response);
			saveInHttpSession(flowExecution, request);
		}
		else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(request);

			// let client tell you what state they are in (if possible)
			String stateId = request.getParameter(getCurrentStateIdParameterName());

			// let client tell you what event was signaled in the current state
			String eventId = request.getParameter(getEventIdParameterName());

			if (!StringUtils.hasText(eventId)) {
				// see if the evenId is set as a request attribute (put
				// there by a servlet filter)
				eventId = (String)request.getAttribute(getEventIdRequestAttributeName());
			}
			if (!StringUtils.hasText(eventId)) {
				// perform an exhaustive search for the eventId in the request
				// parameter list
				if (logger.isDebugEnabled()) {
					logger
							.debug("No '"
									+ getEventIdRequestAttributeName()
									+ "' request attribute was found; performing exhaustive search for the eventId in the request parameter list");
				}
				eventId = searchForRequestParameter(request, getEventIdParameterName());
				if (!StringUtils.hasText(eventId)) {
					throw new IllegalArgumentException(
							"The '"
									+ getEventIdParameterName()
									+ "' request parameter (or '"
									+ getEventIdRequestAttributeName()
									+ "' request attribute) is required to signal an event in the current state of this executing flow '"
									+ flowExecution.getCaption() + "' -- programmer error?");
				}
			}
			// see if the eventId was set to a static marker placeholder because
			// of a view configuration error
			if (eventId.equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId submitted by the browser was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' - this is likely a view (jsp, etc) configuration error - the '"
						+ getEventIdParameterName()
						+ "' parameter must be set to a valid event to execute within the current state '" + stateId
						+ "' of this flow '" + flowExecution.getCaption() + "' - else I don't know what to do!");
			}
			// execute the signaled event within the current state
			modelAndView = flowExecution.signalEvent(eventId, stateId, request, response);
		}
		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected model and view " + modelAndView);
		}
		return modelAndView;
	}

	/**
	 * Create a map of input attributes for new flow executions started by the
	 * execution manager.
	 * <p>
	 * Default implementation returns null. Subclasses can override if needed.
	 * @param request current HTTP request
	 * @return a Map with reference data entries, or null if none
	 */
	protected Map getFlowExecutionInput(HttpServletRequest request) {
		return null;
	}

	/**
	 * Obtain a flow to use from given request. If there is a flow id parameter
	 * specified in the request, the flow with that id will be returend after
	 * lookup using the flow locator. If no flow id parameter is present in the
	 * request, the default top-level flow will be returned.
	 */
	protected Flow getFlow(HttpServletRequest request) {
		String flowId = request.getParameter(getFlowIdParameterName());
		if (!StringUtils.hasText(flowId)) {
			Assert.notNull(this.flow, "This flow execution manager is not configured with a default top-level flow");
			return this.flow;
		}
		else {
			Assert.notNull(this.flowLocator,
					"The flow locator is required to lookup flows to execute by a flow id request parameter");
			return this.flowLocator.getFlow(flowId);
		}
	}

	/**
	 * Create a new flow execution for given flow.
	 * @param flow The flow
	 * @return The created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = flow.createExecution();
		if (flowExecutionListeners != null && !flowExecutionListeners.isEmpty()) {
			flowExecution.getListenerList().add(
					(FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[0]));
		}
		return flowExecution;
	}

	/**
	 * Check if given request is a request for a new flow execution, or a
	 * continuation of an existing one.
	 * @param request the HTTP request to check
	 * @return true or false
	 */
	protected boolean isNewFlowExecutionRequest(HttpServletRequest request) {
		return request.getParameter(getFlowExecutionIdParameterName()) == null;
	}

	/**
	 * Save the flow execution in the HTTP session associated with given
	 * request.
	 */
	protected void saveInHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution '" + flowExecution.getId() + "' in HTTP session");
		}
		request.getSession(false).setAttribute(flowExecution.getId(), flowExecution);
	}

	/**
	 * Get an existing flow execution from the HTTP session associated with
	 * given request.
	 * @throws NoSuchFlowExecutionException If there is no flow execution in the
	 *         HTTP session associated with given request.
	 * @throws ServletRequestBindingException If there is no flow execution id
	 *         bound in given request.
	 */
	protected FlowExecution getRequiredFlowExecution(HttpServletRequest request) throws NoSuchFlowExecutionException,
			ServletRequestBindingException {
		String flowExecutionId = RequestUtils.getRequiredStringParameter(request, getFlowExecutionIdParameterName());
		try {
			return (FlowExecution)WebUtils.getRequiredSessionAttribute(request, flowExecutionId);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(flowExecutionId, e);
		}
	}

	/**
	 * Remove given flow execution from the HTTP session associated with given
	 * request.
	 */
	public void removeFromHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution '" + flowExecution.getId() + "' from HTTP session");
		}
		request.getSession(false).removeAttribute(flowExecution.getId());
	}

	/**
	 * Obtain a named parameter from an HTTP servlet request. This method will
	 * try to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value from the request using just the given
	 * <i>logical </i> name. This handles request parameters of the form
	 * <tt>logicalName = value</tt>. For normal request parameters, e.g.
	 * submitted using a hidden HTML form field, this will return the requested
	 * value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the request is of the form
	 * <tt>logicalName_value = xyz</tt>. This deals with parameter values
	 * submitted using an HTML form submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the
	 * request would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param request the current HTTP request
	 * @param logicalName the <i>logical </i> name of the request parameter
	 * @return the value of the parameter, or <code>null</code> if the
	 *         parameter does not exist in given request
	 */
	protected String searchForRequestParameter(HttpServletRequest request, String logicalName) {
		return searchForRequestParameter(request, logicalName, getParameterValueDelimiter());
	}

	/**
	 * Obtain a named parameter from an HTTP servlet request. This method will
	 * try to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value from the request using just the given
	 * <i>logical </i> name. This handles request parameters of the form
	 * <tt>logicalName = value</tt>. For normal request parameters, e.g.
	 * submitted using a hidden HTML form field, this will return the requested
	 * value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the request is of the form
	 * <tt>logicalName_value = xyz</tt> with "_" being the specified
	 * delimiter. This deals with parameter values submitted using an HTML form
	 * submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the
	 * request would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param request the current HTTP request
	 * @param logicalName the <i>logical </i> name of the request parameter
	 * @param delimiter the delimiter to use
	 * @return the value of the parameter, or <code>null</code> if the
	 *         parameter does not exist in given request
	 */
	protected String searchForRequestParameter(HttpServletRequest request, String logicalName, String delimiter) {
		//first try to get it as a normal name=value parameter
		String value = request.getParameter(logicalName);
		if (value != null) {
			return value;
		}
		//if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalName + delimiter;
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String)paramNames.nextElement();
			if (paramName.startsWith(prefix)) {
				value = paramName.substring(prefix.length());
				//support images buttons, which would submit parameters as
				//name_value.x=123
				if (value.endsWith(".x") || value.endsWith(".y")) {
					value = value.substring(0, value.length() - 2);
				}
				return value;
			}
		}
		//we couldn't find the parameter value
		return null;
	}
}