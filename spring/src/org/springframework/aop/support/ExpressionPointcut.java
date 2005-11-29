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

package org.springframework.aop.support;

import org.springframework.aop.Pointcut;

/**
 * Interface to be implemented by pointcuts that use String expressions.
 * @author Rob Harrop
 * @since 1.3
 */
public interface ExpressionPointcut extends Pointcut {
	
	/**
	 * Return the String expression for this pointcut
	 * @return the String expression for this pointcut
	 */
	String getExpression();
}
