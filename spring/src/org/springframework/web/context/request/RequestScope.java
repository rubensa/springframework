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

package org.springframework.web.context.request;

/**
 * Request-backed {@link org.springframework.beans.factory.config.Scope}
 * implementation.
 * 
 * <p>Relies on a thread-bound {@link RequestAttributes} instance, which
 * can be exported through {@link RequestContextListener},
 * {@link org.springframework.web.filter.RequestContextFilter} or
 * {@link org.springframework.web.servlet.DispatcherServlet}.
 *
 * <p>This <code>Scope</code> will also work for Portlet environments,
 * through an alternate <code>RequestAttributes</code> implementation
 * (as exposed out-of-the-box by Spring's <code>DispatcherPortlet</code>).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.web.context.request.RequestContextHolder#currentRequestAttributes()
 * @see RequestAttributes#SCOPE_REQUEST
 * @see org.springframework.web.context.request.RequestContextListener
 * @see org.springframework.web.filter.RequestContextFilter
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.portlet.DispatcherPortlet
 */
public class RequestScope extends AbstractRequestAttributesScope {

	protected int getScope() {
		return RequestAttributes.SCOPE_REQUEST;
	}


	/**
	 * There is no conversation id concept for a request, so this method
	 * returns <code>null</code>.
	 * @return <code>null</code> 
	 */
	public String getConversationId() {
		return null;
	}

}
