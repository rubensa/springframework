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

package org.springframework.jdbc.core;

/**
 * Subclass of SqlParameter to represent an output parameter.
 * No additional properties: instanceof will be used to check
 * for such types.
 *
 * <p>Output parameters - like all stored procedure parameters -
 * must have names.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public class SqlOutParameter extends SqlParameter {

	private RowCallbackHandler rowCallbackHandler = null;

	private RowMapper rowMapper = null;

	private int rowsExpected = -1;

	/**
	 * Create a new OutputParameter, supplying name and SQL type
	 * @param name name of the parameter, as used in input and output maps
	 * @param type SQL type of the parameter according to java.sql.Types
	 */
	public SqlOutParameter(String name, int type) {
		super(name, type);
	}

	public SqlOutParameter(String name, int type, String typeName) {
		super(name, type, typeName);
	}

	public SqlOutParameter(String name, int type, RowCallbackHandler rch) {
		super(name, type);
		this.rowCallbackHandler = rch;
	}

	public SqlOutParameter(String name, int type, RowMapper rm) {
		super(name, type);
		this.rowMapper = rm;
	}

	public SqlOutParameter(String name, int type, RowMapper rm, int rowsExpected) {
		super(name, type);
		this.rowMapper = rm;
		this.rowsExpected = rowsExpected;
	}

	public boolean isResultSetSupported() {
		return (this.rowCallbackHandler != null || this.rowMapper != null);
	}
	
	public boolean isRowMapperSupported() {
		return (this.rowMapper != null);
	}

	public RowCallbackHandler getRowCallbackHandler() {
		return rowCallbackHandler;
	}

	/**
	 * Return new instance of the implementation of a ResultReader usable for
	 * returned ResultSets. This implementation invokes the RowMapper's
	 * implementation of the mapRow method, via a RowMapperResultReader adapter.
	 * @see #isRowMapperSupported
	 * @see RowMapperResultReader
	 */
	protected final ResultReader newResultReader() {
		return new RowMapperResultReader(this.rowMapper, this.rowsExpected);
	}
	
}
