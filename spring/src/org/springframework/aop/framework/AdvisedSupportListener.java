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
public interface AdvisedSupportListener {
	
	/**
	 * Invoked when first proxy is created
	 * @param advisedSupport
	 */
	void activated(AdvisedSupport advisedSupport);
	
	/**
	 * Invoked when advice is changed after a proxy is created
	 * @param advisedSupport
	 */
	void adviceChanged(AdvisedSupport advisedSupport);

}
