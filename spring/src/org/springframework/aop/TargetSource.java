/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface TargetSource {
	
	Class getTargetClass();
	
	//boolean isDynamic();
	
	Object getTarget() throws Exception;
	
	void releaseTarget(Object target) throws Exception;

}
