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

package org.springframework.dao;

/**
 * Data access exception thrown when something unintended appears to have
 * happened with an update, but the transaction hasn't already been rolled back.
 * Thrown, for example, when we wanted to update 1 row in an RDBMS but actually
 * updated 3.
 * @author Rod Johnson
 */
public class IncorrectUpdateSemanticsDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param msg message
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param msg message
	 * @param ex root cause from the underlying API, such as JDBC
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
	/**
	 * Return whether data was updated.
	 * If this method returns false, there's nothing to roll back.
	 * <p>The default implementation always returns true.
	 * This can be overridden in subclasses.
	 */
	public boolean wasDataUpdated() {
		return getDataWasUpdated();
	}

	/**
	 * @deprecated in favor of <code>wasDataUpdated</code>
	 * @see #wasDataUpdated
	 */
	public boolean getDataWasUpdated() {
		return true;
	}

}
