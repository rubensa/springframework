/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.AttributeRegistry;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Spring implementation of AOP Alliance MethodInvocation interface 
 * @author Rod Johnson
 * @version $Id$
 */
public class MethodInvocationImpl implements MethodInvocation {
	
	/**  
	 * Interface this invocation is against.
	 * May not be the same as the method's declaring interface. 
	 */
	private Class targetInterface;

	private Method method;
	
	private Object[] arguments;
	
	private Object target;
	
	private Object proxy;
	
	/** 
	 * Interceptors and any InterceptionAdvice that needs dynamic checks.
	 **/
	public List interceptorsAndDynamicMethodMatchers;
	
	/** 
	 * Any resources attached to this invocation.
	 * Lazily initialized for efficiency.
	 */
	private HashMap resources;
	
	private boolean exhausted;
	
	
	/**
	 * Index from 0 of the current interceptor we're invoking.
	 * -1 until we invoke: then the current interceptor
	 */
	private int currentInterceptor = -1;
	
	private Class targetClass;
	
	
	/**
	 * Construct a new MethodInvocation with given arguments
	 * @param interceptorsAndDynamicMethodMatchers interceptors that should be applied,
	 * along with any InterceptorAndDynamicMethodMatchers that need evaluation at runtime.
	 * MethodMatchers included in this struct must already have been found to have matched as far
	 * as was possibly statically. Passing an array might be about 10% faster, but would complicate
	 * the code. And it would work only for static pointcuts.
	 */
	public MethodInvocationImpl(Object proxy, Object target, 
					Class targetInterface, Method m, Object[] arguments,
					Class targetClass, List interceptorsAndDynamicMethodMatchers) {
		populate(proxy, target, targetInterface, m, arguments,
				targetClass, interceptorsAndDynamicMethodMatchers);
	}
	
	protected MethodInvocationImpl() {
	}

	protected void populate(Object proxy, Object target,
						Class targetInterface, Method m, Object[] arguments,
						Class targetClass, List interceptorsAndDynamicMethodMatchers) {
		this.proxy = proxy;
		this.target = target;
		this.targetInterface = targetInterface;
		this.targetClass = targetClass;
		this.method = m;
		this.arguments = arguments;
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
		this.currentInterceptor = -1;
	}
	
	public void clear() {
		this.proxy = null;
		this.targetInterface = null;
		this.targetClass = null;
		this.target = null;
		this.method = null;
		this.arguments = null;
		this.interceptorsAndDynamicMethodMatchers = null;
		this.resources = null;
	}
	
	
	/**
	 * Return the method invoked on the proxied interface.
	 * May or may not correspond with a method invoked on an underlying
	 * implementation of that interface.
	 * @return Method
	 */
	public Method getMethod() {
		return this.method;
	}
	
	public AccessibleObject getStaticPart() {
		return this.method;
	}
	
	/**
	 * Return the proxy that this interception was made through
	 * @return Object
	 */
	public Object getProxy() {
		return this.proxy;
	}
	

	public String toString() {
		// Don't do toString on target, it may be
		// proxied
		
		// ToString on args may also fail
		String s =  "Invocation: method=[" + method + "] " +
				//"args=[" + StringUtils.arrayToDelimitedString(arguments, ",") +
				"args=" + this.arguments + 
				"] ";
		 
		s += (this.target == null) ? "target is null": 
				"target is of class " + target.getClass().getName();
		return s;
				
	}


	public Object addAttachment(String key, Object resource) {
		// Invocations are single-threaded, so we can lazily
		// instantiate the resource map if we have to
		if (this.resources == null) {
			this.resources = new HashMap();
		}
		Object oldValue = this.resources.get(key);
		this.resources.put(key, resource);
		return oldValue;
	}
	
	/**
	 * @return the resource or null
	 */
	public Object getAttachment(String key) {
		// Resource map may be null if it hasn't been instantiated
		return (this.resources == null) ? null : this.resources.get(key);
	}
	
	/**
	 * Private optimization method
	 * @return Object[]
	 */
	public Object[] getArguments() {
		return this.arguments;
	}
	
	/**
	 * @see org.aopalliance.intercept.MethodInvocation#getArgument(int)
	 */
	public Object getArgument(int i) {
		return this.arguments[i];
	}

	/**
	 * @see org.aopalliance.intercept.MethodInvocation#getArgumentCount()
	 */
	public int getArgumentCount() {
		return (this.arguments != null) ? this.arguments.length : 0;
	}


	public Class getTargetInterface() {
		return this.targetInterface;
	}

	/**
	 * @see org.aopalliance.intercept.Invocation#proceed
	 */
	public Object proceed() throws Throwable {
		if (exhausted)
			throw new AspectException("All interceptors have already been invoked");
		
		if (this.currentInterceptor == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			exhausted = true;
			return AopProxy.invokeJoinpointUsingReflection(target, method, arguments);
		}
		
		// We begin with -1 and increment early

		Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptor);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			// Evaluate dynamic method matcher here: static part will already have
			// been evaluated and found to match
			InterceptorAndDynamicMethodMatcher dm = (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
				return dm.interceptor.invoke(this);
			}
			else {
				// Dynamic matching failed
				// Skip this interceptor and invoke the next in the chain
				return proceed();
			}
		}
		else {
			// It's an interceptor so we just invoke it: the pointcut will have
			// been evaluated statically before this object was constructed
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}


	/**
	 * @see org.aopalliance.intercept.Invocation#cloneInstance
	 */
	public Invocation cloneInstance() {
		return this;
	}

	/**
	 * @see org.aopalliance.intercept.Invocation#getAttributeRegistry()
	 */
	public AttributeRegistry getAttributeRegistry() {
		throw new UnsupportedOperationException("Likely to be removed from AOP Alliance API");
	}

	/**
	 * @see org.aopalliance.intercept.MethodInvocation#setArgument(int, java.lang.Object)
	 */
	public void setArgument(int index, Object argument) {
		this.arguments[index] = argument;
	}


	/**
	 * @see org.aopalliance.intercept.Invocation#getThis
	 */
	public Object getThis() {
		return this.target;
	}

}
