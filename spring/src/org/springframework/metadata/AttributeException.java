/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata;

import org.springframework.core.NestedRuntimeException;

/**
 * A simple wrapper for exceptions that occur within the metadata package.
 * @author Mark Pollack
 * @since Sep 28, 2003
 * @version $Id$
 * 
 */
public class AttributeException extends NestedRuntimeException {

	public AttributeException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AttributeException(String msg) {
		super(msg);
	}

}
