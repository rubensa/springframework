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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.util.WebUtils;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.ExternalContext.SharedMap;
import org.springframework.webflow.context.StringKeyedMapAdapter;

/**
 * Map backed by the Servlet HTTP session, for accessing session scoped
 * attributes.
 * 
 * @author Keith Donald
 */
public class HttpSessionMap extends StringKeyedMapAdapter implements SharedMap {

	/**
	 * The wrapped http request, providing access to the session.
	 */
	private HttpServletRequest request;

	/**
	 * Create a map wrapping the session of given request.
	 */
	public HttpSessionMap(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Internal helper to get the HTTP session associated with the wrapped
	 * request, or null if there is no such session.
	 */
	private HttpSession getSession() {
		return request.getSession(false);
	}

	protected Object getAttribute(String key) {
		HttpSession session = getSession();
		return (session == null) ? null : session.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		request.getSession(true).setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		HttpSession session = getSession();
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	protected Iterator getAttributeNames() {
		HttpSession session = getSession();
		return session == null ? CollectionUtils.EMPTY_ITERATOR : CollectionUtils.iterator(session.getAttributeNames());
	}

	public Object getMutex() {
		HttpSession session = request.getSession(true);
		Object mutex = session.getAttribute(WebUtils.SESSION_MUTEX_ATTRIBUTE);
		return mutex != null ? mutex : session;
	}
}