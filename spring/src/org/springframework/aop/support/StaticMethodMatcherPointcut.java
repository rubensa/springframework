package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass when we want to force subclasses to
 * implement MethodMatcher interface, but subclasses
 * will want to be pointcuts. The getClassFilter() method can
 * be overriden to customize ClassFilter behaviour as well.
 * @author Rod Johnson
 * @version $Id$
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
