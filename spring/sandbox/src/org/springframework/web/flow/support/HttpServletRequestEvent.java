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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.web.flow.Event;

public class HttpServletRequestEvent extends Event {

	private HttpServletRequest request;

	public HttpServletRequestEvent(HttpServletRequest request) {
		Assert.notNull(request);
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public String getId() {
		return request.getParameter("eventId");
	}

	public String getStateId() {
		return request.getParameter("stateId");
	}

	public Object getParameter(String parameterName) {
		return request.getParameter(parameterName);
	}

	public Map getParameters() {
		return request.getParameterMap();
	}
}