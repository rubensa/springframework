/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * Superinterface for all Advisors that are driven by a pointcut.
 * This covers nearly all advisors except introduction advisors,
 * for which method-level matching doesn't apply.
 * @author Rod Johnson
 * @version $Id$
 */
public interface PointcutAdvisor extends Advisor {
	
	Pointcut getPointcut();

}
