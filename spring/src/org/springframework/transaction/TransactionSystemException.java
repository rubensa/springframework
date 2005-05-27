/*
 * Copyright 2002-2005 the original author or authors.
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

/**
 * Exception thrown when a general transaction system error is encountered,
 * like on commit or rollback.
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 */
public class TransactionSystemException extends TransactionException {

	/**
	 * Constructor for TransactionSystemException.
	 * @param msg message
	 */
	public TransactionSystemException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for TransactionSystemException.
	 * @param msg message
	 * @param ex root cause from transaction API in use
	 */
	public TransactionSystemException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
