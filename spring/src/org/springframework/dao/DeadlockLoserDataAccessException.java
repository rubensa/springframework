/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.dao;

/**
 * Generic exception thrown when the current process was
 * a deadlock loser, and its transaction rolled back.
 * @author Rod Johnson
 * @version $Id$
 */
public class DeadlockLoserDataAccessException extends DataAccessException {

	/**
	 * Constructor for DeadlockLoserDataAccessException.
	 * @param msg mesg
	 * @param ex root cause
	 */
	public DeadlockLoserDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
