/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface AopProxy {
	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the thread context class loader.
	 */
	public abstract Object getProxy();
	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the given class loader.
	 */
	public abstract Object getProxy(ClassLoader cl);
}