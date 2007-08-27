/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.junit38;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.utils.SimpleJdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Abstract {@link Transactional transactional} extension of
 * {@link AbstractJUnit38SpringContextTests} that also adds some convenience
 * functionality for JDBC access. Expects a {@link javax.sql.DataSource} bean to
 * be defined in the Spring {@link ApplicationContext application context}.
 * </p>
 * <p>
 * This class exposes a {@link SimpleJdbcTemplate} and provides an easy way to
 * delete from the database in a new transaction.
 * </p>
 * <p>
 * Concrete subclasses must fulfill the same requirements outlined in
 * {@link AbstractJUnit38SpringContextTests}.
 * </p>
 *
 * @see AbstractJUnit38SpringContextTests
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.TestExecutionListeners
 * @see org.springframework.test.context.transaction.TransactionalTestExecutionListener
 * @see org.springframework.test.context.transaction.TransactionConfiguration
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.test.annotation.NotTransactional
 * @see org.springframework.test.annotation.Rollback
 * @see org.springframework.test.utils.SimpleJdbcTestUtils
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
public class AbstractTransactionalJUnit38SpringContextTests extends AbstractJUnit38SpringContextTests {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected SimpleJdbcTemplate	simpleJdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Default <em>no argument</em> constructor which delegates to
	 * {@link AbstractTransactionalJUnit38SpringContextTests#AbstractTransactionalJUnit38SpringContextTests(String) AbstractTransactionalJUnit38SpringContextTests(String)},
	 * passing a value of <code>null</code> for the test name.
	 *
	 * @see AbstractTransactionalJUnit38SpringContextTests#AbstractTransactionalJUnit38SpringContextTests(String)
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractTransactionalJUnit38SpringContextTests() throws Exception {

		this(null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Delegates to
	 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}.
	 *
	 * @see AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String)
	 * @param name The name of the current test to execute.
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractTransactionalJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Set the DataSource, typically provided via Dependency Injection.
	 *
	 * @param dataSource The DataSource to inject.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {

		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Get the SimpleJdbcTemplate that this base class manages.
	 *
	 * @return the SimpleJdbcTemplate
	 */
	public final SimpleJdbcTemplate getSimpleJdbcTemplate() {

		return this.simpleJdbcTemplate;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName table name to count rows in
	 * @return the number of rows in the table
	 */
	protected int countRowsInTable(final String tableName) {

		return SimpleJdbcTestUtils.countRowsInTable(getSimpleJdbcTemplate(), tableName);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Convenience method for deleting all rows from the specified tables.
	 * </p>
	 * <p>
	 * Use with caution outside of a transaction!
	 * </p>
	 *
	 * @param names The names of the tables from which to delete.
	 * @return The total number of rows deleted from all specified tables.
	 */
	protected int deleteFromTables(final String... names) {

		return SimpleJdbcTestUtils.deleteFromTables(getSimpleJdbcTemplate(), names);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Execute the given SQL script.
	 * </p>
	 * <p>
	 * Use with caution outside of a transaction!
	 * </p>
	 *
	 * @param sqlResourcePath Spring resource path for the SQL script. Should
	 *        normally be loaded by classpath. There should be one statement per
	 *        line. Any semicolons will be removed. <b>Do not use this method to
	 *        execute DDL if you expect rollback.</b>
	 * @param continueOnError whether or not to continue without throwing an
	 *        exception in the event of an error.
	 * @throws DataAccessException if there is an error executing a statement
	 *         and continueOnError was <code>false</code>.
	 */
	protected void executeSqlScript(final String sqlResourcePath, final boolean continueOnError)
			throws DataAccessException {

		SimpleJdbcTestUtils.executeSqlScript(getSimpleJdbcTemplate(), getApplicationContext(), sqlResourcePath,
				continueOnError);
	}

	// ------------------------------------------------------------------------|

}
