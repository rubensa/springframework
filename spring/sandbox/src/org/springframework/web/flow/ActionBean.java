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

/**
 * A action that executes controller, command-like behaivior. Action beans
 * typically delegate down to the service-layer to perform business operations,
 * and/or prep views with dynamic model data for rendering.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface ActionBean {

	/**
	 * Execute this action bean.
	 * 
	 * @param request The current http request, enabling access to request
	 *        attributes/parameters if neccessary
	 * @param response The http response, enabling direct response writing by
	 *        the action in neccessary
	 * @param model The data model for the current flow session execution
	 * @return A logical result outcome, called an "ActionBeanEvent"; used as
	 *         grounds for a transition in the current state
	 * @throws RuntimeException An unrecoverable exception occured because of
	 *         programmer error
	 */
	public ActionBeanEvent execute(HttpServletRequest request, HttpServletResponse response,
			MutableAttributesAccessor model) throws RuntimeException;
}