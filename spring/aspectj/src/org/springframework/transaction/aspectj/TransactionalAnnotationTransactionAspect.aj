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

package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete AspectJ transaction aspect using Java 5 annotations, and Spring
 * transactional annotation. 
 * <p>
 * validation.
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Adrian Colyer
 * @since 2.0
 * @see AbstractTransactionAspect
 */
public aspect TransactionalAnnotationTransactionAspect extends AbstractTransactionAspect {
	
	public TransactionalAnnotationTransactionAspect() {
		super(new AnnotationTransactionAttributeSource());
	}
	
	/**
	 * Matches the execution of any public method in a type with the
	 * Transactional annotation, or any subtype of a type with the
	 * Transactional annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtTransactionalType() :
		execution(public * (@Transactional *+).*(..));
	
	/**
	 * Matches the execution of any public method with the 
	 * Transactional annotation.
	 */
	private pointcut executionOfTransactionalPublicMethod() :
		execution(public * *(..)) && @annotation(Transactional);
	
	/**
	 * Definition of pointcut from super aspect - matched join points
	 * will have Spring transaction management applied.
	 */	
	protected pointcut transactionalMethodExecution(Object txObject) :
		(executionOfAnyPublicMethodInAtTransactionalType() 
		 || executionOfTransactionalPublicMethod())
		&& this(txObject);

}
