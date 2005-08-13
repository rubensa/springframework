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
package org.springframework.webflow.tapestry.binding;

import org.apache.hivemind.Location;
import org.apache.tapestry.IPage;
import org.apache.tapestry.binding.AbstractBinding;
import org.apache.tapestry.coerce.ValueConverter;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.tapestry.Constants;

/**
 * A binding that provides read and write access to an attribute in the
 * flow scope of the active session of the currently executing flow.
 * 
 * @author Keith Donald
 */
public class FlowScopeBinding extends AbstractBinding {

	/**
	 * The page in which this binding object was created; provides 
	 * a context for accessing information about the flow the page is participating in.
	 */
	private IPage page;

	/**
	 * The name of the attribute to read/write from flow scope.
	 */
	private String attributeName;

	/**
	 * Create a new flow scope binding.
	 * @param page the page
	 * @param attributeName the attribute
	 * @param description description
	 * @param valueConverter value converter
	 * @param location the location
	 */
	public FlowScopeBinding(IPage page, String attributeName,
			String description, ValueConverter valueConverter, Location location) {
		super(description, valueConverter, location);
		this.page = page;
		this.attributeName = attributeName;
	}

	public IPage getPage() {
		return page;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Object getObject() {
		return getFlowScope().getAttribute(getAttributeName());
	}

	public void setObject(Object value) {
		getFlowScope().setAttribute(getAttributeName(), value);
	}
	
	protected Scope getFlowScope() {
		FlowExecutionContext context = (FlowExecutionContext) getPage().getProperty(
				Constants.FLOW_EXECUTION_CONTEXT_PAGE_PROPERTY);
		return context.getActiveSession().getScope();
	}
}