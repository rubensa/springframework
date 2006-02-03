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
package org.springframework.webflow.executor.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.support.FlowExecutorHelper;
import org.springframework.webflow.executor.support.FlowExecutorParameterExtractor;

/**
 * Point of integration between Spring MVC and Spring Web Flow: a
 * {@link Controller} that routes incoming requests to one or more managed flow
 * executions.
 * <p>
 * Requests into the web flow system are handled by a {@link FlowExecutor},
 * which this class delegates to using a {@link FlowExecutorHelper}. Consult
 * the JavaDoc of that class for more information on how requests are processed.
 * <p>
 * Note: a single FlowController may execute all flows of your application.
 * Specifically:
 * <ul>
 * <li>To have this controller launch a new flow execution (conversation), have
 * the client send a
 * {@link FlowExecutorParameterExtractor#getFlowIdParameterName()} request
 * parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutorParameterExtractor#getFlowExecutionIdParameterName()}
 * request parameter identifying the conversation to participate in.
 * </ul>
 * <p>
 * See the flowLauncher sample application for an example of this controller
 * parameterization.
 * <p>
 * Usage example:
 * 
 * <pre>
 *     &lt;!--
 *         Exposes flows for execution at a single request URL.
 *         The id of a flow to launch should be passed in by clients using
 *         the &quot;_flowId&quot; request parameter:
 *         e.g. /app.htm?_flowId=flow1
 *     --&gt;
 *     &lt;bean name=&quot;/app.htm&quot; class=&quot;org.springframework.webflow.executor.mvc.FlowController&quot;&gt;
 *         &lt;constructor-arg ref=&quot;flowLocator&quot;/&gt;
 *     &lt;/bean&gt;
 *                               
 *     &lt;!-- Creates the registry of flow definitions for this application --&gt;
 *     &lt;bean name=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowLocations&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;value&gt;/WEB-INF/flow1.xml&quot;&lt;/value&gt;
 *                 &lt;value&gt;/WEB-INF/flow2.xml&quot;&lt;/value&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * It is also possible to customize the {@link FlowExecutorParameterExtractor}
 * strategy to allow for different types of controller parameterization, for
 * example perhaps in conjunction with a REST-style request mapper.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController {

	/**
	 * Delegate for managing flow executions (launching new executions, and
	 * resuming existing executions).
	 */
	private FlowExecutor flowExecutor;

	/**
	 * Delegate for extracting flow executor parameters from a request
	 * made by a {@link ExternalContext}.
	 */
	private FlowExecutorParameterExtractor parameterExtractor = new FlowExecutorParameterExtractor();

	/**
	 * Create a new FlowController that delegates to the configured executor
	 * for driving the execution of web flows.
	 * @param flowExecutor the service to launch and resume flow executions
	 * brokered by this web controller.
	 */
	public FlowController(FlowExecutor flowExecutor) {
		initDefaults();
		setFlowExecutor(flowExecutor);
	}

	/**
	 * Convenience constructor that creates a new FlowController that initially
	 * relies on a default
	 * {@link org.springframework.webflow.executor.FlowExecutorImpl}
	 * implementation that uses the provided flow locator to access flow
	 * definitions at runtime.
	 */
	public FlowController(FlowLocator flowLocator) {
		initDefaults();
		setFlowExecutor(new FlowExecutorImpl(flowLocator));
	}

	/**
	 * Set default properties for this controller. * The "cacheSeconds" property
	 * is by default set to 0 (so by default there is no HTTP header caching for
	 * web flow controllers).
	 */
	protected void initDefaults() {
		// no caching
		setCacheSeconds(0);
	}

	/**
	 * Returns the flow executor used by this controller.
	 * @return the flow executor
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Sets the flow executor to use.
	 * @param flowExecutor the flow executor
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Returns the flow executor parameter extractor used by this controller.
	 * @return the parameter extractor
	 */
	public FlowExecutorParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	/**
	 * Sets the flow executor parameter extractor to use.
	 * @param parameterExtractor the parameter extractor
	 */
	public void setParameterExtractor(FlowExecutorParameterExtractor parameterExtractor) {
		this.parameterExtractor = parameterExtractor;
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ViewSelection selectedView = createControllerHelper().handleFlowRequest(
				new ServletExternalContext(getServletContext(), request, response));
		return toModelAndView(selectedView);
	}

	/**
	 * Factory method that creates a new helper for processing a request into
	 * this flow controller. The controller is a basic template encapsulating
	 * reusable flow execution request handling workflow.
	 * @return the controller helper
	 */
	protected FlowExecutorHelper createControllerHelper() {
		return new FlowExecutorHelper(getFlowExecutor(), getParameterExtractor());
	}

	/**
	 * Create a ModelAndView object based on the information in the selected
	 * view descriptor. Subclasses can override this to return a specialized
	 * ModelAndView or to do custom processing on it.
	 * @param selectedView the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewSelection selectedView) {
		if (selectedView == ViewSelection.NULL_VIEW_SELECTION) {
			return null;
		}
		String viewName = selectedView.getViewName();
		if (selectedView.isRedirect()) {
			viewName = UrlBasedViewResolver.REDIRECT_URL_PREFIX + viewName;
		}
		return new ModelAndView(viewName, selectedView.getModel());
	}
}