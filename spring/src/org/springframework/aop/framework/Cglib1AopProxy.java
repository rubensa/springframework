/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.CodeGenerationException;
import net.sf.cglib.Enhancer;
import net.sf.cglib.Factory;
import net.sf.cglib.MethodFilter;
import net.sf.cglib.MethodInterceptor;
import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

/**
 * CGLIB AopProxy implementation for the Spring AOP framework.
 * Also implements the CGLIB MethodInterceptor and MethodFilter
 * interfaces.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by a AdvisedSupport implementation. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class are threadsafe if the
 * underlying (target) class is threadsafe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id$
 * @see net.sf.cglib.Enhancer
 */
class Cglib1AopProxy implements AopProxy, MethodInterceptor, MethodFilter {
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** Config used to configure this proxy */
	protected final AdvisedSupport advised;
	
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
		if (this.advised.getTargetSource().getTargetClass() == null) {
			throw new AopConfigException("Either an interface or a target is required for proxy creation");
		}
	}
		
	
	/**
	 * Implementation of MethodInterceptor.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 * @see net.sf.cglib.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.MethodProxy)
	 */
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
	
		MethodInvocation invocation = null;
		Object oldProxy = null;
		boolean setProxyContext = false;
	
		TargetSource targetSource = advised.targetSource;
		Class targetClass = null;//targetSource.getTargetClass();
		Object target = null;		
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (isEqualsMethod(method)) {
				// What if equals throws exception!?

				// This class implements the equals() method itself
				// We don't need to use reflection
				return new Boolean(equals(args[0]));
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
			
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}
		
			List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// Check whether we only have one InvokerInterceptor: that is, no real advice,
			// but just reflective invocation of the target.
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = methodProxy.invoke(target, args);
			}
			else {
				// We need to create a method invocation...
				invocation = new CglibMethodInvocation(proxy, target, targetClass, method, args, 
							targetClass, chain, methodProxy);
				
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
			
			if (setProxyContext) {
				// Restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
			
			//if (invocation != null) {
			//	advised.getMethodInvocationFactory().release(invocation);
			//}
		}
	}	// intercept
	
	/**
	 * Is the given method the equals method?
	 */
	protected final boolean isEqualsMethod(Method m) {
		return "equals".equals(m.getName()) && 
				m.getParameterTypes().length == 1 && 
				m.getParameterTypes()[0] == Object.class;
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
		if (logger.isInfoEnabled())
			logger.info("Creating CGLIB proxy for [" + this.advised.getTargetSource().getTargetClass() + "]");
		// delegate to inner class to avoid AopProxy runtime dependency on CGLIB
		// --> J2SE proxies work without cglib.jar then
		return createProxy();
	}
	
	protected Object createProxy() {
		try {
			return Enhancer.enhance(advised.getTargetSource().getTargetClass(), 
				AopProxyUtils.completeProxiedInterfaces(advised),
				this,	// MethodInterceptor
				null, 	// ClassLoader: use default
				null,	// Don't worry about serialization and writeReplace for now
				this	// MethodFilter
			);
		}
		catch (CodeGenerationException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" + advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class, or a non-visible class", ex);
		}
	}
	
	/**
	 * Exclude finalize() method.
	 * @see net.sf.cglib.MethodFilter#accept(java.lang.reflect.Member)
	 */
	public boolean accept(Member member) {
		return !(member.getName().equals("finalize") && member.getDeclaringClass() == Object.class);
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
		
		Cglib1AopProxy otherCglibProxy = null;
		if (other instanceof Cglib1AopProxy) {
			otherCglibProxy = (Cglib1AopProxy) other;
		}
		else if (other instanceof Factory) {
			MethodInterceptor mi = ((Factory) other).interceptor();
			if (!(mi instanceof Cglib1AopProxy))
				return false;
			otherCglibProxy = (Cglib1AopProxy) mi; 
		}
		else {
			// Not a valid comparison
			return false;
		}
		
		return AopProxyUtils.equalsInProxy(advised, otherCglibProxy.advised);
	}

}
