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
package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Simple view selector that makes an selection with the same view name each
 * time. This producer will make all model data from both flow and request scope
 * available to the view.
 * <p>
 * Also supports setting a <i>requestConversationRedirect</i> flag that will
 * trigger a redirect to the ViewSelection made by this selector at a
 * bookmarkable conversation URL.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SimpleViewSelector implements ViewSelector, Serializable {

	/**
	 * The static view name to render.
	 */
	private String viewName;

	/**
	 * A flag indicating if a "redirect to conversation" should be requested.
	 * <p>
	 * Setting this to true allows you to redirect while the flow is in progress
	 * to a stable "conversation URL" that can be safely refreshed.
	 */
	private boolean requestConversationRedirect;

	/**
	 * Creates a view descriptor creator that will produce view descriptors
	 * requesting that the specified view is rendered.
	 * @param viewName the view name
	 */
	public SimpleViewSelector(String viewName) {
		setViewName(viewName);
	}

	/**
	 * Creates a view descriptor creator that will produce view descriptors
	 * requesting that the specified view is rendered.
	 * @param viewName the view name
	 */
	public SimpleViewSelector(String viewName, boolean requestConversationRedirect) {
		setViewName(viewName);
		setRequestConversationRedirect(requestConversationRedirect);
	}

	/**
	 * Returns the name of the view that should be rendered.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Set the name of the view that should be rendered.
	 */
	public void setViewName(String viewName) {
		Assert.hasText(viewName, "The view name is required");
		this.viewName = viewName;
	}

	/**
	 * Returns whether or not this view selection will request a "redirect to
	 * conversation".
	 */
	public boolean isRequestConversationRedirect() {
		return requestConversationRedirect;
	}

	/**
	 * Sets whether or not this view selection will request a "redirect to
	 * conversation".
	 */
	public void setRequestConversationRedirect(boolean requestConversationRedirect) {
		this.requestConversationRedirect = requestConversationRedirect;
	}

	public ViewSelection makeSelection(RequestContext context) {
		return new ViewSelection(getViewName(), context.getModel(), isRequestConversationRedirect());
	}

	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).append("requestConversationRedirect",
				requestConversationRedirect).toString();
	}
}