/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

/**
 * Statistics for a ThreadLocal invoker.
 * @author Rod Johnson
 * @see org.springframework.aop.interceptor.ThreadLocalInvokerInterceptor
 * @version $Id$
 */
public interface ThreadLocalInvokerStats {
	
	/**
	 * @return all invocations against the apartment invoker
	 */
	int getInvocations();

	/**
	 * @return hits that were satisfied by a thread bound object
	 */
	int getHits();

	/**
	 * @return thread bound objects created
	 */
	int getObjects();

}
