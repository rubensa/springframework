/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Simple MethodInvocationFactory implementation that 
 * constructs a new MethodInvocationImpl on every call.
 * @author Rod Johnson
 * @version $Id$
 */
public class SimpleMethodInvocationFactory implements MethodInvocationFactory {

	/**
	 * @see org.springframework.aop.framework.MethodInvocationFactory#getMethodInvocation(org.springframework.aop.framework.Advised, java.util.List, java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public MethodInvocation getMethodInvocation(Advised advised, Object proxy, Method method, Class targetClass, Object[] args, List interceptorsAndDynamicInterceptionAdvice) {
		return new MethodInvocationImpl(
			proxy,
			advised.getTarget(),
			method.getDeclaringClass(),
			method,
			args,
			targetClass,
			interceptorsAndDynamicInterceptionAdvice);
	}
	
	public void release(MethodInvocation invocation) {
		// Not necessary to implement for this implementation
		//	TODO move into AOP Alliance
		//((MethodInvocationImpl) invocation).clear();
	}

}
