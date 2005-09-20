/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.web.filter;

import org.springframework.aop.target.scope.ScopeIdentifierResolver;
import org.springframework.web.util.RequestHolder;

/**
 * Retrieves the HTTP session bound to the current thread. See 
 * {@link org.springframework.web.filter.RequestBindingFilter} and
 * {@link org.springframework.web.filter.RequestBindingHandlerInterceptor} on
 * how to bind the HTTP session to the current thread.
 * 
 * @see org.springframework.web.filter.RequestBindingFilter
 * @see org.springframework.web.filter.RequestBindingHandlerInterceptor
 * @author Steven Devijver
 * @since 1.3
 */
public class ThreadBoundHttpSessionScopeIdentifierResolver implements
		ScopeIdentifierResolver {


	public Object getScopeIdentifier() throws IllegalStateException {
		if (RequestHolder.currentRequest() == null) {
			throw new IllegalStateException("HTTP request is not bound to the current thread!");
		}
		return RequestHolder.currentSession();
	}

}
