/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;


/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class GlobalAdvisorAdapterRegistry extends DefaultAdvisorAdapterRegistry {
	
	private static GlobalAdvisorAdapterRegistry instance = new GlobalAdvisorAdapterRegistry();
	
	public static GlobalAdvisorAdapterRegistry getInstance() {
		return instance;
	}
	
	private GlobalAdvisorAdapterRegistry() {
		
	}

	
}
