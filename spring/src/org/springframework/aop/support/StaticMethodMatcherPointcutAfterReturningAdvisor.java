
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.AfterReturningAdvisor;
import org.springframework.aop.MethodAfterReturningAdvice;

/**
 * Convenient superclass for static method pointcuts that hold a MethodAfterReturningAdvice,
 * making them an Advisor.
 * @author Rod Johnson
 * @version $Id$
 */
public abstract class StaticMethodMatcherPointcutAfterReturningAdvisor extends StaticMethodMatcherPointcutAdvisor implements AfterReturningAdvisor {

	private MethodAfterReturningAdvice afterAdvice;
	
	protected StaticMethodMatcherPointcutAfterReturningAdvisor() {
	}

	protected StaticMethodMatcherPointcutAfterReturningAdvisor(MethodAfterReturningAdvice afterAdvice) {
		this.afterAdvice = afterAdvice;
	}

	public abstract boolean matches(Method m, Class targetClass);
	

	public AfterReturningAdvice getAfterReturningAdvice() {
		return afterAdvice;
	}

}
