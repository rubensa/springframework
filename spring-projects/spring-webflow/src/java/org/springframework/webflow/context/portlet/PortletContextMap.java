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
package org.springframework.webflow.context.portlet;

import java.util.Iterator;

import javax.portlet.PortletContext;

import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.SharedMap;
import org.springframework.webflow.context.StringKeyedMapAdapter;

/**
 * Map backed by the Portlet context, for accessing application scoped
 * attributes.
 * 
 * @author Keith Donald
 */
public class PortletContextMap extends StringKeyedMapAdapter implements SharedMap {

	/**
	 * The wrapped portlet context.
	 */
	private PortletContext context;

	/**
	 * Create a new map wrapping given portlet context.
	 */
	public PortletContextMap(PortletContext context) {
		this.context = context;
	}

	protected Object getAttribute(String key) {
		return context.getAttribute(key);
	}

	protected void setAttribute(String key, Object value) {
		context.setAttribute(key, value);
	}

	protected void removeAttribute(String key) {
		context.removeAttribute(key);
	}

	protected Iterator getAttributeNames() {
		return CollectionUtils.iterator(context.getAttributeNames());
	}

	public Object getMutex() {
		return context;
	}
}