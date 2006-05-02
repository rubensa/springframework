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
package org.springframework.webflow.support;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.ViewSelection;

/**
 * Concrete response type that requests a redirect to an external URL outside of
 * Spring Web Flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class ExternalRedirect extends ViewSelection {

	/**
	 * The arbitrary url path to redirect to.
	 */
	private final String url;

	/**
	 * A flag indicating if the redirect URL is context relative.
	 * <p>
	 * The default is "false": A URL that starts with a slash will be interpreted
	 * as absolute, i.e. taken as-is. If true, the context path will be
	 * prepended to the URL in such a case. 
	 */
	private final boolean contextRelative;

	/**
	 * Creates an external redirect request.
	 * @param url the url path to redirect to
	 * @param whether the url should be treated as context relative
	 */
	public ExternalRedirect(String url, boolean contextRelative) {
		Assert.notNull(url, "The external URL to redirect to is required");
		this.url = url;
		this.contextRelative = contextRelative;
	}
	
	/**
	 * Returns the external URL to redirect to.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the flag indicating if the external URL is context (application) relative.
	 */
	public boolean isContextRelative() {
		return contextRelative;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ExternalRedirect)) {
			return false;
		}
		ExternalRedirect other = (ExternalRedirect)o;
		return url.equals(other.url) && contextRelative == other.contextRelative;
	}

	public int hashCode() {
		return url.hashCode() + (contextRelative ? 1 : 0) * 29;
	}

	public String toString() {
		return new ToStringCreator(this).append("url", url).append("contextRelative", contextRelative).toString();
	}
}