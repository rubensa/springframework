/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;

/**
 * Used as a mixin.
 * @author Rod Johnson
 * @version $Id$
 */
public interface Modifiable {

	boolean isModified();
	
	void acceptChanges();
	
}
