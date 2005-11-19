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

package org.springframework.web.portlet.handler;

import javax.portlet.PortletRequest;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.web.portlet.HandlerExecutionChain;
import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.HandlerMapping;

/**
 * Abstract base class for HandlerMapping implementations.
 * Supports ordering, a default handler, and handler interceptors.
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 1.3
 * @see #getHandlerInternal
 * @see org.springframework.web.portlet.HandlerInterceptor
 */
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport
    implements HandlerMapping, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Object defaultHandler;

	private HandlerInterceptor[] interceptors;


	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the default handler for this handler mapping.
	 * This handler will be returned if no specific mapping was found.
	 * <p>Default is <code>null</code>, indicating no default handler.
	 * @param defaultHandler default handler instance, or null if none
	 */
	public final void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
		if (logger.isInfoEnabled()) {
			logger.info("Default mapping to handler [" + this.defaultHandler + "]");
		}
	}

	/**
	 * Return the default handler for this handler mapping.
	 * @return the default handler instance, or null if none
	 */
	protected final Object getDefaultHandler() {
		return defaultHandler;
	}

	/**
	 * Set the handler interceptors to apply for all handlers mapped by
	 * this handler mapping.
	 * @param interceptors array of handler interceptors, or null if none
	 */
	public final void setInterceptors(HandlerInterceptor[] interceptors) {
		this.interceptors = interceptors;
	}


	/**
	 * Look up a handler for the given request, falling back to the default
	 * handler if no specific one is found.
	 * @param request current HTTP request
	 * @return the looked up handler instance, or the default handler
	 * @see #getHandlerInternal
	 */
	public final HandlerExecutionChain getHandler(PortletRequest request) throws Exception {
		Object handler = getHandlerInternal(request);
		if (handler == null) {
			handler = this.defaultHandler;
		}
		if (handler == null) {
			return null;
		}
		// bean name of resolved handler?
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		return new HandlerExecutionChain(handler, this.interceptors);
	}

	/**
	 * Lookup a handler for the given request, returning null if no specific
	 * one is found. This method is called by getHandler, a null return value
	 * will lead to the default handler, if one is set.
	 * @param request current portlet request
	 * @return the looked up handler instance, or null
	 * @throws Exception if there is an internal error
	 */
	protected abstract Object getHandlerInternal(PortletRequest request) throws Exception;

}
