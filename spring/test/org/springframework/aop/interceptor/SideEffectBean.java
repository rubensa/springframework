/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

/**
 * Bean that changes state on a business invocation, so that
 * we can check whether it's been invoked
 * @author Rod Johnson
 * @version $Id$
 */
public class SideEffectBean {
	
	private int count;
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public void doWork() {
		++count;
	}

}
