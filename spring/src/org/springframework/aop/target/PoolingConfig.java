/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;

/**
 * Config interface for a pooling invoker.
 * @author Rod Johnson
 * @version $Id$
 */
public interface PoolingConfig {
	
	int getMaxSize();
	
	int getActive() throws UnsupportedOperationException;
	
	int getFree() throws UnsupportedOperationException;

}
