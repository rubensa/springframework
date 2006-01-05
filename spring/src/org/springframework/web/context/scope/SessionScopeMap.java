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

package org.springframework.web.context.scope;

import org.springframework.aop.target.scope.ScopeMap;

/**
 * Session-backed ScopeMap implementation. Relies on a thread-bound
 * RequestAttributes instance, which can be exported through
 * RequestContextListener, RequestContextFilter or DispatcherServlet.
 *
 * <p>This ScopeMap will also work for Portlet environments,
 * through an alternate RequestAttributes implementation
 * (as exposed out-of-the-box by Spring's DispatcherPortlet).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see RequestContextHolder#currentRequestAttributes()
 * @see RequestAttributes#SCOPE_SESSION
 * @see RequestAttributes#SCOPE_GLOBAL_SESSION
 * @see RequestContextListener
 * @see org.springframework.web.filter.RequestContextFilter
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.portlet.DispatcherPortlet
 */
public class SessionScopeMap implements ScopeMap {

	private int scope = RequestAttributes.SCOPE_SESSION;


	/**
	 * Set whether to store attributes in the global session,
	 * provided that such a distinction is available.
	 * <p>Default is "false:" Do not store in a global session
	 * but rather prefer a locally isolated session.
	 * <p>This distinction is important for Portlet environments,
	 * where there are two notions of a session: "portlet scope"
	 * and "application scope". If this flag is on, objects will
	 * be put into the "application scope" session; else they
	 * will end up in the "portlet scope" session (the default).
	 * <p>In a Servlet environment, this flag is effectively ignored.
	 * @see org.springframework.web.portlet.context.PortletRequestAttributes
	 * @see ServletRequestAttributes
	 */
	public void setGlobalSession(boolean globalSession) {
		this.scope = (globalSession ? RequestAttributes.SCOPE_GLOBAL_SESSION : RequestAttributes.SCOPE_SESSION);
	}


	public boolean isPersistent() {
		return false;
	}

	public Object get(String name) {
		return RequestContextHolder.currentRequestAttributes().getAttribute(name, this.scope);
	}

	public void put(String name, Object value) {
		RequestContextHolder.currentRequestAttributes().setAttribute(name, value, this.scope);
	}

	public void remove(String name) {
		RequestContextHolder.currentRequestAttributes().removeAttribute(name, this.scope);
	}

}
