/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction;

import org.springframework.core.NestedRuntimeException;

/**
 * Superclass for all transaction exceptions.
 * @author Rod Johnson
 * @since 17-Mar-2003
 * @version $Revision$
 */
public abstract class TransactionException extends NestedRuntimeException {

	public TransactionException(String msg) {
		super(msg);
	}

	public TransactionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
