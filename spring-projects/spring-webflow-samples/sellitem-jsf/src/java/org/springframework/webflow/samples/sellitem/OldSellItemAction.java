/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.FormAction;

public class OldSellItemAction extends FormAction {

	public OldSellItemAction() {
		setFormObjectName("sale");
		setFormObjectClass(Sale.class);
		setFormObjectScope(ScopeType.FLOW);
	}

	/**
	 * Hook method that returns a boolean telling the form action whether
	 * validation should occur in the context of the current request.
	 * <p>
	 * Here it should only happen if and only if a 'validatorMethod' property
	 * was specified in the calling action state.
	 */
	protected boolean validationEnabled(RequestContext context) {
		return context.getProperties().containsAttribute(VALIDATOR_METHOD_PROPERTY);
	}
}