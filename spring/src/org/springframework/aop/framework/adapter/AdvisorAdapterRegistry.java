/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface AdvisorAdapterRegistry {
	
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;
	
	/**
	 * Don't worry about the pointcut: just return an interceptor
	 * @param advisor
	 * @return
	 * @throws UnknownAdviceTypeException
	 */
	Interceptor getInterceptor(Advisor advisor) throws UnknownAdviceTypeException;;
	
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}
