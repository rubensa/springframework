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
 *
 * Created on 16-Feb-2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj;

import org.springframework.core.Ordered;

/**
 * Interface to be implemented by types that can supply the information
 * needed to sort advice/advisors by AspectJ's precedence rules.
 * 
 * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceAwareOrderComparator
 * @author Adrian Colyer
 * @since 2.0
 */
public interface AspectJPrecedenceInformation extends Ordered {
	
	// Implementation note:
	// We need the level of indirection this interface provides as otherwise the
	// AspectJPrecedenceAwareOrderComparator must ask an Advisor for its Advice in
	// all cases in order to sort advisors. This causes problems with the 
	// InstantiationModelAwarePointcutAdvisor which needs to delay creating its advice
	// for aspects with non-singleton instantiation models.
	
	/**
	 * The name of the aspect (bean) in which the advice was declared.
	 */
	String getAspectName();
	
	/**
	 * The declaration order of the advice member within the aspect
	 */
	int getDeclarationOrder();
	
	boolean isBeforeAdvice();
	
	boolean isAfterAdvice();
}
