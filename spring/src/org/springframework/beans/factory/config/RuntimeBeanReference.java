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

package org.springframework.beans.factory.config;

import org.springframework.util.Assert;

/** 
 * Immutable placeholder class used for a property value object when it's
 * a reference to another bean in this factory to be resolved at runtime.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public class RuntimeBeanReference {
	
	private final String beanName;

	private final boolean toParent;


	/**
	 * Create a new RuntimeBeanReference to the given bean name,
	 * without explicitly marking it as reference to a bean in
	 * the parent factory.
	 * @param beanName name of the target bean
	 */
	public RuntimeBeanReference(String beanName) {
		this(beanName, false);
	}

	/**
	 * Create a new RuntimeBeanReference to the given bean name,
	 * with the option to mark it as reference to a bean in
	 * the parent factory.
	 * @param beanName name of the target bean
	 * @param toParent whether this is an explicit reference to
	 * a bean in the parent factory
	 */
	public RuntimeBeanReference(String beanName, boolean toParent) {
		Assert.hasText(beanName, "Bean name must not be empty");
		this.beanName = beanName;
		this.toParent = toParent;
	}


	/**
	 * Return the target bean name.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Return whether this is an explicit reference to a bean
	 * in the parent factory.
	 */
	public boolean isToParent() {
		return toParent;
	}


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RuntimeBeanReference)) {
			return false;
		}
		RuntimeBeanReference that = (RuntimeBeanReference) other;
		return (this.beanName.equals(that.beanName) && this.toParent == that.toParent);
	}

	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}

	public String toString() {
	   return '<' + getBeanName() + '>';
	}

}
