/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context.support;

import org.springframework.beans.factory.support.BeanDefinitionRegistryLocation;

/**
 * Location for resources loaded using ApplicationContext loading methods
 * @author Rod Johnson
 * @version $Id$
 */
public class ApplicationContextResourceBeanDefinitionRegistryLocation implements BeanDefinitionRegistryLocation {
	
	private final String path;
	
	public ApplicationContextResourceBeanDefinitionRegistryLocation(String path) {
		this.path = path;
	}
	
	public String toString() {
		return "application context resource location: '" + path + "'";
	}

}
