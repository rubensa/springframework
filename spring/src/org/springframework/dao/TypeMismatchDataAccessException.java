/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Exception thrown on mismatch between Java type and database type:
 * for example on an attempt to set an object of the wrong type
 * in an RDBMS column.
 * @author Rod Johnson
 * @version $Id$
 */
public class TypeMismatchDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * Constructor for TypeMismatchDataAccessException.
	 * @param msg message
	 * @param ex root cause
	 */
	public TypeMismatchDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
