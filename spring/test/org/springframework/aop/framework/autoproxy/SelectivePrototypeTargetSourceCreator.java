/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.autoproxy.target.PrototypeTargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Overrides generic PrototypeTargetSourceCreator to create a prototype only for beans
 * with names beginning with "prototype"
 * @author Rod Johnson
 * @version $Id$
 */
public class SelectivePrototypeTargetSourceCreator extends PrototypeTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.autoproxy.target.AbstractPrototypeTargetSourceCreator#createPrototypeTargetSource(java.lang.Object, java.lang.String, org.springframework.beans.factory.BeanFactory)
	 */
	protected AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		if (!beanName.startsWith("prototype"))
			return null;
		return super.createPrototypeTargetSource(bean, beanName, factory);
	}
}
