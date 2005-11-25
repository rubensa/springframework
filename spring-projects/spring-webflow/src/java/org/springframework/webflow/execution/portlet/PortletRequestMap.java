/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.webflow.execution.portlet;

import java.util.Enumeration;

import javax.portlet.PortletRequest;

import org.springframework.webflow.util.StringKeyedAttributeMapAdapter;

/**
 * Map backed by the Portlet request, for accessing request scoped attributes.
 * 
 * @author Keith Donald
 */
public class PortletRequestMap extends StringKeyedAttributeMapAdapter {

	/**
	 * The wrapped portlet request.
	 */
	private PortletRequest request;

	/**
	 * Create a new map wrapping the attributes of given portlet request.
	 */
	public PortletRequestMap(PortletRequest request) {
		this.request = request;
	}

	protected Object getAttribute(String key) {
		return request.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		request.setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		request.removeAttribute(key);
	}

	protected Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}
}