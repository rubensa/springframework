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

package org.springframework.jdbc.support.incrementer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Class to increment maximum value of a given MySQL table with the equivalent
 * of an auto-increment column. Note: If you use this class, your MySQL key
 * column should <i>NOT</i> be auto-increment, as the sequence table does the job.
 *
 * <p>The sequence is kept in a table; there should be one sequence table per
 * table that needs an auto-generated key. The table type of the sequence table
 * should be MyISAM so the sequences are allocated without regard to any
 * transactions that might be in progress.
 *
 * <p>Example:
 *
 * <pre>
 * create table tab (id int unsigned not null primary key, text varchar(100));
 * create table tab_sequence (value int not null) type=MYISAM;
 * insert into tab_sequence values(0);</pre>
 *
 * If cacheSize is set, the intermediate values are served without querying the
 * database. If the server or your application is stopped or crashes or a transaction
 * is rolled back, the unused values will never be served. The maximum hole size in
 * numbering is consequently the value of cacheSize.
 *
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @author Thomas Risberg
 */

public class MySQLMaxValueIncrementer extends AbstractDataFieldMaxValueIncrementer {

	/** The Sql string for retrieving the new sequence value */
	private static final String VALUE_SQL = "select last_insert_id()";

	/** The name of the column for this sequence */
	private String columnName;

	/** The number of keys buffered in a cache */
	private int cacheSize = 1;

	/** The next id to serve */
	private long nextId = 0;

	/** The max id to serve */
	private long maxId = 0;


	/**
	 * Default constructor.
	 **/
	public MySQLMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 **/
	public MySQLMaxValueIncrementer(DataSource ds, String incrementerName, String columnName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		this.columnName = columnName;
		afterPropertiesSet();
	}

	/**
	 * Set the name of the column in the sequence table.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Return the name of the column in the sequence table.
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * Set the number of buffered keys.
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	/**
	 * Return the number of buffered keys.
	 */
	public int getCacheSize() {
		return this.cacheSize;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.columnName == null) {
			throw new IllegalArgumentException("columnName is required");
		}
	}


	protected synchronized long getNextKey() throws DataAccessException {
		if (this.maxId == this.nextId) {
			/*
			* Need to use straight JDBC code because we need to make sure that the insert and select
			* are performed on the same connection (otherwise we can't be sure that last_insert_id()
			* returned the correct value)
			*/
			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				// increment the sequence column
				stmt.executeUpdate("update "+ getIncrementerName() + " set " + this.columnName +
													 " = last_insert_id(" + this.columnName + " + " + getCacheSize() + ")");
				// retrieve the new max of the sequence column
				ResultSet rs = stmt.executeQuery(VALUE_SQL);
				try {
					if (!rs.next()) {
						throw new DataAccessResourceFailureException("last_insert_id() failed after executing an update");
					}
					this.maxId = rs.getLong(1);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
				this.nextId = this.maxId - getCacheSize() + 1;
			}
			catch (SQLException ex) {
				throw new DataAccessResourceFailureException("Could not obtain last_insert_id()", ex);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
			}
		}
		else {
			this.nextId++;
		}
		return this.nextId;
	}

}
