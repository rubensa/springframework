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

import java.util.Collections;
import java.util.Map;

import org.springframework.util.ObjectUtils;
import org.springframework.webflow.ViewSelection;

/**
 * Requests a forward to a local, internal application view resource, such as a
 * JSP, Velocity, or Freemarker template.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class ApplicationViewSelection extends ViewSelection {

	/**
	 * The name of the view (or page or other response) to render. This name
	 * may identify a <i>logical</i> view resource or may be a <i>physical</i>
	 * path to an internal view template.
	 */
	private final String viewName;

	/**
	 * A map of the application data available to the view for rendering.
	 */
	private final Map model;

	/**
	 * Creates a new forward to the given view name.
	 * @param viewName the name of the view to forward to for rendering.
	 * @param model the map of application model data available to the view
	 * during rendering; map of model names (Strings) to model objects
	 * (Objects), model entries may not be null, but the model Map may be null
	 * if there is no model data
	 */
	public ApplicationViewSelection(String viewName, Map model) {
		if (model == null) {
			model = Collections.EMPTY_MAP;
		}
		this.viewName = viewName;
		this.model = model;
	}

	/**
	 * Returns the name of the view to render.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Return the view's application model map. Never returns null.
	 */
	public Map getModel() {
		return Collections.unmodifiableMap(model);
	}

	public boolean equals(Object o) {
		if (!(o instanceof ApplicationViewSelection)) {
			return false;
		}
		ApplicationViewSelection other = (ApplicationViewSelection)o;
		return ObjectUtils.nullSafeEquals(viewName, other.viewName) && model.equals(other.model);
	}

	public int hashCode() {
		return (viewName != null ? viewName.hashCode() : 0) + model.hashCode();
	}
}