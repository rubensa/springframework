/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.AfterReturningAdvisor;
import org.springframework.aop.Pointcut;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class DefaultAfterReturningAdvisor extends AbstractPointcutAdvisor implements AfterReturningAdvisor {
	
	private final AfterReturningAdvice advice;
	
	public DefaultAfterReturningAdvisor(Pointcut pointcut, AfterReturningAdvice advice) {
		super(pointcut);
		this.advice = advice;
	}
	
	public DefaultAfterReturningAdvisor(AfterReturningAdvice advice) {
		this(Pointcut.TRUE, advice);
	}

	/**
	 * @see org.springframework.aop.BeforeAdvisor#getBeforeAdvice()
	 */
	public AfterReturningAdvice getAfterReturningAdvice() {
		return advice;
	}

}
