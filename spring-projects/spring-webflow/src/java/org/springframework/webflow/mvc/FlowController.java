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
package org.springframework.webflow.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.webflow.Flow;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.servlet.ServletFlowExecutionManager;

/**
 * Web controller for the Spring web MVC framework that routes incoming requests to one
 * or more managed web flows. Requests into the web flow system are managed using a
 * configurable {@link ServletFlowExecutionManager}. Consult the JavaDoc of that class
 * for more information on how requests are processed.
 * <p>
 * Note that a single FlowController may manage executions for all flows of your application
 * -- simply parameterize this controller from view code with the <code>_flowId</code> to
 * execute. See the flowLauncher sample application for an example of this.
 * <p>
 * Configuration note: you may achieve fine-grained control over flow execution management by 
 * passing in a configured flow execution manager instance. Alternatively, if this
 * controller should manage executions in the default manner for a single flow definition,
 * simply configure the flow property.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowExecutionManager</td>
 * <td>{@link org.springframework.webflow.execution.servlet.ServletFlowExecutionManager default}</td>
 * <td>Configures the HTTP servlet flow execution manager implementation to use.</td>
 * </tr>
 * <tr>
 * <td>flow</td>
 * <td>nulls</td>
 * <td>Configures a single Flow definition to manage. Note this property should only be set as a
 * convenience if fine-grained configuration of the flowExecutionManager is not neccessary.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.webflow.Flow
 * @see org.springframework.webflow.execution.servlet.ServletFlowExecutionManager
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController implements InitializingBean {

	/**
	 * The HTTP servlet-based manager for flow executions.
	 */
	private ServletFlowExecutionManager flowExecutionManager;

	/**
	 * Create a new FlowController.
	 * <p>
	 * The "cacheSeconds" property is by default set to 0 (so no caching for
	 * web flow controllers).
	 */
	public FlowController() {
		initDefaults();
	}

	/**
	 * Set default properties for this controller.
	 */
	protected void initDefaults() {
		// no caching
		setCacheSeconds(0);
		setFlowExecutionManager(new ServletFlowExecutionManager());
	}

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the HTTP flow execution manager
	 */
	protected ServletFlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 * Note: do not call both this method and <code>setFlow()</code> -- call one or the other. 
	 * @param manager the flow execution manager
	 * 
	 * @see #setFlow(Flow)
	 */
	public void setFlowExecutionManager(ServletFlowExecutionManager manager) {
		this.flowExecutionManager = manager;
	}

	/**
	 * Convenience setter that configures a single flow definition for this controller to
	 * manage. This is a convenience feature to make it easy configure the flow for
	 * a controller which just uses the default flow execution manager.
	 * Note: do not call both this method and <code>setFlowExecutionManager()</code> -- call one
	 * or the other.
	 * @param flow the flow that this controller will manage
	 * 
	 * @see #setFlowExecutionManager(ServletFlowExecutionManager)
	 */
	public void setFlow(Flow flow) {
		this.flowExecutionManager.setFlow(flow);
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.flowExecutionManager, "The http servlet flow execution manager is required");
		this.flowExecutionManager.setBeanFactory(getApplicationContext());
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// delegate to the flow execution manager to process the request
		ViewDescriptor viewDescriptor = flowExecutionManager.handle(request, response);
		// convert the view descriptor to a ModelAndView object
		return toModelAndView(viewDescriptor);
	}

	/**
	 * Create a ModelAndView object based on the information in given view
	 * descriptor. Subclasses can override this to return a specialized ModelAndView
	 * or to do custom processing on it.
	 * @param viewDescriptor the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewDescriptor viewDescriptor) {
		String viewName = viewDescriptor.getViewName();
		if (viewDescriptor.isRedirect()) {
			viewName = UrlBasedViewResolver.REDIRECT_URL_PREFIX + viewName;
		}
		return viewDescriptor == null ? null : new ModelAndView(viewName, viewDescriptor.getModel());
	}
}