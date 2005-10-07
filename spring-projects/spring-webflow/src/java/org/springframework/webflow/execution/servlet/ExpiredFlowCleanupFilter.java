/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Servlet 2.3 filter that cleans up expired web flow executions in the HTTP
 * session associated with the request being filtered. A flow execution has
 * expired when it has not handled any requests for more than a specified
 * timeout period.
 * <p>
 * This filter can be configured in the <tt>web.xml</tt> deployment descriptor
 * of your web application. Here's an example:
 * 
 * <pre>
 *  &lt;!DOCTYPE web-app PUBLIC &quot;-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN&quot;
 *  	&quot;http://java.sun.com/dtd/web-app_2_3.dtd&quot;&gt;
 *  &lt;web-app&gt;
 *  	&lt;filter&gt;
 *  		&lt;filter-name&gt;flowCleanup&lt;/filter-name&gt;
 *  		&lt;filter-class&gt;org.springframework.webflow.execution.servlet.ExpiredFlowCleanupFilter&lt;/filter-class&gt;
 *  	&lt;/filter&gt;
 *  	&lt;filter-mapping&gt;
 *  		&lt;filter-name&gt;flowCleanup&lt;/filter-name&gt;
 *  		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  	&lt;/filter-mapping&gt;
 *  	...
 * </pre>
 * 
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>timeout</td>
 * <td>10</td>
 * <td>Specifies the flow execution timeout in <b>minutes</b>. If the flow
 * execution is inactive for more than this period of time it will expire and be
 * removed from the HTTP session.</td>
 * </tr>
 * </table>
 * These parameters can be configured using <tt>init-param</tt>
 * values in the deployment descriptor.
 * 
 * @author Erwin Vervaet
 */
public class ExpiredFlowCleanupFilter extends OncePerRequestFilter {

	/**
	 * The default web flow timout: 10 minutes.
	 */
	public static final int DEFAULT_TIMEOUT = 10;

	/**
	 * The time value in minutes.
	 */
	private int timeout = DEFAULT_TIMEOUT;

	/**
	 * Get the flow timout (expiry), expressed in minutes.
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set the flow timout (expiry), expressed in minutes.
	 * @param timeout the timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		doCleanup(request);
		filterChain.doFilter(request, response);
	}

	/**
	 * Remove expired flow executions from the HTTP session associated with
	 * given request.
	 * @param request the request
	 */
	protected void doCleanup(HttpServletRequest request) {
		// get the session if there is one
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		// execute the cleanup process
		Set attributeNamesToRemove = new HashSet();
		Enumeration attributeNames = session.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attributeName = (String)attributeNames.nextElement();
			Object attributeValue = session.getAttribute(attributeName);
			if (attributeValue instanceof FlowExecution) {
				FlowExecution flowExecution = (FlowExecution)attributeValue;
				if (hasExpired(request, flowExecution)) {
					if (logger.isInfoEnabled()) {
						logger.info("Flow execution '" + attributeName
								+ "' for flow '" + flowExecution.getActiveSession().getFlow().getId()
								+ "' has expired and will be removed from the HTTP session '" + session.getId() + "'");
					}
					attributeNamesToRemove.add(attributeName);
				}
			}
		}
		Iterator it = attributeNamesToRemove.iterator();
		while (it.hasNext()) {
			session.removeAttribute((String)it.next());
		}
	}

	/**
	 * Check if given web flow execution, found in the session associated with
	 * given request, has expired.
	 * <p>
	 * Subclasses can override this method if they want to change the expiry
	 * logic, e.g. to keep flow executions alive in certain situations.
	 * @param request current HTTP request
	 * @param flowExecution the web flow execution that needs to be checked for
	 *        expiry
	 */
	protected boolean hasExpired(HttpServletRequest request, FlowExecution flowExecution) {
		return (System.currentTimeMillis() - flowExecution.getLastRequestTimestamp()) > (getTimeout() * 60000);
	}
}