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
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for solicting form input.
 * 
 * @author Keith Donald
 */
public class ViewState extends TransitionableState {

	protected static final String VIEW_SUFFIX = "View";

	/**
	 * The logical name of the view.
	 */
	private String viewName;

	public ViewState(String id) {
		super(id);
	}
	
	public ViewState(String id, String viewName) {
		super(id);
		setViewName(viewName);
	}

	public ViewState(String id, String viewName, Transition transition) {
		super(id, transition);
		setViewName(viewName);
	}

	public ViewState(String id, String viewName, Transition[] transitions) {
		super(id, transitions);
		setViewName(viewName);
	}

	public ViewState(Flow flow, String id) {
		super(flow, id);
	}

	public ViewState(Flow flow, String id, String viewName) {
		super(flow, id);
		setViewName(viewName);
	}

	public ViewState(Flow flow, String id, Transition transition) {
		super(flow, id, transition);
	}

	public ViewState(Flow flow, String id, Transition[] transitions) {
		super(flow, id, transitions);
	}

	public ViewState(Flow flow, String id, String viewName, Transition transition) {
		super(flow, id, transition);
		setViewName(viewName);
	}

	public ViewState(Flow flow, String id, String viewName, Transition[] transitions) {
		super(flow, id, transitions);
		setViewName(viewName);
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public boolean isViewState() {
		return true;
	}

	/**
	 * Return a view descriptor pointing requesting front controllers to a
	 * logical view resource to be displayed. The descriptor also contains a
	 * model map needed when the view is rendered, for populating dynamic
	 * content.
	 * 
	 * @param sessionExecutionStack The session execution stack, tracking the
	 *        current active flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected ViewDescriptor doEnterState(FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.hasText(getViewName())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning view name '" + viewName + "' to render");
			}
			return new ViewDescriptor(viewName, sessionExecution.getAttributes());
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning a view descriptor null object; no view to render");
			}
			return ViewDescriptor.NULL_OBJECT;
		}
	}

	protected void createToString(ToStringCreator creator) {
		super.createToString(creator);
		creator.append("viewName", viewName);
	}
}