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

package org.springframework.jdbc.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Proxy for a target DataSource, fetching actual JDBC Connections lazily,
 * i.e. not until first creation of a Statement. Connection initialization
 * properties like auto-commit mode, transaction isolation and read-only mode
 * will be kept and applied to the actual JDBC Connection as soon as an
 * actual Connection is fetched (if ever). Consequently, commit and rollback
 * calls will be ignored if no Statements have been created.
 *
 * <p>This DataSource proxy allows to avoid fetching JDBC Connections from
 * a pool unless actually necessary. JDBC transaction control can happen
 * without fetching a Connection from the pool or communicating with the
 * database; this will be done lazily on first creation of a JDBC Statement.
 *
 * <p>Lazy fetching of physical JDBC Connections is particularly beneficial
 * in a generic transaction demarcation environment. It allows you to demarcate
 * transactions on all methods that could potentially perform data access,
 * without paying a performance penalty if no actual data access happens.
 *
 * <p><b>This DataSource proxy gives you behavior analogous to JTA and a
 * transactional JNDI DataSource (as provided by the J2EE server), even
 * with a local transaction strategy like DataSourceTransactionManager or
 * HibernateTransactionManager.</b> It does not add value with Spring's
 * JtaTransactionManager as transaction strategy.
 *
 * <p>Lazy fetching of JDBC Connections is also recommended for read-only
 * operations with Hibernate, in particular if the chances of resolving the
 * result in the second-level cache are high.</b> This avoids the need to
 * communicate with the database at all for such read-only operations.
 * You will get the same effect with non-transactional reads, but lazy fetching
 * of JDBC Connections allows you to still perform reads in transactions.
 *
 * <p><b>NOTE:</b> This DataSource proxy needs to return wrapped Connections to
 * handle lazy fetching of an actual JDBC Connection. Therefore, the returned
 * Connections cannot be cast to a native JDBC Connection type like OracleConnection,
 * or to a connection pool implementation type. Use a corresponding
 * NativeJdbcExtractor to retrieve the native JDBC Connection.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see ConnectionProxy
 * @see DataSourceTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
 */
public class LazyConnectionDataSourceProxy extends DelegatingDataSource {

	private Boolean defaultAutoCommit;

	private Integer defaultTransactionIsolation;


	/**
	 * Create a new LazyConnectionDataSourceProxy.
	 * @see #setTargetDataSource
	 */
	public LazyConnectionDataSourceProxy() {
	}

	/**
	 * Create a new LazyConnectionDataSourceProxy.
	 * @param targetDataSource the target DataSource
	 */
	public LazyConnectionDataSourceProxy(DataSource targetDataSource) {
		setTargetDataSource(targetDataSource);
		afterPropertiesSet();
	}

	/**
	 * Set the default auto-commit mode to expose when no target Connection
	 * has been fetched yet (-> actual JDBC Connection default not known yet).
	 * <p>Default gets determined by checking a target Connection on startup.
	 * Fallback default (in case of connect failure on startup) is true.
	 * @see java.sql.Connection#getAutoCommit
	 */
	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = new Boolean(defaultAutoCommit);
	}

	/**
	 * Set the default transaction isolation level to expose when no target Connection
	 * has been fetched yet (-> actual JDBC Connection default not known yet).
	 * <p>Default gets determined by checking a target Connection on startup.
	 * Fallback default (in case of connect failure on startup) is READ_COMMITTED.
	 * @see java.sql.Connection#getTransactionIsolation
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		this.defaultTransactionIsolation = new Integer(defaultTransactionIsolation);
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// Determine default auto-commit and transaction isolation
		// via a Connection from the target DataSource, if possible.
		if (this.defaultAutoCommit == null || this.defaultTransactionIsolation == null) {
			try {
				Connection con = getTargetDataSource().getConnection();
				try {
					if (this.defaultAutoCommit == null) {
						this.defaultAutoCommit = new Boolean(con.getAutoCommit());
					}
					if (this.defaultTransactionIsolation == null) {
						this.defaultTransactionIsolation = new Integer(con.getTransactionIsolation());
					}
				}
				finally {
					con.close();
				}
			}
			catch (SQLException ex) {
				logger.warn("Could not retrieve default auto-commit and transaction isolation settings", ex);
			}
		}
	}


	/**
	 * Return a Connection handle that lazily fetches an actual JDBC Connection
	 * when asked for a Statement (or PreparedStatement or CallableStatement).
	 * <p>The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * @return a transactional Connection if any, a new one else
	 * @see DataSourceUtils#doGetConnection
	 * @see ConnectionProxy#getTargetConnection
	 */
	public Connection getConnection() throws SQLException {
		return getLazyConnectionProxy(getTargetDataSource());
	}

	/**
	 * Return a Connection proxy that delegates every method call to a target
	 * Connection from the given DataSource, but does not fetch a physical
	 * JDBC Connection until first creation of a Statement.
	 * @param dataSource DataSource to get an actual Connection from
	 * @return the wrapped Connection
	 * @see DataSourceUtils#doCloseConnectionIfNecessary
	 */
	protected Connection getLazyConnectionProxy(DataSource dataSource) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new LazyConnectionInvocationHandler(dataSource, this.defaultAutoCommit, this.defaultTransactionIsolation));
	}


	/**
	 * Invocation handler that defers fetching an actual JDBC Connection
	 * until first creation of a Statement.
	 */
	private static class LazyConnectionInvocationHandler implements InvocationHandler {

		private final DataSource dataSource;

		private Connection target;

		private Boolean readOnly = Boolean.FALSE;

		private Integer transactionIsolation;

		private Integer defaultTransactionIsolation;

		private Boolean autoCommit;

		private boolean closed = false;

		public LazyConnectionInvocationHandler(DataSource dataSource, Boolean autoCommit, Integer transactionIsolation) {
			this.dataSource = dataSource;
			this.autoCommit = (autoCommit != null ? autoCommit : Boolean.TRUE);
			this.transactionIsolation = (transactionIsolation != null ?
					transactionIsolation : new Integer(Connection.TRANSACTION_READ_COMMITTED));
			this.defaultTransactionIsolation = this.transactionIsolation;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			// Handle getTargetConnection method: return underlying connection.
			if (method.getName().equals("getTargetConnection")) {
				return getTargetConnection();
			}

			if (!hasTargetConnection()) {
				// No physical target Connection kept yet ->
				// resolve transaction demarcation methods without fetching
				// a physical JDBC Connection until absolutely necessary.

				if (method.getName().equals("isReadOnly")) {
					return this.readOnly;
				}
				else if (method.getName().equals("setReadOnly")) {
					this.readOnly = (Boolean) args[0];
					return null;
				}
				else if (method.getName().equals("getTransactionIsolation")) {
					return this.transactionIsolation;
				}
				else if (method.getName().equals("setTransactionIsolation")) {
					this.transactionIsolation = (Integer) args[0];
					return null;
				}
				else if (method.getName().equals("getAutoCommit")) {
					return this.autoCommit;
				}
				else if (method.getName().equals("setAutoCommit")) {
					this.autoCommit = (Boolean) args[0];
					return null;
				}
				else if (method.getName().equals("commit")) {
					// Ignore: no statements created yet.
					return null;
				}
				else if (method.getName().equals("rollback")) {
					// Ignore: no statements created yet.
					return null;
				}
				else if (method.getName().equals("getWarnings")) {
					return null;
				}
				else if (method.getName().equals("clearWarnings")) {
					return null;
				}
				else if (method.getName().equals("isClosed")) {
					return new Boolean(this.closed);
				}
				else if (method.getName().equals("close")) {
					// Ignore: no target connection yet.
					this.closed = true;
					return null;
				}
				else if (this.closed) {
					// Connection proxy closed, without ever having fetched a
					// physical JDBC Connection: throw corresponding SQLException.
					throw new SQLException("Illegal operation: connection is closed");
				}
			}

			// Target Connection already fetched,
			// or target Connection necessary for current operation ->
			// invoke method on target connection.
			try {
				return method.invoke(getTargetConnection(), args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Return whether the proxy currently holds a target Connection.
		 */
		protected boolean hasTargetConnection() {
			return (this.target != null);
		}

		/**
		 * Return the target Connection, fetching it and initializing it if necessary.
		 */
		protected Connection getTargetConnection() throws SQLException {
			if (this.target == null) {
				// Fetch physical Connection from DataSource.
				this.target = this.dataSource.getConnection();
				// Apply kept transaction settings, if any.
				if (this.readOnly.booleanValue()) {
					this.target.setReadOnly(this.readOnly.booleanValue());
				}
				if (!this.transactionIsolation.equals(this.defaultTransactionIsolation)) {
					this.target.setTransactionIsolation(this.transactionIsolation.intValue());
				}
				if (this.autoCommit.booleanValue() != this.target.getAutoCommit()) {
					this.target.setAutoCommit(this.autoCommit.booleanValue());
				}
			}
			return this.target;
		}
	}

}
