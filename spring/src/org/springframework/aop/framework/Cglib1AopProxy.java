/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.CodeGenerationException;
import net.sf.cglib.Enhancer;
import net.sf.cglib.MethodInterceptor;
import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

/**
 * InvocationHandler implementation for the Spring AOP framework,
 * based on CGLIB1 proxies.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by a AdvisedSupport implementation. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class can be threadsafe if the
 * underlying (target) class is threadsafe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id$
 * @see java.lang.reflect.Proxy
 * @see net.sf.cglib.Enhancer
 */
class Cglib1AopProxy implements AopProxy, MethodInterceptor {
	
	/**
	 * Invoke the target directly via reflection
	 * @return
	 */
	public static Object invokeJoinpointUsingMethodProxy(Object target, Method m, Object[] args, MethodProxy methodProxy) throws Throwable {
		//	Use reflection to invoke the method
		 try {
			 Object rval = methodProxy.invoke(target, args);
			 return rval;
		 }
		 catch (InvocationTargetException ex) {
			 // Invoked method threw a checked exception. 
			 // We must rethrow it. The client won't see the interceptor
			 Throwable t = ex.getTargetException();
			 throw t;
		 }
		 catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " + m + " on [" + target + "]: " +  ex);
		 }
		 catch (IllegalAccessException ex) {
			 throw new AspectException("Couldn't access method " + m, ex);
		 }
	}
	
	private final Log logger = LogFactory.getLog(getClass());

	/** Config used to configure this proxy */
	private final AdvisedSupport advised;
	
	/**
	 * 
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected Cglib1AopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE)
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		this.advised = config;
	}
	
	
	
	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 *
	 * @see net.sf.cglib.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.MethodProxy)
	 */
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
	
		MethodInvocation invocation = null;
		MethodInvocation oldInvocation = null;
		Object oldProxy = null;
		boolean setInvocationContext = false;
		boolean setProxyContext = false;
	
		TargetSource targetSource = advised.getTargetSource();
		Class targetClass = null;//targetSource.getTargetClass();
		Object target = null;		
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (AopProxyUtils.EQUALS_METHOD.equals(method)) {
				// What if equals throws exception!?

				// This class implements the equals() method itself
				return method.invoke(this, args);
			}
			else if (Advised.class.equals(method.getDeclaringClass())) {
				// Service invocations on ProxyConfig with the proxy config
				return method.invoke(this.advised, args);
			}
			
			Object retVal = null;
			
			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}
		
			List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// Check whether we only have one InvokerInterceptor: that is, no real advice,
			// but just reflective invocation of the target.
			// We can only do this if the Advised config object lets us.
			if (advised.canOptimizeOutEmptyAdviceChain() && 
					chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = invokeJoinpointUsingMethodProxy(target, method, args, methodProxy);
			}
			else {
				// We need to create a method invocation...
				invocation = advised.getMethodInvocationFactory().getMethodInvocation(proxy, method, targetClass, target, args, chain, advised);
			
				if (this.advised.getExposeInvocation()) {
					// Make invocation available if necessary.
					// Save the old value to reset when this method returns
					// so that we don't blow away any existing state
					oldInvocation = AopContext.setCurrentInvocation(invocation);
					// We need to know whether we actually set it, as
					// this block may not have been reached even if exposeInvocation
					// is true
					setInvocationContext = true;
				}
				
				if (this.advised.getExposeProxy()) {
					// Make invocation available if necessary
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				
				// If we get here, we need to create a MethodInvocation
				retVal = invocation.proceed();
				
			}
			
			// Massage return value if necessary
			if (retVal != null && retVal == target) {
				// Special case: it returned "this"
				// Note that we can't help if the target sets
				// a reference to itself in another returned object
				retVal = proxy;
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource
				targetSource.releaseTarget(target);
			}
			
			if (setInvocationContext) {
				// Restore old invocation, which may be null
				AopContext.setCurrentInvocation(oldInvocation);
			}
			if (setProxyContext) {
				// Restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
			
			if (invocation != null) {
				advised.getMethodInvocationFactory().release(invocation);
			}
		}
	}
	

	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the thread context class loader.
	 */
	public Object getProxy() {
		return getProxy(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the given class loader.
	 */
	public Object getProxy(ClassLoader cl) {
		// Use CGLIB
		if (this.advised.getTargetSource().getTargetClass() == null) {
			throw new IllegalArgumentException("Either an interface or a target is required for proxy creation");
		}
		// proxy the given class itself: CGLIB necessary
		if (logger.isInfoEnabled())
			logger.info("Creating CGLIB proxy for [" + this.advised.getTargetSource().getTargetClass() + "]");
		// delegate to inner class to avoid AopProxy runtime dependency on CGLIB
		// --> J2SE proxies work without cglib.jar then
		return createProxy();
	}
	
	private Object createProxy() {
		try {
			return Enhancer.enhance(advised.getTargetSource().getTargetClass(), AopProxyUtils.completeProxiedInterfaces(advised),
				this
			);
		}
		catch (CodeGenerationException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" + advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class, or a non-visible class", ex);
		}
	}
	

	/**
	 * Equality means interceptors and interfaces are ==.
	 * This will only work with J2SE dynamic proxies,	not with CGLIB ones
	 * (as CGLIB doesn't delegate equals calls to proxies).
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param other may be a dynamic proxy wrapping an instance
	 * of this class
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		
		Cglib1AopProxy aopr2 = null;
		if (other instanceof Cglib1AopProxy) {
			aopr2 = (Cglib1AopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof Cglib1AopProxy))
				return false;
			aopr2 = (Cglib1AopProxy) ih; 
		}
		else {
			// Not a valid comparison
			return false;
		}
		
		// If we get here, aopr2 is the other AopProxy
		if (this == aopr2)
			return true;
			
		if (!Arrays.equals(aopr2.advised.getProxiedInterfaces(), this.advised.getProxiedInterfaces()))
			return false;
		
		if (!Arrays.equals(aopr2.advised.getAdvisors(), this.advised.getAdvisors()))
			return false;
			
		return true;
	}




}
