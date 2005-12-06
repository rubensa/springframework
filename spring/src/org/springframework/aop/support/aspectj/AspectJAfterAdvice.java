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
package org.springframework.aop.support.aspectj;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.weaver.tools.PointcutExpression;

/**
 * Spring AOP advice wrapping an AspectJ after advice method
 * @author Rod Johnson
 * @since 1.3
 */
public class AspectJAfterAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
	
	public AspectJAfterAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pe, aif);
	}
	
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		finally {
			invokeAdviceMethod(mi.getArguments());
		}
	}
}