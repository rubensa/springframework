/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface MethodBeforeAdvice extends BeforeAdvice {
	
	void before(Method m, Object[] args, Object target) throws Throwable;

}
