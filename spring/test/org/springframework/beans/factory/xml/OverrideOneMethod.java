/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

import org.springframework.beans.TestBean;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public abstract class OverrideOneMethod {
	
	public abstract TestBean getPrototypeDependency();
	
	protected abstract TestBean protectedOverrideSingleton();
	
	public String echo(String echo) {
		return echo;
	}
	
	public String replaceMe(String echo) {
		return echo;
	}

}
