/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.mail;

import org.springframework.core.NestedRuntimeException;

/**
 * Superclass for all mail exceptions.
 * @author Dmitriy Kopylenko
 * @version $Id$
 */
public abstract class MailException extends NestedRuntimeException {

	public MailException(String msg) {
		super(msg);
	}

	public MailException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
