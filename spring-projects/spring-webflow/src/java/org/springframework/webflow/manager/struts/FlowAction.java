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
package org.springframework.webflow.manager.struts;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.web.struts.ActionSupport;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.manager.FlowExecutionManager;
import org.springframework.webflow.manager.support.FlowExecutionManagerHelper;
import org.springframework.webflow.manager.support.FlowExecutionManagerParameterExtractor;

/**
 * Point of integration between Struts and Spring Web Flow: a Struts Action that
 * acts a front controller entry point into the web flow system. A single
 * FlowAction may launch any new FlowExecution. In addition, a single Flow
 * Action may signal events in any existing/restored FlowExecutions.
 * <p>
 * Requests are managed by and delegated to a {@link FlowExecutionManager},
 * allowing reuse of common front flow controller logic in other environments.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * <li>To have this controller launch a new flow execution (conversation), have
 * the client send a
 * {@link FlowExecutionManagerParameterExtractor#getFlowIdParameterName()}
 * request parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutionManagerParameterExtractor#getFlowExecutionIdParameterName()}
 * request parameter identifying the conversation to participate in.
 * <p>
 * On each request received by this action, a {@link StrutsExternalContext}
 * object is created as input to the web flow system. This external source event
 * provides access to the action form, action mapping, and other struts-specific
 * constructs.
 * <p>
 * This class also is aware of the <code>SpringBindingActionForm</code>
 * adapter, which adapts Spring's data binding infrastructure (based on POJO
 * binding, a standard Errors interface, and property editor type conversion) to
 * the Struts action form model. This option gives backend web-tier developers
 * full support for POJO-based binding with minimal hassel, while still
 * providing consistency to view developers who already have a lot of experience
 * with Struts for markup and request dispatching.
 * <p>
 * Below is an example <code>struts-config.xml</code> configuration for a
 * FlowAction:
 * 
 * <pre>
 *      &lt;action path=&quot;/userRegistration&quot;
 *          type=&quot;org.springframework.webflow.struts.FlowAction&quot;
 *          name=&quot;springBindingActionForm&quot; scope=&quot;request&quot;&gt;
 *      &lt;/action&gt;
 * </pre>
 * 
 * This example associates the logical request URL
 * <code>/userRegistration.do</code> as a Flow controller. It is expected that
 * flows to launch be provided in a dynamic fashion by the views (allowing this
 * single <code>FlowAction</code> to manage any number of flow executions). A
 * Spring binding action form instance is set in request scope, acting as an
 * adapter enabling POJO-based binding and validation with Spring.
 * <p>
 * Other notes regarding Struts web-flow integration:
 * <ul>
 * <li>Logical view names returned when <code>ViewStates</code> and
 * <code>EndStates</code> are entered are mapped to physical view templates
 * using standard Struts action forwards (typically global forwards).
 * <li>Use of the <code>SpringBindingActionForm</code> requires no special
 * setup in <code>struts-config.xml</code>: simply declare a form bean in
 * request scope of the class
 * <code>org.springframework.web.struts.SpringBindingActionForm</code> and use
 * it with your FlowAction.
 * </ul>
 * <p>
 * The benefits here are substantial: developers now have a powerful web flow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * 
 * @see org.springframework.webflow.manager.FlowExecutionManager
 * @see org.springframework.web.struts.SpringBindingActionForm
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends ActionSupport {

	/**
	 * The flow execution manager will be retreived from the application context
	 * using this bean name if no manager is explicitly set.
	 */
	public static final String FLOW_EXECUTION_MANAGER_BEAN_NAME = "flowExecutionManager";

	/**
	 * The flow execution manager parameter extractor will be retreived from the
	 * application context using this bean name if no extractor is explicitly
	 * set.
	 */
	public static final String PARAMETER_EXTRACTOR_BEAN_NAME = "flowExecutionManagerParameterExtractor";

	/**
	 * The manager responsible for launching and signaling struts-originating
	 * events in flow executions.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * Delegate for extract flow execution manager parameters.
	 */
	private FlowExecutionManagerParameterExtractor parameterExtractor;

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the flow execution manager
	 */
	public FlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 */
	public void setFlowExecutionManager(FlowExecutionManager flowExecutionManager) {
		this.flowExecutionManager = flowExecutionManager;
	}

	/**
	 * Returns the flow execution manager parameter extractor used by this
	 * controller.
	 * @return the parameter extractor
	 */
	public FlowExecutionManagerParameterExtractor getParameterExtractor() {
		return parameterExtractor;
	}

	/**
	 * Sets the flow execution manager parameter extractor to use.
	 * @param parameterExtractor the parameter extractor
	 */
	public void setParameterExtractor(FlowExecutionManagerParameterExtractor parameterExtractor) {
		this.parameterExtractor = parameterExtractor;
	}

	protected void onInit() {
		if (getFlowExecutionManager() == null) {
			setFlowExecutionManager((FlowExecutionManager)getWebApplicationContext().getBean(
					FLOW_EXECUTION_MANAGER_BEAN_NAME, FlowExecutionManager.class));
		}
		if (getParameterExtractor() == null) {
			setParameterExtractor((FlowExecutionManagerParameterExtractor)getWebApplicationContext().getBean(
					PARAMETER_EXTRACTOR_BEAN_NAME, FlowExecutionManagerParameterExtractor.class));
		}
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ExternalContext context = new StrutsExternalContext(mapping, form, request, response);
		ViewSelection selectedView = createControllerHelper().handleFlowRequest(context);
		return toActionForward(selectedView, mapping, request);
	}

	/**
	 * Factory method that creates a new helper for processing a request into
	 * this flow controller.
	 * @return the controller helper
	 */
	protected FlowExecutionManagerHelper createControllerHelper() {
		return new FlowExecutionManagerHelper(getFlowExecutionManager(), getParameterExtractor());
	}

	/**
	 * Return a Struts ActionForward given a ViewSelection. Adds all attributes
	 * from the ViewSelection as request attributes.
	 */
	protected ActionForward toActionForward(ViewSelection selectedView, ActionMapping mapping,
			HttpServletRequest request) {
		if (selectedView != null) {
			WebUtils.exposeRequestAttributes(request, selectedView.getModel());
			ActionForward forward = mapping.findForward(selectedView.getViewName());
			if (forward != null) {
				// the 1.2.1 copy constructor would ideally be better to use,
				// but it is not Struts 1.1 compatible
				forward = new ActionForward(forward.getName(), forward.getPath(), selectedView.isRedirect());
			}
			else {
				if (selectedView.isRedirect()) {
					forward = new ActionForward(buildRedirectUrlPath(selectedView), true);
				}
				else {
					forward = new ActionForward(selectedView.getViewName(), false);
				}
			}
			forward.freeze();
			return forward;
		}
		else {
			return null;
		}
	}

	/**
	 * Takes the view name of the selected view and appends the model properties
	 * as query parameters.
	 * @param selectedView the selected view
	 * @return the relative url path to redirect to
	 */
	protected String buildRedirectUrlPath(ViewSelection selectedView) {
		StringBuffer path = new StringBuffer(selectedView.getViewName());
		if (selectedView.getModel().size() > 0) {
			// append model attributes as redirect query parameters
			path.append('?');
			Iterator it = selectedView.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				path.append(entry.getKey()).append('=').append(entry.getValue());
				if (it.hasNext()) {
					path.append('&');
				}
			}
		}
		return path.toString();
	}
}