/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

/**
 * Interface specifying the API for a Simple JDBC Insert implemented by {@link SimpleJdbcInsert}.
 * This interface is not often used directly, but provides the
 * option to enhance testability, as it can easily be mocked or stubbed.
 * @author trisberg
 * @simce 2.1
 */
public interface SimpleJdbcInsertOperations {
	
	/**
	 * Specify the table name to be used for the insert.
	 *
	 * @param tableName the name of the stored table
	 * @return the instance of this SimpleJdbcInsert
	 */
	SimpleJdbcInsert withTableName(String tableName);

	/**
	 * Specify the shema name, if any, to be used for the insert.
	 *
	 * @param schemaName
	 * @return the instance of this SimpleJdbcInsert
	 */
	SimpleJdbcInsert withSchemaName(String schemaName);

	/**
	 * Specify the catalog name, if any, to be used for the insert.
	 *
	 * @param catalogName
	 * @return the instance of this SimpleJdbcInsert
	 */
	SimpleJdbcInsert withCatalogName(String catalogName);

	/**
	 * Specify the column names that the insert statement should be limited to use.
	 *
	 * @param columnNames one or more column names
	 * @return the instance of this SimpleJdbcInsert
	 */
	SimpleJdbcInsert usingColumns(String... columnNames);

	/**
	 * Specify the name sof any columns that have auto generated keys.
	 *
	 * @param columnNames one or more column names
	 * @return the instance of this SimpleJdbcInsert
	 */
	SimpleJdbcInsert usingGeneratedKeyColumns(String... columnNames);

	/**
	 * Execute the insert using the values passed in.
	 *
	 * @param args Map containing column names and corresponding value
	 * @return the number of rows affected as returned by the JDBC driver
	 */
	int execute(Map<String, Object> args);

	/**
	 * Execute the insert using the values passed in.
	 *
	 * @param parameterSource SqlParameterSource containing values to use for insert
	 * @return the number of rows affected as returned by the JDBC driver
	 */
	int execute(SqlParameterSource parameterSource);

	/**
	 * Execute the insert using the values passed in and return the generated key.  This requires that
	 * the name of the columns with auto generated keys have been specified.
	 *
	 * @param args Map containing column names and corresponding value
	 * @return the generated key value
	 */
	Number executeAndReturnKey(Map<String, Object> args);

	/**
	 * Execute the insert using the values passed in and return the generated key.  This requires that
	 * the name of the columns with auto generated keys have been specified.
	 *
	 * @param parameterSource SqlParameterSource containing values to use for insert
	 * @return the generated key value
	 */
	Number executeAndReturnKey(SqlParameterSource parameterSource);

	/**
	 * Execute the insert using the values passed in and return the generated keys.  This requires that
	 * the name of the columns with auto generated keys have been specified.
	 *
	 * @param args Map containing column names and corresponding value
	 * @return the KeyHolder containing all generated keys
	 */
	KeyHolder executeAndReturnKeyHolder(Map<String, Object> args);

	/**
	 * Execute the insert using the values passed in and return the generated keys.  This requires that
	 * the name of the columns with auto generated keys have been specified.
	 *
	 * @param parameterSource SqlParameterSource containing values to use for insert
	 * @return the KeyHolder containing all generated keys
	 */
	KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource);

	/**
	 * Execute a batch insert using the batch of values passed in.
	 *
	 * @param batch an array of Maps containing a batch of column names and corresponding value
	 * @return the array of number of rows affected as returned by the JDBC driver
	 */
	int[] executeBatch(Map<String, Object>[] batch);

	/**
	 * Execute a batch insert using the batch of values passed in.
	 *
	 * @param batch an array of SqlParameterSource containing values for the batch
	 * @return the array of number of rows affected as returned by the JDBC driver
	 */
	int[] executeBatch(SqlParameterSource[] batch);
}
