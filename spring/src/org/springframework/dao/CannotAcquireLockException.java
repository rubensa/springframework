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

package org.springframework.dao;

/**
 * Exception thrown on failure to aquire a lock during an update,
 * for example during a "select for update" statement.
 * @author Rod Johnson
 */
public class CannotAcquireLockException extends PessimisticLockingFailureException {

	/**
	 * Constructor for CannotAcquireLockException.
	 * @param msg message
	 */
	public CannotAcquireLockException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for CannotAcquireLockException.
	 * @param msg message
	 * @param ex root cause from data access API in use
	 */
	public CannotAcquireLockException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
