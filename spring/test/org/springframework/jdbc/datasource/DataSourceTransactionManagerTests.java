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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Juergen Hoeller
 * @since 04.07.2003
 */
public class DataSourceTransactionManagerTests extends TestCase {
	
	public void testTransactionCommitWithAutoCommitTrue() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(true, false, false);
	}
	
	public void testTransactionCommitWithAutoCommitFalse() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(false, false, false);
	}

	public void testTransactionCommitWithAutoCommitTrueAndLazyConnection() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(true, true, false);
	}

	public void testTransactionCommitWithAutoCommitFalseAndLazyConnection() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(false, true, false);
	}

	public void testTransactionCommitWithAutoCommitTrueAndLazyConnectionAndStatementCreated() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(true, true, true);
	}

	public void testTransactionCommitWithAutoCommitFalseAndLazyConnectionAndStatementCreated() throws Exception {
		doTestTransactionCommitRestoringAutoCommit(false, true, true);
	}

	private void doTestTransactionCommitRestoringAutoCommit(
			boolean autoCommit, boolean lazyConnection, final boolean createStatement)
			throws Exception {

		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();

		if (lazyConnection) {
			ds.getConnection();
			dsControl.setReturnValue(con, 1);
			con.getAutoCommit();
			conControl.setReturnValue(autoCommit, 1);
			con.getTransactionIsolation();
			conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED, 1);
			con.close();
			conControl.setVoidCallable(1);
		}

		if (!lazyConnection || createStatement) {
			ds.getConnection();
			dsControl.setReturnValue(con, 1);
			con.getAutoCommit();
			conControl.setReturnValue(autoCommit, 1);
			if (autoCommit) {
				// Must disable autocommit
				con.setAutoCommit(false);
				conControl.setVoidCallable(1);
			}
			if (createStatement) {
				con.createStatement();
				conControl.setReturnValue(null, 1);
			}
			con.commit();
			conControl.setVoidCallable(1);
			con.isReadOnly();
			conControl.setReturnValue(false, 1);
			if (autoCommit) {
				// must restore autoCommit
				con.setAutoCommit(true);
				conControl.setVoidCallable(1);
			}
			con.close();
			conControl.setVoidCallable(1);
		}

		conControl.replay();
		dsControl.replay();

		final DataSource dsToUse = (lazyConnection ? new LazyConnectionDataSourceProxy(ds) : ds);
		PlatformTransactionManager tm = new DataSourceTransactionManager(dsToUse);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(dsToUse));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(dsToUse));
				assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
				assertTrue("Is new transaction", status.isNewTransaction());
				Connection tCon = DataSourceUtils.getConnection(dsToUse);
				try {
					if (createStatement) {
						tCon.createStatement();
						assertEquals(con, new CommonsDbcpNativeJdbcExtractor().getNativeConnection(tCon));
					}
				}
				catch (SQLException ex) {
					throw new UncategorizedSQLException("", "", ex);
				}
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(dsToUse));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		conControl.verify();
		dsControl.verify();
	}
	
	public void testTransactionRollbackWithAutoCommitTrue() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(true, false, false);
	}
	
	public void testTransactionRollbackWithAutoCommitFalse() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(false, false, false);
	}

	public void testTransactionRollbackWithAutoCommitTrueAndLazyConnection() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(true, true, false);
	}

	public void testTransactionRollbackWithAutoCommitFalseAndLazyConnection() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(false, true, false);
	}

	public void testTransactionRollbackWithAutoCommitTrueAndLazyConnectionAndCreateStatement() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(true, true, true);
	}

	public void testTransactionRollbackWithAutoCommitFalseAndLazyConnectionAndCreateStatement() throws Exception  {
		doTestTransactionRollbackRestoringAutoCommit(false, true, true);
	}

	private void doTestTransactionRollbackRestoringAutoCommit(
			boolean autoCommit, boolean lazyConnection, final boolean createStatement) throws Exception {

		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();

		if (lazyConnection) {
			ds.getConnection();
			dsControl.setReturnValue(con, 1);
			con.getAutoCommit();
			conControl.setReturnValue(autoCommit, 1);
			con.getTransactionIsolation();
			conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED, 1);
			con.close();
			conControl.setVoidCallable(1);
		}

		if (!lazyConnection || createStatement) {
			ds.getConnection();
			dsControl.setReturnValue(con, 1);
			con.getAutoCommit();
			conControl.setReturnValue(autoCommit, 1);
			if (autoCommit) {
				// Must disable autocommit
				con.setAutoCommit(false);
				conControl.setVoidCallable(1);
			}
			if (createStatement) {
				con.createStatement();
				conControl.setReturnValue(null, 1);
			}
			con.rollback();
			conControl.setVoidCallable(1);
			con.isReadOnly();
			conControl.setReturnValue(false, 1);
			if (autoCommit) {
				// Must restore autocommit
				con.setAutoCommit(true);
				conControl.setVoidCallable(1);
			}
			con.close();
			conControl.setVoidCallable(1);
		}

		conControl.replay();
		dsControl.replay();

		final DataSource dsToUse = (lazyConnection ? new LazyConnectionDataSourceProxy(ds) : ds);
		PlatformTransactionManager tm = new DataSourceTransactionManager(dsToUse);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(dsToUse));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		final RuntimeException ex = new RuntimeException("Application exception");
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
					assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(dsToUse));
					assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue("Is new transaction", status.isNewTransaction());
					Connection con = DataSourceUtils.getConnection(dsToUse);
					if (createStatement) {
						try {
							con.createStatement();
						}
						catch (SQLException ex) {
							throw new UncategorizedSQLException("", "", ex);
						}
					}
					throw ex;
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex2) {
			// expected
			assertTrue("Correct exception thrown", ex2.equals(ex));
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionRollbackOnly() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		conControl.replay();
		dsControl.replay();

		DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
		tm.setTransactionSynchronization(DataSourceTransactionManager.SYNCHRONIZATION_NEVER);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		TransactionSynchronizationManager.bindResource(ds, new ConnectionHolder(con));
		final RuntimeException ex = new RuntimeException("Application exception");
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
					assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
					assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue("Is existing transaction", !status.isNewTransaction());
					throw ex;
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex2) {
			// expected
			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
			//ex2.printStackTrace();
			assertEquals("Correct exception thrown", ex, ex2);
		}
		finally {
			TransactionSynchronizationManager.unbindResource(ds);
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testExistingTransaction() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		con.rollback();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				tt.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
						assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
						assertTrue("Is existing transaction", !status.isNewTransaction());
						status.setRollbackOnly();
					}
				});
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testPropagationRequiresNewWithExistingTransaction() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 2);
		con.rollback();
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 2);
		con.close();
		conControl.setVoidCallable(2);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 2);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
				tt.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
						assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
						assertTrue("Is new transaction", status.isNewTransaction());
						status.setRollbackOnly();
					}
				});
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testPropagationNotSupportedWithExistingTransaction() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
				tt.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
						assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
						assertTrue("Isn't new transaction", !status.isNewTransaction());
						status.setRollbackOnly();
					}
				});
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testPropagationNeverWithExistingTransaction() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		con.rollback();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
					assertTrue("Is new transaction", status.isNewTransaction());
					tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
					tt.execute(new TransactionCallbackWithoutResult() {
						protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
							fail("Should have thrown IllegalTransactionStateException");
						}
					});
					fail("Should have thrown IllegalTransactionStateException");
				}
			});
		}
		catch (IllegalTransactionStateException ex) {
			// expected
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionWithIsolation() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getTransactionIsolation();
		conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED, 1);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conControl.setVoidCallable(1);
		con.getAutoCommit();
		conControl.setReturnValue(true, 1);
		con.setAutoCommit(false);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.setAutoCommit(true);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conControl.setVoidCallable(1);
		con.close();
		conControl.setVoidCallable(1);

		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// something transactional
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionWithLongTimeout() throws Exception {
		doTestTransactionWithTimeout(10);
	}

	public void testTransactionWithShortTimeout() throws Exception {
		doTestTransactionWithTimeout(1);
	}

	private void doTestTransactionWithTimeout(int timeout) throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setReturnValue(true, 1);
		con.setAutoCommit(false);
		conControl.setVoidCallable(1);
		con.prepareStatement("some SQL statement");
		conControl.setReturnValue(ps, 1);
		if (timeout >= 2) {
			ps.setQueryTimeout(timeout - 2);
			psControl.setVoidCallable(1);
			con.commit();
		}
		else {
			con.rollback();
		}
		conControl.setVoidCallable(1);
		con.setAutoCommit(true);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		psControl.replay();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setTimeout(timeout);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));

		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						Thread.sleep(2000);
					}
					catch (InterruptedException ex) {
					}
					try {
						Connection con = DataSourceUtils.getConnection(ds);
						PreparedStatement ps = con.prepareStatement("some SQL statement");
						DataSourceUtils.applyTransactionTimeout(ps, ds);
					}
					catch (SQLException ex) {
						throw new DataAccessResourceFailureException("", ex);
					}
				}
			});
			if (timeout < 2) {
				fail("Should have thrown TransactionTimedOutException");
			}
		}
		catch (TransactionTimedOutException ex) {
			if (timeout < 2) {
				// expected
			}
			else {
				throw ex;
			}
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
		psControl.verify();
	}

	public void testTransactionAwareDataSourceProxy() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setReturnValue(true, 1);
		con.setAutoCommit(false);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.setAutoCommit(true);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// something transactional
				assertEquals(con, DataSourceUtils.getConnection(ds));
				TransactionAwareDataSourceProxy dsProxy = new TransactionAwareDataSourceProxy(ds);
				try {
					assertEquals(con, ((ConnectionProxy) dsProxy.getConnection()).getTargetConnection());
					assertEquals(con, new CommonsDbcpNativeJdbcExtractor().getNativeConnection(dsProxy.getConnection()));
					// should be ignored
					dsProxy.getConnection().close();
				}
				catch (SQLException ex) {
					throw new UncategorizedSQLException("", "", ex);
				}
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	/**
	 * Test behavior if the first operation on a connection (getAutoCommit) throws SQLException.
	 */
	public void testTransactionWithExceptionOnBegin() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setThrowable(new SQLException("Cannot begin"));
		con.close();
		conControl.setVoidCallable();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown CannotCreateTransactionException");
		}
		catch (CannotCreateTransactionException ex) {
			// expected
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
	}

	public void testTransactionWithExceptionOnCommit() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		// No need to restore it
		conControl.setReturnValue(false, 1);
		con.commit();
		conControl.setThrowable(new SQLException("Cannot commit"), 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
	}

	public void testTransactionWithExceptionOnCommitAndRollbackOnCommitFailure() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		// No need to change or restore
		conControl.setReturnValue(false);
		con.commit();
		conControl.setThrowable(new SQLException("Cannot commit"), 1);
		con.rollback();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);
		conControl.replay();
		dsControl.replay();

		DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
		tm.setRollbackOnCommitFailure(true);
		TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
	}

	public void testTransactionWithExceptionOnRollback() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setReturnValue(true, 1);
		// Must restore
		con.setAutoCommit(false);
		conControl.setVoidCallable(1);
				
		con.rollback();
		conControl.setThrowable(new SQLException("Cannot rollback"), 1);
		con.setAutoCommit(true);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
					status.setRollbackOnly();
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
	}

	public void testJtaTransactionCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		JtaTransactionManager ptm = new JtaTransactionManager(ut);
		TransactionTemplate tt = new TransactionTemplate(ptm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
				assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
				assertTrue("Is new transaction", status.isNewTransaction());

				Connection con = DataSourceUtils.getConnection(ds);
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				DataSourceUtils.closeConnectionIfNecessary(con, ds);

				con = DataSourceUtils.getConnection(ds);
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				DataSourceUtils.closeConnectionIfNecessary(con, ds);			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionWithPropagationSupports() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
				assertTrue("Is not new transaction", !status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		dsControl.verify();
	}

	public void testTransactionWithPropagationNotSupported() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
				assertTrue("Is not new transaction", !status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		dsControl.verify();
	}

	public void testTransactionWithPropagationNever() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NEVER);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
				assertTrue("Is not new transaction", !status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		dsControl.verify();
	}

	public void testExistingTransactionWithPropagationNested() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();

		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.releaseSavepoint(sp);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);
		ds.getConnection();
		dsControl.setReturnValue(con, 1);

		spControl.replay();
		mdControl.replay();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				tt.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
						assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
						assertTrue("Is nested transaction", status.isNewTransaction());
					}
				});
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		spControl.verify();
		mdControl.verify();
		conControl.verify();
		dsControl.verify();
	}

	public void testExistingTransactionWithPropagationNestedAndRollback() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.rollback(sp);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		spControl.replay();
		mdControl.replay();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				tt.execute(new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
						assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
						assertTrue("Is nested transaction", status.isNewTransaction());
						status.setRollbackOnly();
					}
				});
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		spControl.verify();
		mdControl.verify();
		conControl.verify();
		dsControl.verify();
	}

	public void testExistingTransactionWithManualSavepoint() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();

		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.releaseSavepoint(sp);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);
		ds.getConnection();
		dsControl.setReturnValue(con, 1);

		spControl.replay();
		mdControl.replay();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				Object savepoint = status.createSavepoint();
				status.releaseSavepoint(savepoint);
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		spControl.verify();
		mdControl.verify();
		conControl.verify();
		dsControl.verify();
	}

	public void testExistingTransactionWithManualSavepointAndRollback() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.rollback(sp);
		conControl.setVoidCallable(1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		spControl.replay();
		mdControl.replay();
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				Object savepoint = status.createSavepoint();
				status.rollbackToSavepoint(savepoint);
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		spControl.verify();
		mdControl.verify();
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionWithPropagationNested() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		con.commit();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	public void testTransactionWithPropagationNestedAndRollback() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		con.getAutoCommit();
		conControl.setReturnValue(false, 1);
		con.rollback();
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		con.close();
		conControl.setVoidCallable(1);

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		conControl.replay();
		dsControl.replay();

		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) throws RuntimeException {
				assertTrue("Is new transaction", status.isNewTransaction());
				status.setRollbackOnly();
			}
		});

		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		conControl.verify();
		dsControl.verify();
	}

	protected void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}
