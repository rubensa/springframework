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

package org.springframework.mock.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mock implementation of the RequestDispatcher interface.
 *
 * <p>Used for testing the web framework; typically not
 * necessary for testing application controllers.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public class MockRequestDispatcher implements RequestDispatcher {

	private final Log logger = LogFactory.getLog(getClass());

	private final String url;

	public MockRequestDispatcher(String url) {
		this.url = url;
	}

	public void forward(ServletRequest servletRequest, ServletResponse servletResponse) {
		if (servletResponse.isCommitted()) {
			throw new IllegalStateException("Cannot perform forward - response is already committed");
		}
		if (!(servletResponse instanceof MockHttpServletResponse)) {
			throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
		}
		((MockHttpServletResponse) servletResponse).setForwardedUrl(this.url);
		if (logger.isDebugEnabled()) {
			logger.debug("MockRequestDispatcher: forwarding to URL [" + this.url + "]");
		}
	}

	public void include(ServletRequest servletRequest, ServletResponse servletResponse) {
		if (!(servletResponse instanceof MockHttpServletResponse)) {
			throw new IllegalArgumentException("MockRequestDispatcher requires MockHttpServletResponse");
		}
		((MockHttpServletResponse) servletResponse).setIncludedUrl(this.url);
		if (logger.isDebugEnabled()) {
			logger.debug("MockRequestDispatcher: including URL [" + this.url + "]");
		}
	}

}
