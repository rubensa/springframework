/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * Object representing the override of a method on a managed
 * object by the IoC container.
 * Note that the override mechanism is <i>not</i> intended
 * as a generic means of inserting crosscutting code:
 * use AOP for that.
 * @author Rod Johnson
 * @version $Id$
 */
public abstract class MethodOverride {
	
	private final String methodName;
	
	protected MethodOverride(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the name of the method to be overriden
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Subclasses must override this to indicate whether they
	 * match the given method. This allows for argument list checking
	 * as well as method name checking.
	 * @param m method to check
	 * @return whether this override matches the given method
	 */
	public abstract boolean matches(Method m);
}
