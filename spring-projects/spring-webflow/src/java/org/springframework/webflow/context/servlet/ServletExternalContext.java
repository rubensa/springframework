/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.context.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ParameterMap;
import org.springframework.webflow.SharedAttributeMap;

/**
 * Provides contextual information about an HTTP Servlet environment that has
 * interacted with SWF.
 * 
 * @author Keith Donald
 */
public class ServletExternalContext implements ExternalContext {

	/**
	 * The context.
	 */
	private ServletContext context;

	/**
	 * The request.
	 */
	private HttpServletRequest request;

	/**
	 * The response.
	 */
	private HttpServletResponse response;

	/**
	 * Create a new external context wrapping given servlet HTTP request and
	 * response.
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	public ServletExternalContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
	}

	public String getContextPath() {
		return request.getContextPath();
	}
	
	public String getDispatcherPath() {
		return request.getServletPath();
	}

	public String getRequestPathInfo() {
		return request.getPathInfo();
	}

	public ParameterMap getRequestParameterMap() {
		return new ParameterMap(new HttpServletRequestParameterMap(request));
	}

	public AttributeMap getRequestMap() {
		return new AttributeMap(new HttpServletRequestMap(request));
	}

	public SharedAttributeMap getSessionMap() {
		return new SharedAttributeMap(new HttpSessionMap(request));
	}

	public SharedAttributeMap getApplicationMap() {
		return new SharedAttributeMap(new HttpServletContextMap(context));
	}

	/**
	 * Return the wrapped HTTP servlet context.
	 */
	public ServletContext getContext() {
		return context;
	}

	/**
	 * Return the wrapped HTTP servlet request.
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Return the wrapped HTTP servlet response.
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	public String toString() {
		return new ToStringCreator(this).append("requestParameterMap", getRequestParameterMap()).toString();
	}
}