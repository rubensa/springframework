package org.springframework.jdbc.core.support;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.CannotCloseJdbcConnectionException;
import org.springframework.jdbc.datasource.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Convenient super class for JDBC data access objects.
 * Requires a DataSource to be set, providing a
 * JdbcTemplate based on it to subclasses.
 *
 * <p>This base class is mainly intended for JdbcTemplate usage
 * but can also be used when working with DataSourceUtils directly
 * or with org.springframework.jdbc.object classes.
 *
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setDataSource
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.DataSourceUtils
 */
public abstract class JdbcDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private JdbcTemplate jdbcTemplate;

	/**
	 * Set the JDBC DataSource to be used by this DAO.
	 */
	public final void setDataSource(DataSource dataSource) {
	  this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Return the JDBC DataSource used by this DAO.
	 */
	protected final DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	/**
	 * Set the JdbcTemplate for this DAO explicitly,
	 * as an alternative to specifying a DataSource.
	 */
	public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Return the JdbcTemplate for this DAO,
	 * pre-initialized with the DataSource or set explicitly.
	 */
	protected final JdbcTemplate getJdbcTemplate() {
	  return jdbcTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.jdbcTemplate == null) {
			throw new IllegalArgumentException("dataSource or jdbcTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}

	/**
	 * Get a JDBC Connection, either from the current transaction or a new one.
	 * @return the JDBC Connection
	 * @throws CannotGetJdbcConnectionException if the attempt to get a Connection failed
	 */
	protected final Connection getConnection() throws CannotGetJdbcConnectionException {
		return DataSourceUtils.getConnection(getDataSource());
	}

	/**
	 * Return the SQLExceptionTranslator of this DAO's JdbcTemplate,
	 * for translating SQLExceptions in custom JDBC access code.
	 */
	protected final SQLExceptionTranslator getExceptionTranslator() {
		return this.jdbcTemplate.getExceptionTranslator();
	}

	/**
	 * Close the given JDBC Connection if necessary, created via this bean's
	 * DataSource, if it isn't bound to the thread.
	 * @param con Connection to close
	 * @throws CannotCloseJdbcConnectionException if the attempt to close the
	 * Connection failed
	 */
	protected final void closeConnectionIfNecessary(Connection con) throws CannotCloseJdbcConnectionException {
		DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
	}

}
