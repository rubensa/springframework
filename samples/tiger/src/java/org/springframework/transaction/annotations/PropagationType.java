package org.springframework.transaction.annotations;

import org.springframework.transaction.TransactionDefinition; 

/**
 * TODO: document after interfaces and classes are stable
 *
 * @author Colin Sampaleanu
 */
public enum PropagationType {
	
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
	REQUIRESNEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
	NOTSUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
	NEVER(TransactionDefinition.PROPAGATION_NEVER),
	NESTED(TransactionDefinition.PROPAGATION_NESTED);

	PropagationType(int value) { this.value = value; }
	
	private final int value;

	public int value() { return value; }	
	
}
