/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction;

import org.springframework.core.NestedRuntimeException;

/**
 * Superclass for all transaction exceptions.
 * @author Rod Johnson
 * @since 17.03.2003
 */
public abstract class TransactionException extends NestedRuntimeException {

	/**
	 * Constructor for TransactionException.
	 * @param msg message
	 */
	public TransactionException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for TransactionException.
	 * @param msg message
	 * @param ex root cause from transaction API in use
	 */
	public TransactionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
