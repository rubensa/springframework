/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;


/**
 * Interface for transaction class. Attributes are on the class,
 * not this interface.
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface TxClass {
	
	
	public int defaultTxAttribute();
	
	
	public void echoException(Exception ex) throws Exception;

}
