package org.springframework.workflow.jbpm;

import org.springframework.dao.DataAccessException;

/**
 * @author Rpb Harrop
 */
public class NestedDataAccessException extends DataAccessException {

	public NestedDataAccessException(String message) {
		super(message);
	}

	public NestedDataAccessException(String message, Throwable cause) {
		super(message, cause);
	}
}
