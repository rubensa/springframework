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

package org.springframework.web.portlet.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * Portlet-based implementation of the RequestAttributes interface.
 *
 * <p>Accesses objects from portlet request and portlet session scope,
 * with a distinction between "session" (the PortletSession's "portlet scope")
 * and "global session" (the PortletSession's "application scope").
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.portlet.PortletRequest#getAttribute
 * @see javax.portlet.PortletSession#getAttribute
 * @see javax.portlet.PortletSession#PORTLET_SCOPE
 * @see javax.portlet.PortletSession#APPLICATION_SCOPE
 * @see RequestAttributes#SCOPE_SESSION
 * @see RequestAttributes#SCOPE_GLOBAL_SESSION
 */
public class PortletRequestAttributes extends AbstractRequestAttributes {

	/**
	 * We'll create a lot of these objects, so we don't want a new logger every time.
	 */
	private static final Log logger = LogFactory.getLog(PortletRequestAttributes.class);


	private final PortletRequest request;

	private final Map sessionAttributesToUpdate = new HashMap();

	private final Map globalSessionAttributesToUpdate = new HashMap();


	/**
	 * Create a new PortletRequestAttributes instance for the given request.
	 * @param request current portlet request
	 */
	public PortletRequestAttributes(PortletRequest request) {
		Assert.notNull(request, "Request must not be null");
		this.request = request;
	}

	/**
	 * Expose the PortletRequest that we're wrapping to subclasses.
	 */
	protected final PortletRequest getRequest() {
		return request;
	}


	public Object getAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			return this.request.getAttribute(name);
		}
		else {
			PortletSession session = this.request.getPortletSession(false);
			if (session != null) {
				if (scope == SCOPE_GLOBAL_SESSION) {
					Object value = session.getAttribute(name, PortletSession.APPLICATION_SCOPE);
					if (value != null) {
						this.globalSessionAttributesToUpdate.put(name, value);
					}
					return value;
				}
				else {
					Object value = session.getAttribute(name);
					if (value != null) {
						this.sessionAttributesToUpdate.put(name, value);
					}
					return value;
				}
			}
			else {
				return null;
			}
		}
	}

	public void setAttribute(String name, Object value, int scope) {
		if (scope == SCOPE_REQUEST) {
			this.request.setAttribute(name, value);
		}
		else {
			PortletSession session = this.request.getPortletSession(true);
			if (scope == SCOPE_GLOBAL_SESSION) {
				session.setAttribute(name, value, PortletSession.APPLICATION_SCOPE);
				this.globalSessionAttributesToUpdate.remove(name);
			}
			else {
				session.setAttribute(name, value);
				this.sessionAttributesToUpdate.remove(name);
			}
		}
	}

	public void removeAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			this.request.removeAttribute(name);
			removeRequestDestructionCallback(name);
		}
		else {
			PortletSession session = this.request.getPortletSession(false);
			if (session != null) {
				if (scope == SCOPE_GLOBAL_SESSION) {
					session.removeAttribute(name, PortletSession.APPLICATION_SCOPE);
					this.globalSessionAttributesToUpdate.remove(name);
				}
				else {
					session.removeAttribute(name);
					this.sessionAttributesToUpdate.remove(name);
				}
			}
		}
	}

	public void registerDestructionCallback(String name, Runnable callback, int scope) {
		if (scope == SCOPE_REQUEST) {
			registerRequestDestructionCallback(name, callback);
		}
		else {
			registerSessionDestructionCallback(name, callback);
		}
	}

	public Object getSessionMutex() {
		return PortletUtils.getSessionMutex(this.request.getPortletSession());
	}


	/**
	 * Update all accessed session attributes through <code>session.setAttribute</code>
	 * calls, explicitly indicating to the container that they might have been modified.
	 */
	protected void updateAccessedSessionAttributes() {
		PortletSession session = this.request.getPortletSession(false);
		if (session != null) {
			for (Iterator it = this.sessionAttributesToUpdate.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String name = (String) entry.getKey();
				Object newValue = entry.getValue();
				Object oldValue = session.getAttribute(name);
				if (oldValue == newValue) {
					session.setAttribute(name, newValue);
				}
			}
			for (Iterator it = this.globalSessionAttributesToUpdate.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String name = (String) entry.getKey();
				Object newValue = entry.getValue();
				Object oldValue = session.getAttribute(name, PortletSession.APPLICATION_SCOPE);
				if (oldValue == newValue) {
					session.setAttribute(name, newValue, PortletSession.APPLICATION_SCOPE);
				}
			}
		}
		this.sessionAttributesToUpdate.clear();
		this.globalSessionAttributesToUpdate.clear();
	}

	/**
	 * Register the given callback as to be executed after session termination.
	 * @param name the name of the attribute to register the callback for
	 * @param callback the callback to be executed for destruction
	 */
	private void registerSessionDestructionCallback(String name, Runnable callback) {
		if (logger.isWarnEnabled()) {
			logger.warn("Could not register destruction callback [" + callback + "] for attribute '" + name +
					"' for session scope because Portlet API 1.0 does not support session attribute callbacks");
		}
	}

}
