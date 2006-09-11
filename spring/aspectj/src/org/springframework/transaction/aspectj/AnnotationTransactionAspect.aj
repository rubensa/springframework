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

package org.springframework.transaction.aspectj;

import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete AspectJ transaction aspect using Spring Transactional annotation
 * for JDK 1.5+.
 * 
 * <p>
 * When using this aspect, you <i>must</i> annotate the implementation class 
 * (and/or methods within that class), <i>not</i> the interface (if any) that
 * the class implements. AspectJ follows Java's rule that annotations on 
 * interfaces are <i>not</i> inherited.
 * </p>
 * <p>A @Transactional annotation on a class specifies the default transaction
 * semantics for the execution of any <b>public</b> operation in the class.</p>
 * <p>A @Transactional annotation on a method within the class overrides the
 * default transaction semantics given by the class annotation (if present). 
 * Methods with public, protected, and default visibility may all be annotated.
 * Annotating protected and default visibility methods directly is the only way
 * to get transaction demarcation for the execution of such operations.</p> 
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.transaction.annotation.Transactional
 */
public aspect AnnotationTransactionAspect extends AbstractTransactionAspect {
	
	public AnnotationTransactionAspect() {
		super(new AnnotationTransactionAttributeSource());
	}


	/**
	 * Matches the execution of any public method in a type with the
	 * Transactional annotation, or any subtype of a type with the
	 * Transactional annotation.
	 */
	private pointcut executionOfAnyPublicMethodInAtTransactionalType() :
		execution(public * ((@Transactional *)+).*(..));
	
	/**
	 * Matches the execution of any non-private method with the 
	 * Transactional annotation.
	 */
	private pointcut executionOfTransactionalNonPrivateMethod() :
		execution(!private * *(..)) && @annotation(Transactional);
	
	/**
	 * Definition of pointcut from super aspect - matched join points
	 * will have Spring transaction management applied.
	 */	
	protected pointcut transactionalMethodExecution(Object txObject) :
		(executionOfAnyPublicMethodInAtTransactionalType() 
		 || executionOfTransactionalNonPrivateMethod())
		 && this(txObject);

	/**
	 * Annotating private methods with @Transactional has no effect...
	 */
	declare warning 
	    // note, we should be able to say the following in one execution pcd, but
	    // aj doesn't like it...
		: execution(@Transactional * *(..)) &&
		  execution(private * *(..))
		: "@Transactional annotation on private method will be ignored";
}
