/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.core.namedparam;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * Interface specifying a basic set of JDBC operations allowing
 * the use of named parameters rather than the traditional '?' placeholders.
 *
 * <p>This is an alternative to the classic JdbcOperations interface,
 * implemented by NamedParameterJdbcTemplate. This interface is not often used
 * directly, but provides a useful option to enhance testability, as it can
 * easily be mocked or stubbed.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcTemplate
 * @see org.springframework.jdbc.core.JdbcOperations
 */
public interface NamedParameterJdbcOperations {

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * classic JDBC operations.
	 */
	JdbcOperations getJdbcOperations();


	/**
	 * Query given SQL to create a prepared statement from SQL and a list of
	 * arguments to bind to the query, reading the ResultSet on a per-row basis
	 * with a RowCallbackHandler.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @param rch object that will extract results, one row at a time
	 * @throws DataAccessException if the query fails
	 */
	public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch)
			throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list of
	 * arguments to bind to the query, reading the ResultSet on a per-row basis
	 * with a RowCallbackHandler.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @param rch object that will extract results, one row at a time
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 */
	void query(String sql, Map paramMap, RowCallbackHandler rch) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list
	 * of arguments to bind to the query, mapping each row to a Java object
	 * via a RowMapper.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @param rowMapper object that will map one object per row
	 * @return the result List, containing mapped objects
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 */
	List query(String sql, SqlParameterSource paramSource, RowMapper rowMapper)
			throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list
	 * of arguments to bind to the query, mapping each row to a Java object
	 * via a RowMapper.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @param rowMapper object that will map one object per row
	 * @return the result List, containing mapped objects
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 */
	List query(String sql, Map paramMap, RowMapper rowMapper) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list
	 * of arguments to bind to the query, mapping a single result row to a
	 * Java object via a RowMapper.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @param rowMapper object that will map one object per row
	 * @return the single mapped object
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 */
	Object queryForObject(String sql, SqlParameterSource paramSource, RowMapper rowMapper)
			throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a list
	 * of arguments to bind to the query, mapping a single result row to a
	 * Java object via a RowMapper.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @param rowMapper object that will map one object per row
	 * @return the single mapped object
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 */
	Object queryForObject(String sql, Map paramMap, RowMapper rowMapper) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result object.
	 * <p>The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type, or <code>null</code> in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)
	 */
	Object queryForObject(String sql, SqlParameterSource paramSource, Class requiredType)
			throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result object.
	 * <p>The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type, or <code>null</code> in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(String, Class)
	 */
	Object queryForObject(String sql, Map paramMap, Class requiredType) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result Map.
	 * <p>The query is expected to be a single row query; the result row will be
	 * mapped to a Map (one entry for each column, using the column name as the key).
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @return the result Map (one entry for each column, using the column name as the key)
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(String)
	 * @see org.springframework.jdbc.core.ColumnMapRowMapper
	 */
	Map queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result Map.
	 * The queryForMap() methods defined by this interface are appropriate
	 * when you don't have a domain model. Otherwise, consider using
	 * one of the queryForObject() methods.
	 * <p>The query is expected to be a single row query; the result row will be
	 * mapped to a Map (one entry for each column, using the column name as the key).
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return the result Map (one entry for each column, using the column name as the key)
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(String)
	 * @see org.springframework.jdbc.core.ColumnMapRowMapper
	 */
	Map queryForMap(String sql, Map paramMap) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in a long value.
	 * <p>The query is expected to be a single row/single column query that
	 * results in a long value.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @return the long value, or 0 in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForLong(String)
	 */
	long queryForLong(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in a long value.
	 * <p>The query is expected to be a single row/single column query that
	 * results in a long value.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return the long value, or 0 in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException
	 * if the query does not return exactly one row, or does not return exactly
	 * one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForLong(String)
	 */
	long queryForLong(String sql, Map paramMap) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in an int value.
	 * <p>The query is expected to be a single row/single column query that
	 * results in an int value.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @return the int value, or 0 in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the query does not return
	 * exactly one row, or does not return exactly one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForInt(String)
	 */
	int queryForInt(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in an int value.
	 * <p>The query is expected to be a single row/single column query that
	 * results in an int value.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return the int value, or 0 in case of SQL NULL
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the query does not return
	 * exactly one row, or does not return exactly one column in that row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForInt(String)
	 */
	int queryForInt(String sql, Map paramMap) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>The results will be mapped to a List (one entry for each row) of
	 * result objects, each of them matching the specified element type.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @param elementType the required type of element in the result list
	 * (for example, <code>Integer.class</code>)
	 * @return a List of objects that match the specified element type
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Class)
	 * @see org.springframework.jdbc.core.SingleColumnRowMapper
	 */
	List queryForList(String sql, SqlParameterSource paramSource, Class elementType)
			throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>The results will be mapped to a List (one entry for each row) of
	 * result objects, each of them matching the specified element type.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @param elementType the required type of element in the result list
	 * (for example, <code>Integer.class</code>)
	 * @return a List of objects that match the specified element type
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String, Class)
	 * @see org.springframework.jdbc.core.SingleColumnRowMapper
	 */
	List queryForList(String sql, Map paramMap, Class elementType) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>The results will be mapped to a List (one entry for each row) of
	 * Maps (one entry for each column, using the column name as the key).
	 * Thus  Each element in the list will be of the form returned by this interface's
	 * queryForMap() methods.
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @return a List that contains a Map per row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String)
	 */
	List queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>The results will be mapped to a List (one entry for each row) of
	 * Maps (one entry for each column, using the column name as the key).
	 * Each element in the list will be of the form returned by this interface's
	 * queryForMap() methods.
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return a List that contains a Map per row
	 * @throws org.springframework.dao.DataAccessException if the query fails
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(String)
	 */
	List queryForList(String sql, Map paramMap) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a SqlRowSet.
	 * <p>The results will be mapped to an SqlRowSet which holds the data in a
	 * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
	 * <p>Note that that, for the default implementation, JDBC RowSet support needs to
	 * be available at runtime: by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code>
	 * class is used, which is part of JDK 1.5+ and also available separately as part of
	 * Sun's JDBC RowSet Implementations download (rowset.jar).
	 * @param sql SQL query to execute
	 * @param paramSource container of arguments to bind to the query
	 * @return a SqlRowSet representation (possibly a wrapper around a
	 * <code>javax.sql.rowset.CachedRowSet</code>)
	 * @throws org.springframework.dao.DataAccessException if there is any problem executing the query
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(String)
	 * @see org.springframework.jdbc.core.SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a SqlRowSet.
	 * <p>The results will be mapped to an SqlRowSet which holds the data in a
	 * disconnected fashion. This wrapper will translate any SQLExceptions thrown.
	 * <p>Note that that, for the default implementation, JDBC RowSet support needs to
	 * be available at runtime: by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code>
	 * class is used, which is part of JDK 1.5+ and also available separately as part of
	 * Sun's JDBC RowSet Implementations download (rowset.jar).
	 * @param sql SQL query to execute
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return a SqlRowSet representation (possibly a wrapper around a
	 * <code>javax.sql.rowset.CachedRowSet</code>)
	 * @throws org.springframework.dao.DataAccessException if there is any problem executing the query
	 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(String)
	 * @see org.springframework.jdbc.core.SqlRowSetResultSetExtractor
	 * @see javax.sql.rowset.CachedRowSet
	 */
	SqlRowSet queryForRowSet(String sql, Map paramMap) throws DataAccessException;

	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param paramSource container of arguments and SQL types to bind to the query
	 * @return the number of rows affected
	 * @throws org.springframework.dao.DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param paramMap map of parameters to bind to the query
	 * (leaving it to the PreparedStatement to guess the corresponding SQL type)
	 * @return the number of rows affected
	 * @throws org.springframework.dao.DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, Map paramMap) throws DataAccessException;

    /**
     * Issue an update via a prepared statement, binding the given arguments.
     * @param sql SQL, containing bind parameters
     * @param paramSource container of arguments and SQL types to bind to the query
     * @param generatedKeyHolder KeyHolder that will hold the generated keys
     * @param keyColumnNames names of the columns that will have keys generated for them
     * @return the number of rows affected
     * @throws org.springframework.dao.DataAccessException if there is any problem issuing the update
     */
    int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames) throws DataAccessException;


}
