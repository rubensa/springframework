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
package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;

/**
 * Transition criteria that negates the result of the evaluation of
 * another criteria object.
 * 
 * @author Keith Donald
 */
public class NotTransitionCriteria implements TransitionCriteria, Serializable {

	private TransitionCriteria criteria;
	
	/**
	 * Create a new transition criteria object that will negate
	 * the result of given criteria object.
	 * @param criteria the criteria to negate 
	 */
	public NotTransitionCriteria(TransitionCriteria criteria) {
		Assert.notNull(criteria, "The criteria object to negate is required");
		this.criteria = criteria;
	}
	
	public boolean test(RequestContext context) {
		return !criteria.test(context);
	}
	
	public String toString() {
		return "not(" + criteria + ")";
	}
}