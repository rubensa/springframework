/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;

/**
 * Simple subclass of <code>AbstractReflectiveMBeanInfoAssembler</code>
 * that always votes yes for method and property inclusion, effectively exposing
 * all public methods and properties as operations and attributes.
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class SimpleReflectiveMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler {

	/**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return true;
	}

	/**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return true;
	}

  /**
	 * Always returns <code>true</code>.
	 */
	protected boolean includeOperation(Method method, String beanKey) {
		return true;
	}

}
