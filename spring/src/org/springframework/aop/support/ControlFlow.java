/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface ControlFlow {
	
	boolean under(Class clazz);
	/**
	 * Matches whole method name
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	boolean under(Class clazz, String methodName);
	
	boolean underToken(String token);
}