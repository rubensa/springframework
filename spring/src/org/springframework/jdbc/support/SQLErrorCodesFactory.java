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

package org.springframework.jdbc.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Factory for creating SQLErrorCodes based on the
 * "databaseProductName" taken from the DatabaseMetaData.
 *
 * <p>Returns SQLErrorCodes populated with vendor codes
 * defined in a configuration file named "sql-error-codes.xml".
 * Reads the default file in this package if not overridden by a file in
 * the root of the class path (e.g. in the "/WEB-INF/classes" directory).
 *
 * @author Thomas Risberg
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.sql.DatabaseMetaData#getDatabaseProductName
 */
public class SQLErrorCodesFactory {

	protected static final Log logger = LogFactory.getLog(SQLErrorCodesFactory.class);

	/**
	 * Name of custom SQL error codes file, loading from the root
	 * of the class path (e.g. in the "WEB-INF/classes" directory).
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "sql-error-codes.xml";

	/**
	 * Name of default SQL error code files, loading from the class path.
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "org/springframework/jdbc/support/sql-error-codes.xml";

	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 */
	private static final SQLErrorCodesFactory instance = new SQLErrorCodesFactory();

	/**
	 * Return singleton instance.
	 */
	public static SQLErrorCodesFactory getInstance() {
		return instance;
	}


	/**
	 * Map to hold database product name retrieved from database metadata.
	 * Key is the DataSource, value is the database product name.
	 */
	private final Map dataSourceProductNames = new HashMap(10);

	/**
	 * Map to hold error codes for all databases defined in the config file.
	 * Key is the database product name, value is the SQLErrorCodes instance.
	 */
	private final Map rdbmsErrorCodes;

	/**
	 * Not public to enforce Singleton design pattern.
	 * Would be private except to allow testing via overriding the loadResource method.
	 * <b>Do not subclass in application code.</b>
	 * @see #loadResource
	 */
	protected SQLErrorCodesFactory() {
		Map errorCodes = null;

		try {
			String path = SQL_ERROR_CODE_OVERRIDE_PATH;
			Resource resource = loadResource(path);
			if (resource == null || !resource.exists()) {
				path = SQL_ERROR_CODE_DEFAULT_PATH;
				resource = loadResource(path);
				if (resource == null || !resource.exists()) {
					throw new BeanDefinitionStoreException(
							"Unable to locate file [" + SQL_ERROR_CODE_DEFAULT_PATH  + "]");
				}
			}
			XmlBeanFactory bf = new XmlBeanFactory(resource);
			Map errorCodeBeans = bf.getBeansOfType(SQLErrorCodes.class, true, false);
			errorCodes = new HashMap(errorCodeBeans.size());

			for (Iterator it = errorCodeBeans.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String rdbmsName = (String) entry.getKey();
				SQLErrorCodes ec = (SQLErrorCodes) entry.getValue();

				if (ec.getBadSqlGrammarCodes() == null) {
					ec.setBadSqlGrammarCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getBadSqlGrammarCodes());
				}
				if (ec.getDataIntegrityViolationCodes() == null) {
					ec.setDataIntegrityViolationCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getDataIntegrityViolationCodes());
				}
				if (ec.getDataRetrievalFailureCodes() == null) {
					ec.setDataRetrievalFailureCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getDataRetrievalFailureCodes());
				}
				if (ec.getOptimisticLockingFailureCodes() == null) {
					ec.setOptimisticLockingFailureCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getOptimisticLockingFailureCodes());
				}
				if (ec.getCannotAcquireLockCodes() == null) {
					ec.setCannotAcquireLockCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getCannotAcquireLockCodes());
				}
				if (ec.getDataAccessResourceFailureCodes() == null) {
					ec.setDataAccessResourceFailureCodes(new String[0]);
				}
				else {
					Arrays.sort(ec.getDataAccessResourceFailureCodes());
				}
				if (!ec.getCustomTranslations().isEmpty()) {
					Iterator customIter = ec.getCustomTranslations().iterator();
					while (customIter.hasNext()) {
						CustomSQLErrorCodesTranslation customCode = (CustomSQLErrorCodesTranslation) customIter.next();
						Arrays.sort(customCode.getErrorCodes());
					}
				}

				if (ec.getDatabaseProductName() == null) {
					errorCodes.put(rdbmsName, ec);
				}
				else {
					errorCodes.put(ec.getDatabaseProductName(), ec);
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("SQLErrorCodes loaded: " + errorCodes.keySet());
			}
		}
		catch (BeanDefinitionStoreException ex) {
			logger.warn("Error loading error codes from config file. Message: " + ex.getMessage());
			errorCodes = new HashMap(0);
		}

		this.rdbmsErrorCodes = errorCodes;
	}
	
	/**
	 * Protected for testability. Load the given resource from the class path.
	 * @param path resource path. SQL_ERROR_CODE_DEFAULT_PATH or
	 * SQL_ERROR_CODE_OVERRIDE_PATH.
	 * <b>Not to be overridden by application developers, who should obtain instances
	 * of this class from the static getInstance() method.</b>
	 * @return the input stream or null if the resource wasn't found
	 * @see #getInstance
	 */
	protected Resource loadResource(String path) {
		return new ClassPathResource(path);
	}

	/**
	 * Return SQLErrorCodes for the given DataSource,
	 * evaluating databaseProductName from DatabaseMetaData,
	 * or an empty error codes instance if no SQLErrorCodes were found.
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName
	 */
	public SQLErrorCodes getErrorCodes(DataSource dataSource) {
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up default SQLErrorCodes for DataSource [" + dataSource + "]");
		}

		// Let's avoid looking up database product info if we can.
		String dataSourceDbName = (String) this.dataSourceProductNames.get(dataSource);
		if (dataSourceDbName != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Database product name found in cache for DataSource [" + dataSource +
				    "]: name is '" + dataSourceDbName + "'");
			}
			return getErrorCodes(dataSourceDbName);
		}

		// We could not find it - got to look it up.
		try {
			String dbName = (String)
					JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName");

			if (dbName != null) {
				// Special check for DB2 -- !!! DEPRECATED AS OF Spring 1.1 !!!
				// !!! This will be removed in a future version !!!
				// We have added wildcard support so you should add a
				// <property name="databaseProductName"><value>DB2*</value></property>
				// entry to your custom sql-error-cdes.xml instead.
				if (dbName.startsWith("DB2")) {
					dbName = "DB2";
				}

				// Special check for wild card match:W we can match on a database name like
				// 'DB2*' meaning the database name starts with 'DB2', or '*DB2' for ends
				// with 'DB2', or even '*DB2*' for contains 'DB2'.
				Iterator dbNameIter = this.rdbmsErrorCodes.keySet().iterator();
				while (dbNameIter.hasNext()) {
					String checkDbName = (String) dbNameIter.next();
					if (checkDbName != null && (checkDbName.startsWith("*") || checkDbName.endsWith("*"))) {
						if (checkDbName.startsWith("*") && checkDbName.endsWith("*")) {
							if (dbName.indexOf(checkDbName.substring(1, checkDbName.length() - 1)) >= 0) {
								dbName = checkDbName;
							}
						}
						else if (checkDbName.startsWith("*")) {
							if (dbName.endsWith(checkDbName.substring(1, checkDbName.length()))) {
								dbName = checkDbName;
							}
						}
						else if (checkDbName.endsWith("*")) {
							if (dbName.startsWith(checkDbName.substring(0, checkDbName.length() - 1))) {
								dbName = checkDbName;
							}
						}
					}
				}

				this.dataSourceProductNames.put(dataSource, dbName);
				if (logger.isDebugEnabled()) {
					logger.debug("Database product name cached for DataSource [" + dataSource +
							"]: name is '" + dbName + "'");
				}
				return getErrorCodes(dbName);
			}
		}
		catch (MetaDataAccessException ex) {
			logger.warn("Error while extracting database product name - falling back to empty error codes");
		}

		// fallback is to return an empty ErrorCodes instance
		return new SQLErrorCodes();
	}

	/**
	 * Return SQLErrorCodes instance for the given database.
	 * No need for a database metadata lookup.
	 */
	public SQLErrorCodes getErrorCodes(String dbName) {
		SQLErrorCodes sec = (SQLErrorCodes) this.rdbmsErrorCodes.get(dbName);
		if (sec != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL error codes for '" + dbName + "' found");
			}
			return sec;
		}
		// could not find the database among the defined ones
		if (logger.isInfoEnabled()) {
			logger.info("SQL error codes for '" + dbName + "' not found");
		}
		return new SQLErrorCodes();
	}

}
