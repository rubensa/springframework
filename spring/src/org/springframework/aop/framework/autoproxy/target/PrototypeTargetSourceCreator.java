/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * TargetSource creator that creates a PrototypeTargetSource,
 * ensuring that each invocation is routed to a new instance of the target.
 * @author Rod Johnson
 * @version $Id$
 */
public final class PrototypeTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.support.TargetSourceCreator#getTargetSource(java.lang.Object,
	 *      java.lang.String, org.springframework.beans.factory.ListableBeanFactory)
	 */
	protected AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		return new PrototypeTargetSource();
	}
}