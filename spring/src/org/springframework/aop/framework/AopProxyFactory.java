/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

/**
 * Interface to be implemented by objects that can create
 * AOP proxies based on AdvisedSupport objects
 * @author Rod Johnson
 * @version $Id$
 */
public interface AopProxyFactory {
	
	/**
	 * Return an AopProxy for the given AdvisedSupport object
	 * @param advisedSupport AOP configuration
	 * @return an AOP proxy
	 * @throws AopConfigException if the configuration is invalid
	 */
	AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException;

}
