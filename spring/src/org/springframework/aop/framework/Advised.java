/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.TargetSource;
import org.springframework.aop.ThrowsAdvice;

/**
 * Interface to be implemented by classes that hold the configuration
 * of a factory of AOP proxies. This configuration includes
 * the Interceptors and other advice, and Advisors, and the proxied interfaces.
 *
 * <p>Any AOP proxy obtained from Spring can be cast to this interface to allow
 * manipulation of its AOP advice.
 *
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id$
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised {
	
	/**
	 * Return the TargetSource used by this Advised object
	 * @return the TargetSource used by this advised object
	 */
	TargetSource getTargetSource();
	
	/**
	 * Change the TargetSource used by this Advised object.
	 * Only works if the configuration isn't frozen.
	 * @param targetSource new TargetSource to use
	 */
	void setTargetSource(TargetSource targetSource);
	
	/**
	 * Get whether the factory should expose the proxy as a ThreadLocal. 
	 * This can be necessary if a target object needs to invoke a method on itself
	 * benefitting from advice. (If it invokes a method on <code>this</code> no advice
	 * will apply.) Getting the proxy is analogous to an EJB calling getEJBObject().
	 * @return whether the factory should expose the proxy as a ThreadLocal
	 * @see AopContext
	 */
	boolean getExposeProxy();
	
	/**
	 * Should we proxy the target class as well as any interfaces?
	 * Can use this to force CGLIB proxying.
	 * @return whether we proxy the target class as well as any interfaces
	 */
	boolean getProxyTargetClass();
	
	/**
	 * Return the Advisors applying to this proxy.
	 * @return a list of Advisors applying to this proxy. Cannot return null,
	 * but may return the empty array.
	 */
	Advisor[] getAdvisors();
	
	/**
	 * Return the interfaces proxied by the AOP proxy. Will not
	 * include the target class, which may also be proxied.
	 * @return the interfaces proxied by the AOP proxy
	 */
	Class[] getProxiedInterfaces();
	
	/**
	 * Return whether this interface is proxied
	 * @param intf interface to test
	 * @return whether the interface is proxied
	 */
	boolean isInterfaceProxied(Class intf);
	
	/**
	 * Add the given AOP Alliance interceptor to the tail of the advice (interceptor) chain..
	 * Use addAdvice method instead.
	 * @param interceptor to add to the tail of the chain
	 * @see #addInterceptor(int, Interceptor)
	 * @see org.springframework.aop.support.DefaultPointcutAdvisor
	 * @deprecated will be removed in Spring 1.2
	 */
	void addInterceptor(Interceptor interceptor) throws AopConfigException;
	
	/**
	 * Add the given AOP Alliance advice to the tail of the advice (interceptor) chain.
	 * This will be wrapped in a DefaultPointcutAdvisor with a pointcut that
	 * always applies, and returned from the getAdvisors() method in this
	 * wrapped form.
	 * @param advice advice to add to the tail of the chain
	 * @see #addAdvice(int, Advice)
	 * @see org.springframework.aop.support.DefaultPointcutAdvisor
	 */
	void addAdvice(Advice advice) throws AopConfigException;

	/**
	 * Add the given AOP Alliance interceptor at the specified position in the interceptor chain.
	 * Use addAdvice(int, Advice) instead.
	 * @param pos index from 0 (head)
	 * @param interceptor interceptor to add at the specified position in the
	 * interceptor chain
	 * @deprecated
	 */
	void addInterceptor(int pos, Interceptor interceptor) throws AopConfigException;
	
	/**
	 * Add the given AOP Alliance Advice at the specified position in the interceptor chain.
	 * @param pos index from 0 (head)
	 * @param advice advice to add at the specified position in the
	 * interceptor chain
	 */
	void addAdvice(int pos, Advice advice) throws AopConfigException;
	
	/**
	 * Add an AfterReturningAdvice to the tail of the advice chain
	 * @param afterReturningAdvice AfterReturningAdvice to add
	 * @throws AopConfigException if the advice cannot be added--for example, because
	 * the proxy configuration is frozen
	 * @deprecated
	 */
	void addAfterReturningAdvice(AfterReturningAdvice afterReturningAdvice) throws AopConfigException;
	
	/**
	 * Add a MethodBeforeAdvice to the tail of the advice chain
	 * @param beforeAdvice MethodBeforeAdvice to add
	 * @throws AopConfigException if the advice cannot be added--for example, because
	 * the proxy configuration is frozen
	 * @deprecated
	 */		
	void addBeforeAdvice(MethodBeforeAdvice beforeAdvice) throws AopConfigException;
	
	/**
	 * Add a ThrowsAdvice to the tail of the advice chain
	 * @param throwsAdvice ThrowsAdvice to add
	 * @throws AopConfigException if the advice cannot be added--for example, because
	 * the proxy configuration is frozen
	 * @deprecated
	 */
	void addThrowsAdvice(ThrowsAdvice throwsAdvice) throws AopConfigException;

	/** 
	 * Add an Advisor at the end of the advisor chain.
	 * The Advisor may be an IntroductionAdvisor, in which new interfaces
	 * will be available when a proxy is next obtained from the relevant factory.
	 * @param advisor Advisor to add to the end of the chain
	 */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/** 
	 * Add an Advisor at the specified position in the chain
	 * @param advisor advisor to add at the specified position in the chain
	 * @param pos position in chain (0 is head). Must be valid.
	 */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;
	
	/**
	 * Return the index (from 0) of the given advisor,
	 * or -1 if no such advisor applies to this proxy.
	 * The return value of this method can be used to index into
	 * the Advisors array.
	 * @param advisor advisor to search for
	 * @return index from 0 of this advisor, or -1 if there's
	 * no such advisor.
	 */
	int indexOf(Advisor advisor);
	
	/**
	 * Remove the given advisor
	 * @param advisor advisor to remove
	 * @return true if the advisor was removed; false if the
	 * advisor was not found and hence could not be removed
	 */
	boolean removeAdvisor(Advisor advisor) throws AopConfigException;
	
	/**
	 * Remove the advisor at the given index
	 * @param index index of advisor to remove
	 * @throws AopConfigException if the index is invalid
	 */
	void removeAdvisor(int index) throws AopConfigException;
	
	/**
	 * Remove the Advisor containing the given advice
	 * @param advice advice to remove
	 * @return whether the Advice was found and removed (false if
	 * there was no such advice)
	 */
	boolean removeAdvice(Advice advice) throws AopConfigException;
	
	/**
	 * Replace the given advisor.
	 * <b>NB:</b>If the advisor is an IntroductionAdvisor
	 * and the replacement is not or implements different interfaces,
	 * the proxy will need to be re-obtained or the old interfaces
	 * won't be supported and the new interface won't be implemented.
	 * @param a advisor to replace
	 * @param b advisor to replace it with
	 * @return whether it was replaced. If the advisor wasn't found in the
	 * list of advisors, this method returns false and does nothing.
	 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;
	
	/**
	 * @return whether the Advised configuration is frozen, and no
	 * advice changes can be made
	 */
	boolean isFrozen();
	
	/**
	 * As toString() will normally be delegated to the target, 
	 * this returns the equivalent for the AOP proxy
	 * @return a string description of the proxy configuration
	 */
	String toProxyConfigString();

}
