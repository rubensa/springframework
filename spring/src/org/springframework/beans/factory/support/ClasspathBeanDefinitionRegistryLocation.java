/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

/**
 * BeanDefinitionRegistryLocation implementation for resources loaded
 * from the classpath.
 * @author Rod Johnson
 * @version $Id$
 */
public class ClasspathBeanDefinitionRegistryLocation implements BeanDefinitionRegistryLocation {

	private final String path; 
	
	public ClasspathBeanDefinitionRegistryLocation(String path) {
		this.path = path;
	}
	
	public String toString() {
		return "Classpath resource='" + path + "'";
	}
}
