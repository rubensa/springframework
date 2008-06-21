/*
 * Copyright 2002-2008 the original author or authors.
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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of the standard JDBC {@link javax.sql.DataSource} interface,
 * configuring the plain old JDBC {@link java.sql.DriverManager} via bean properties, and
 * returning a new {@link java.sql.Connection} from every <code>getConnection</code> call.
 *
 * <p><b>NOTE: This class is not an actual connection pool; it does not actually
 * pool Connections.</b> It just serves as simple replacement for a full-blown
 * connection pool, implementing the same standard interface, but creating new
 * Connections on every call.
 *
 * <p>Useful for test or standalone environments outside of a J2EE container, either
 * as a DataSource bean in a corresponding ApplicationContext or in conjunction with
 * a simple JNDI environment. Pool-assuming <code>Connection.close()</code> calls will
 * simply close the Connection, so any DataSource-aware persistence code should work.
 *
 * <p><b>NOTE: Within special class loading environments such as OSGi, this class
 * is effectively superseded by {@link SimpleDriverDataSource} due to general class
 * loading issues with the JDBC DriverManager that be resolved through direct Driver
 * usage (which is exactly what SimpleDriverDataSource does).</b>
 *
 * <p>In a J2EE container, it is recommended to use a JNDI DataSource provided by
 * the container. Such a DataSource can be exposed as a DataSource bean in a Spring
 * ApplicationContext via {@link org.springframework.jndi.JndiObjectFactoryBean},
 * for seamless switching to and from a local DataSource bean like this class.
 * For tests, you can then either set up a mock JNDI environment through Spring's
 * {@link org.springframework.mock.jndi.SimpleNamingContextBuilder}, or switch the
 * bean definition to a local DataSource (which is simpler and thus recommended).
 *
 * <p>If you need a "real" connection pool outside of a J2EE container, consider
 * <a href="http://jakarta.apache.org/commons/dbcp">Apache's Jakarta Commons DBCP</a>
 * or <a href="http://sourceforge.net/projects/c3p0">C3P0</a>.
 * Commons DBCP's BasicDataSource and C3P0's ComboPooledDataSource are full
 * connection pool beans, supporting the same basic properties as this class
 * plus specific settings (such as minimal/maximal pool size etc).
 *
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see SimpleDriverDataSource
 */
public class DriverManagerDataSource extends AbstractDriverBasedDataSource {

	/**
	 * Constructor for bean-style configuration.
	 */
	public DriverManagerDataSource() {
	}

	/**
	 * Create a new DriverManagerDataSource with the given JDBC URL,
	 * not specifying a username or password for JDBC access.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url) {
		setUrl(url);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard
	 * DriverManager parameters.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @see java.sql.DriverManager#getConnection(String, String, String)
	 */
	public DriverManagerDataSource(String url, String username, String password) {
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create a new DriverManagerDataSource with the given JDBC URL,
	 * not specifying a username or password for JDBC access.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param conProps JDBC connection properties
	 * @see java.sql.DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url, Properties conProps) {
		setUrl(url);
		setConnectionProperties(conProps);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard
	 * DriverManager parameters.
	 * @param driverClassName the JDBC driver class name
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @deprecated since Spring 2.5. DriverManagerDataSource is primarily
	 * intended for accessing <i>pre-registered</i> JDBC drivers.
	 * If you need to register a new driver, consider using
	 * {@link SimpleDriverDataSource} instead.
	 */
	public DriverManagerDataSource(String driverClassName, String url, String username, String password) {
		setDriverClassName(driverClassName);
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}


	/**
	 * Set the JDBC driver class name. This driver will get initialized
	 * on startup, registering itself with the JDK's DriverManager.
	 * <p><b>NOTE: DriverManagerDataSource is primarily intended for accessing
	 * <i>pre-registered</i> JDBC drivers.</b> If you need to register a new driver,
	 * consider using {@link SimpleDriverDataSource} instead. Alternatively, consider
	 * initializing the JDBC driver yourself before instantiating this DataSource.
	 * The "driverClassName" property is mainly preserved for backwards compatibility,
	 * as well as for migrating between Commons DBCP and this DataSource.
	 * @see java.sql.DriverManager#registerDriver(java.sql.Driver)
	 * @see SimpleDriverDataSource
	 */
	public void setDriverClassName(String driverClassName) {
		Assert.hasText(driverClassName, "Property 'driverClassName' must not be empty");
		String driverClassNameToUse = driverClassName.trim();
		try {
			Class.forName(driverClassNameToUse, true, ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException ex) {
			IllegalStateException ise =
					new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]");
			ise.initCause(ex);
			throw ise;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Loaded JDBC driver: " + driverClassNameToUse);
		}
	}


	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		String url = getUrl();
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC DriverManager Connection to [" + url + "]");
		}
		return getConnectionFromDriverManager(url, props);
	}

	/**
	 * Getting a Connection using the nasty static from DriverManager is extracted
	 * into a protected method to allow for easy unit testing.
	 * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);
	}

}
