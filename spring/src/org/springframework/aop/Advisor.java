/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop;


/**
 * Base interface for advice. InterceptionAdvice and IntroductionAdvice
 * are the allowed subclasses.
 * 
 * @version $Id$
 */
public abstract interface Advisor {
	
	
	// Aspect getAspect();
	
	/**
	 * Is this advice 
	 */
	boolean isPerInstance();

}
