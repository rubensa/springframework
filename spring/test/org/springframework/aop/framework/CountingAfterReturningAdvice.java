/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

import org.springframework.aop.MethodAfterReturningAdvice;


/**
 * Simple before advice example that we can use for counting checks.
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class CountingAfterReturningAdvice extends MethodCounter implements MethodAfterReturningAdvice {
	public void afterReturning(Object o, Method m, Object[] args, Object target) throws Throwable {
		count(m);
	}
}