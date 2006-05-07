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

package org.springframework.test;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Convenient superclass for tests that should occur in a transaction, but normally
 * will roll the transaction back on the completion of each test.
 *
 * <p>This is useful in a range of circumstances, allowing the following benefits:
 * <ul>
 * <li>Ability to delete or insert any data in the database, without affecting other tests
 * <li>Providing a transactional context for any code requiring a transaction
 * <li>Ability to write anything to the database without any need to clean up.
 * </ul>
 *
 * <p>This class is typically very fast, compared to traditional setup/teardown scripts.
 *
 * <p>If data should be left in the database, call the <code>setComplete()</code>
 * method in each test. The "defaultRollback" property, which defaults to "true",
 * determines whether transactions will complete by default.
 *
 * <p>It is even possible to end the transaction early; for example, to verify lazy
 * loading behavior of an O/R mapping tool. (This is a valuable away to avoid
 * unexpected errors when testing a web UI, for example.)  Simply call the
 * <code>endTransaction()</code> method. Execution will then occur without a
 * transactional context.
 *
 * <p>The <code>startNewTransaction()</code> method may be called after a call to
 * <code>endTransaction()</code> if you wish to create a new transaction, quite
 * independent of the old transaction. The new transaction's default fate will be to
 * roll back, unless <code>setComplete()</code> is called again during the scope of the
 * new transaction. Any number of transactions may be created and ended in this way.
 * The final transaction will automatically be rolled back when the test case is
 * torn down.
 *
 * <p>Transactional behavior requires a single bean in the context implementing the
 * PlatformTransactionManager interface. This will be set by the superclass's
 * Dependency Injection mechanism. If using the superclass's Field Injection mechanism,
 * the implementation should be named "transactionManager". This mechanism allows the
 * use of this superclass even when there's more than one transaction manager in the context.
 * 
 * <p><i>This superclass can also be used without transaction management, if no
 * PlatformTransactionManager bean is found in the context provided. Be careful about
 * using this mode, as it allows the potential to permanently modify data.
 * This mode is available only if dependency checking is turned off in
 * the AbstractDependencyInjectionSpringContextTests superclass. The non-transactional
 * capability is provided to enable use of the same subclass in different environments.</i>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1.1
 */
public abstract class AbstractTransactionalSpringContextTests extends AbstractDependencyInjectionSpringContextTests {

	protected PlatformTransactionManager transactionManager;

	/**
	 * TransactionStatus for this test. Typical subclasses won't need to use it.
	 */
	protected TransactionStatus transactionStatus;

	private boolean defaultRollback = true;

	/**
	 * Should we commit this transaction?
	 */
	private boolean complete;

	/**
	 * Number of transactions started
	 */
	private int transactionsStarted;
	
	/**
	 * Default transaction definition is used.
	 * Subclasses can change this to cause different behaviour.
	 */
	private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();


	/**
	 * Default constructor for AbstractTransactionalSpringContextTests.
	 */
	public AbstractTransactionalSpringContextTests() {
	}

	/**
	 * Constructor for AbstractTransactionalSpringContextTests with a JUnit name.
	 */
	public AbstractTransactionalSpringContextTests(String name) {
		super(name);
	}
	
	/**
	 * Call in an overridden runBare() method to prevent transaction execution.
	 */
	protected void preventTransaction() {
		this.transactionDefinition = null;
	}
	
	
	/**
	 * Override the transaction attributes that will be used.
	 * Call in an overridden runBare() method so that setUp()
	 * and tearDown() behaviour are modified.
	 * @param customDefinition custom definition to override with
	 */
	protected void setTransactionDefinition(TransactionDefinition customDefinition) {
		this.transactionDefinition = customDefinition;
	}


	/**
	 * Subclasses can set this value in their constructor to change
	 * default, which is always to roll the transaction back.
	 */
	public void setDefaultRollback(boolean defaultRollback) {
		this.defaultRollback = defaultRollback;
	}

	/**
	 * The transaction manager to use. No transaction management will be available
	 * if this is not set. (This mode works only if dependency checking is turned off in
	 * the AbstractDependencyInjectionSpringContextTests superclass.)
	 * Populated by dependency injection by superclass.
	 */
	public void setTransactionManager(PlatformTransactionManager ptm) {
		this.transactionManager = ptm;
	}


	/**
	 * This implementation creates a transaction before test execution.
	 * Override <code>onSetUpBeforeTransaction</code> and/or
	 * <code>onSetUpInTransaction</code> to add custom set-up behavior.
	 * @see #onSetUpBeforeTransaction()
	 * @see #onSetUpInTransaction()
	 */
	protected final void onSetUp() throws Exception {
		this.complete = !this.defaultRollback;

		if (this.transactionManager == null) {
			logger.info("No transaction manager set: tests will NOT run within a transaction");
		}
		else if (this.transactionDefinition == null) {
			logger.info("Transaction definition is null: test " + getName() + " will NOT run within a transaction");
		}
		else {
			onSetUpBeforeTransaction();
			startNewTransaction();
			onSetUpInTransaction();
		}
	}


	/**
	 * Subclasses can override this method to perform any setup operations,
	 * such as populating a database table, <i>before</i> the transaction
	 * created by this class. Only invoked if there <i>is</i> a transaction--
	 * that is, if preventTransaction has not been invoked in an overridden
	 * runTest() method.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onSetUpBeforeTransaction() throws Exception {
	}

	/**
	 * Subclasses can override this method to perform any setup operations,
	 * such as populating a database table, <i>within</i> the transaction
	 * created by this class.
	 * <p><b>NB:</b> Not called if there is no transaction management, due to no
	 * transaction manager being provided in the context.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onSetUpInTransaction() throws Exception {
	}


	/**
	 * This implementation ends the transaction after test execution.
	 * Override <code>onTearDownInTransaction</code> and/or
	 * <code>onTearDownAfterTransaction</code> to add custom tear-down behavior.
	 * @throws Exception simply let any exception propagate
	 * @see #onTearDownInTransaction()
	 * @see #onTearDownAfterTransaction()
	 */
	protected final void onTearDown() throws Exception {
		try {
			onTearDownInTransaction();
		}
		finally {
			endTransaction();
		}

		onTearDownAfterTransaction();
	}

	/**
	 * Subclasses can override this method to run invariant tests here.
	 * The transaction is <i>still open</i>, so any changes made in the
	 * transaction will still be visible.
	 * There is no need to clean up the database, as rollback will follow automatically.
	 * <p><b>NB:</b> Not called if there is no transaction management, due to no
	 * transaction manager being provided in the context.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onTearDownInTransaction() throws Exception {
	}

	/**
	 * Subclasses can override this method to perform cleanup here.
	 * The transaction is <i>not open anymore</i> at this point.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onTearDownAfterTransaction() throws Exception {
	}


	/**
	 * Cause the transaction to commit for this test method,
	 * even if default is set to rollback.
	 * @throws UnsupportedOperationException if the operation cannot be set to
	 * complete as no transaction manager was provided
	 */
	protected void setComplete() throws UnsupportedOperationException {
		if (this.transactionManager == null) {
			throw new UnsupportedOperationException("Cannot set complete: no transaction manager");
		}
		this.complete = true;
	}

	/**
	 * Immediately force a commit or rollback of the transaction,
	 * according to the complete flag.
	 * <p>Can be used to explicitly let the transaction end early,
	 * for example to check whether lazy associations of persistent objects
	 * work outside of a transaction (i.e. have been initialized properly).
	 * @see #setComplete
	 */
	protected void endTransaction() {
		if (this.transactionStatus != null) {
			try {
				if (!this.complete) {
					this.transactionManager.rollback(this.transactionStatus);
					logger.info("Rolled back transaction after test execution");
				}
				else {
					this.transactionManager.commit(this.transactionStatus);
					logger.info("Committed transaction after test execution");
				}
			}
			finally {
				this.transactionStatus = null;
			}
		}
	}

	/**
	 * Start a new transaction. Only call this method if <code>endTransaction()</code>
	 * has been called. <code>setComplete()</code> can be used again in the new transaction.
	 * The fate of the new transaction, by default, will be the usual rollback.
	 * @see #endTransaction()
	 * @see #setComplete()
	 */
	protected void startNewTransaction() throws TransactionException {
		if (this.transactionStatus != null) {
			throw new IllegalStateException("Cannot start new transaction without ending existing transaction:" +
					"Invoke endTransaction() before startNewTransaction()");
		}

		this.transactionStatus = this.transactionManager.getTransaction(this.transactionDefinition);
		++this.transactionsStarted;
		this.complete = !this.defaultRollback;

		if (logger.isInfoEnabled()) {
			logger.info("Began transaction (" + this.transactionsStarted + "): transaction manager [" +
					this.transactionManager + "]; default rollback = " + this.defaultRollback);
		}
	}

}
