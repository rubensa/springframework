/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

/**
 * Tag class. Its class means it has the opposite 
 * behaviour to the RollbackRule superclass.
 * @author Rod Johnson
 * @since 09-Apr-2003
 * @version $Id$
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {

	/**
	 * @param exceptionName
	 */
	public NoRollbackRuleAttribute(String exceptionName) {
		super(exceptionName);
	}
	
	public String toString() {
		return "No" + super.toString();
	}

}
