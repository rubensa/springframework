package org.springframework.jdbc.support.incrementer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Abstract base class for incrementers that use a database sequence.
 * Subclasses need to provide the database-specific SQL to use.
 * @author Juergen Hoeller
 * @since 26.02.2004
 * @see #getSequenceQuery
 */
public abstract class AbstractSequenceMaxValueIncrementer extends AbstractDataFieldMaxValueIncrementer {

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
	}

	protected long getNextKey() throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			rs = stmt.executeQuery(getSequenceQuery());
			if (rs.next()) {
				return rs.getLong(1);
			}
			else {
				throw new DataAccessResourceFailureException("Sequence query did not return a result");
			}
		}
		catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain sequence value", ex);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	/**
	 * Return the database-specific query to use for retrieving a sequence value.
	 */
	protected abstract String getSequenceQuery();

}
