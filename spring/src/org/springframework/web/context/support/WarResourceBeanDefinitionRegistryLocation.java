/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.support.BeanDefinitionRegistryLocation;

/**
 * BeanDefinitionRegistryLocation implementation for resources loaded a from servlet context
 * @author Rod Johnson
 * @version $Id$
 */
public class WarResourceBeanDefinitionRegistryLocation implements BeanDefinitionRegistryLocation {
	
	private final String path;
	
	public WarResourceBeanDefinitionRegistryLocation(ServletContext context, String path) {
		this.path = "ServletContext name='" + context.getServletContextName() + "'; path='" + path + "'" ;
	}
	
	public String toString() {
		return "WAR resource location: " + path;
	}

}
