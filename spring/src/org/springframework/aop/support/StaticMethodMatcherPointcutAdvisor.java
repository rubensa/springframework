/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for Advisors that are also static pointcuts.
 * @author Rod Johnson
 * @version $Id$
 */
public abstract class StaticMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcut
    implements PointcutAdvisor {

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public Pointcut getPointcut() {
		return this;
	}

}
