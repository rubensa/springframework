/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.SimpleIntroductionAdvisor;

/**
 * Advisor for use with a LockMixin. Applies to all classes.
 * @author Rod Johnson
 * @version $Id$
 */
public class LockMixinAdvisor extends SimpleIntroductionAdvisor {
	
	public LockMixinAdvisor() {
		super(new LockMixin(), Lockable.class);
	}

}
