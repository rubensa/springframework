/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class NoSuchUserException extends Exception {
	
	private long id;
	
	public NoSuchUserException(long id) {
		super("No user with id " + id);
		this.id = id;
	}

	/**
	 * @return Returns the id.
	 */
	public long getId() {
		return this.id;
	}

}
