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

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.RequestHolder;

import junit.framework.TestCase;

public class HttpRequestAndSessionScopeIdentifierResolverTests extends TestCase {

	public void testRetrievingThreadBoundRequest() {
		HttpServletRequest request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		Object result = new ThreadBoundHttpRequestScopeIdentifierResolver().getScopeIdentifier();
		assertSame(result, request);
		RequestHolder.clear();
	}
	
	public void testRetrievingThreadBoundSession() {
		HttpServletRequest request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		Object result = new ThreadBoundHttpSessionScopeIdentifierResolver().getScopeIdentifier();
		assertSame(result, request.getSession());
		RequestHolder.clear();
	}
}
