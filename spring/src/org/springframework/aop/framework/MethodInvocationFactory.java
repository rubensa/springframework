/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.util.List;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Factory for method invocations.
 * @author Rod Johnson
 * @version $Id$
 */
public interface MethodInvocationFactory {
	
	MethodInvocation getMethodInvocation(Object proxy, Method method,
								Class targetClass, Object target, Object[] args, 
								List interceptorsAndDynamicInterceptionAdvice, AdvisedSupport advised);
	
	void release(MethodInvocation mi);
	

}
