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

package org.springframework.web.portlet.handler;

import java.util.Arrays;
import java.util.Enumeration;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.context.PortletApplicationObjectSupport;

/**
 * Convenient superclass for any kind of web content generator,
 * like AbstractController. Can also be used for custom handlers 
 * that have their own HandlerAdapter.
 *
 * <p>Supports portlet cache control options.
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 * @see org.springframework.web.portlet.mvc.AbstractController
 */
public abstract class PortletContentGenerator extends PortletApplicationObjectSupport {

	private boolean requireSession = false;

	private int cacheSeconds = -1;


	/**
	 * Set whether a session should be required to handle requests.
	 */
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
	}

	/**
	 * Return whether a session is required to handle requests.
	 */
	public final boolean isRequireSession() {
		return requireSession;
	}

	/**
	 * Cache content for the given number of seconds. Default is -1,
	 * indicating no override of portlet content caching.
	 * <p>Only if this is set to 0 (no cache) or a positive value (cache for
	 * this many seconds) will this class override the portlet settings.
	 * <p>The cache setting can be overwritten by subclasses, before content is generated.
	 */
	public final void setCacheSeconds(int seconds) {
		this.cacheSeconds = seconds;
	}

	/**
	 * Return the number of seconds that content is cached.
	 */
	public final int getCacheSeconds() {
		return cacheSeconds;
	}

	/**
	 * Check and prepare the given request and response according to the settings
	 * of this generator. Checks for a required session, and applies the number of 
	 * cache seconds configured for this generator (if it is a render request/reponse).
	 * @param request current portlet request
	 * @param response current portlet response
	 * @throws PortletException if the request cannot be handled because a check failed
	 */
	protected final void checkAndPrepare(
			PortletRequest request, PortletResponse response)
		throws PortletException {
		checkAndPrepare(request, response, this.cacheSeconds);
	}

	/**
	 * Check and prepare the given request and response according to the settings
	 * of this generator. Checks for a required session, and applies the given 
	 * number of cache seconds (if it is a render request/reponse).
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param cacheSeconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 * @throws PortletException if the request cannot be handled because a check failed
	 */
	protected final void checkAndPrepare(
			PortletRequest request, PortletResponse response, int cacheSeconds)
		throws PortletException {

	    // debug output of request
	    if (logger.isDebugEnabled()) {
	        logger.debug("Checking and preparing " + 
	                (request instanceof RenderRequest ? "RenderRequest" : "ActionRequest"));
		    Enumeration params = request.getParameterNames();
	        if (params.hasMoreElements()) logger.debug("Request Parameters:");
		    while (params.hasMoreElements()) {
		        String paramName = (String)params.nextElement();
		        String paramValues[] = request.getParameterValues(paramName);
		        logger.debug("-- '" + paramName + "' = " +
		                (paramValues == null ? "NULL" : Arrays.asList(paramValues).toString()));
		    }	        
	    }
	    
		// check whether session is required
		if (this.requireSession) {
			if (request.getPortletSession(false) == null) {
				throw new PortletSessionRequiredException("Pre-existing session required but none found");
			}
		}

		// Do declarative cache control.
		if (response instanceof RenderResponse)
			applyCacheSeconds((RenderResponse)response, cacheSeconds);
	}

	/**
	 * Prevent the render response from being cached.
	 */
	protected final void preventCaching(RenderResponse response) {
		cacheForSeconds(response, 0);
	}

	/**
	 * Set portlet response to allow caching for the given number of seconds.
	 * @param response current portlet render response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 */
	protected final void cacheForSeconds(RenderResponse response, int seconds) {
		response.setProperty(RenderResponse.EXPIRATION_CACHE, new Integer(seconds).toString());
	}

	/**
	 * Apply the given cache seconds to the render response
	 * @param response current portlet render response
	 * @param seconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 */
	protected final void applyCacheSeconds(RenderResponse response, int seconds) {
		if (seconds > 0) {
			cacheForSeconds(response, seconds);
		}
		else if (seconds == 0) {
			preventCaching(response);
		}
		// Leave caching to the portlet configuration otherwise.
	}

}
