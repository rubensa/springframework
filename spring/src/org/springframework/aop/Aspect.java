/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;



/**
 * Object containing multiple interceptors and pointcuts (advice) together 
 * making up the modularization of an Aspect.
 * <b>Not currently used.</b> 
 * 
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id$
 */
public interface Aspect {
	
	/**
	 * Must not return the empty array or null
	 * @return
	 */
	Advice[] geAdvice();

}
