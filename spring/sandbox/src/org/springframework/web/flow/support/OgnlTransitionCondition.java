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
package org.springframework.web.flow.support;

import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.util.Assert;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class OgnlTransitionCondition implements TransitionCriteria {

	private String expressionString;

	public OgnlTransitionCondition(String expressionString) {
		this.expressionString = expressionString;
	}

	public boolean test(FlowExecutionContext context) {
		try {
			Object result = Ognl.getValue(this.expressionString, context);
			Assert.isInstanceOf(Boolean.class, result);
			return ((Boolean)result).booleanValue();
		}
		catch (OgnlException e) {
			IllegalArgumentException iae = new IllegalArgumentException("Invalid transition expression");
			iae.initCause(e);
			throw iae;
		}
	}
}
