/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class TimestampIntroductionAdvisor extends DefaultIntroductionAdvisor {

	/**
	 * @param dii
	 */
	public TimestampIntroductionAdvisor() {
		super(new DelegatingIntroductionInterceptor(new TimestampIntroductionInterceptor()));
	}

}
