/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator;
import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.beans.factory.BeanFactory;

/**
 * Simple PoolingTargetSourceCreator that pools everything.
 * @author Rod Johnson
 * @version $Id$
 */
public class TestPoolingTargetSourceCreator extends AbstractPoolingTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator#getPoolingAttribute(java.lang.Object, java.lang.String, org.springframework.beans.factory.BeanFactory)
	 */
	protected PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory bf) {
		return new PoolingAttribute(25);
	}



}
