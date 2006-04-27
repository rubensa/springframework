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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.FlowVariable;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * A concrete flow variable subclass that obtains variable values from a Spring
 * {@link BeanFactory}.
 * 
 * @author Keith Donald
 */
public class BeanFactoryFlowVariable extends FlowVariable {

	/**
	 * The name of the bean factory whose value will be used as the flow
	 * variable.  The bean should be a prototype.
	 */
	private String beanName;

	/**
	 * The bean factory where initial variable values will be obtained.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a new flow variable.
	 * @param variableName the variable name
	 * @param scope the variable scope
	 * @param beanName the bean name
	 * @param beanFactory the bean factory where initial variable values will be
	 * obtained
	 */
	public BeanFactoryFlowVariable(String variableName, ScopeType scope, String beanName, BeanFactory beanFactory) {
		super(variableName, scope);
		if (StringUtils.hasText(beanName)) {
			this.beanName = beanName;
		}
		else {
			this.beanName = variableName;
		}
		Assert.notNull(beanFactory, "The variable bean factory is required");
		Assert.isTrue(!beanFactory.isSingleton(this.beanName), "The variable bean with name '" + this.beanName
				+ "' must be a prototype (singleton=false)");
		this.beanFactory = beanFactory;
	}

	protected Object createVariableValue(RequestContext context) {
		return beanFactory.getBean(beanName);
	}
}