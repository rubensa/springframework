package org.springframework.transaction.annotations;

import java.sql.Connection;

/**
 * Represents transaction isolation levels for use with with 
 * TxAttribute Annotation.
 * 
 * @see org.springframework.transaction.annotations.TxAttribute
 *
 * @author Colin Sampaleanu
 */
public enum IsolationLevel {
	
	DEFAULT(-1),
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	IsolationLevel(int value) { this.value = value; }
	
	private final int value;

	public int value() { return value; }	
	
}
