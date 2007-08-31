/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic implementation for the {@link CallMetaDataProvider} interface.
 * This class can be extended to provide database specific behavior.
 * This class is intended for internal use by the Simple JDBC classes.
 *
 * @author Thomas Risberg
 * @since 2.1
 */
public class GenericCallMetaDataProvider implements CallMetaDataProvider {

	/** Logger available to subclasses */
	protected static final Log logger = LogFactory.getLog(CallMetaDataProvider.class);

	private boolean procedureColumnMetaDataUsed = false;

	private String userName;

	private boolean supportsCatalogsInProcedureCalls = true;

	private boolean supportsSchemasInProcedureCalls = true;

	private boolean storesUpperCaseIdentifiers = true;

	private boolean storesLowerCaseIdentifiers = false;

	private List<CallParameterMetaData> callParameterMetaData = new ArrayList<CallParameterMetaData>();

	/**
	 * Constructor used to initialize with provided database meta data.
	 * @param databaseMetaData meta data to be used
	 * @throws SQLException
	 */
	protected GenericCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		userName = databaseMetaData.getUserName();
	}

	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {

		try {
			setSupportsCatalogsInProcedureCalls(databaseMetaData.supportsCatalogsInProcedureCalls());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.supportsCatalogsInProcedureCalls' - " + se.getMessage());
		}
		try {
			setSupportsSchemasInProcedureCalls(databaseMetaData.supportsSchemasInProcedureCalls());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.supportsSchemasInProcedureCalls' - " + se.getMessage());
		}
		try {
			setStoresUpperCaseIdentifiers(databaseMetaData.storesUpperCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesUpperCaseIdentifiers' - " + se.getMessage());
		}
		try {
			setStoresLowerCaseIdentifiers(databaseMetaData.storesLowerCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesLowerCaseIdentifiers' - " + se.getMessage());
		}

	}

	public void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String procedureName)
			throws SQLException {

		procedureColumnMetaDataUsed = true;

		processProcedureColumns(databaseMetaData, catalogName, schemaName,  procedureName);

	}


	public List<CallParameterMetaData> getCallParameterMetaData() {
		return callParameterMetaData;
	}

	public String procedureNameToUse(String procedureName) {
		if (procedureName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return procedureName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return procedureName.toLowerCase();
		else
			return procedureName;
	}

	public String catalogNameToUse(String catalogName) {
		if (catalogName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return catalogName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return catalogName.toLowerCase();
		else
		return catalogName;
	}

	public String schemaNameToUse(String schemaName) {
		if (schemaName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return schemaName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return schemaName.toLowerCase();
		else
		return schemaName;
	}

	public String metaDataCatalogNameToUse(String catalogName) {
		if (isSupportsCatalogsInProcedureCalls())
			return catalogNameToUse(catalogName);
		else
			return null;
	}

	public String metaDataSchemaNameToUse(String schemaName) {
		if (isSupportsSchemasInProcedureCalls())
			return schemaNameToUse(schemaName);
		else
			return null;
	}

	public String parameterNameToUse(String parameterName) {
		if (parameterName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return parameterName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return parameterName.toLowerCase();
		else
		return parameterName;
	}

	public boolean byPassReturnParameter(String parameterName) {
		return false;
	}

	public SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlOutParameter(parameterName, meta.getSqlType());
	}

	public SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlParameter(parameterName, meta.getSqlType());		
	}

	public String getUserName() {
		return userName;
	}

	public boolean isReturnResultSetSupported() {
		return true;
	}

	public boolean isRefCursorSupported() {
		return false;
	}

	public int getRefCursorSqlType() {
		return Types.OTHER;
	}

	public boolean isProcedureColumnMetaDataUsed() {
		return procedureColumnMetaDataUsed;
	}


	/**
	 * Does the database support the use of catalog name in procedure calls
	 */
	protected boolean isSupportsCatalogsInProcedureCalls() {
		return supportsCatalogsInProcedureCalls;
	}

	/**
	 * Specify whether the database supports the use of catalog name in procedure calls
	 */
	protected void setSupportsCatalogsInProcedureCalls(boolean supportsCatalogsInProcedureCalls) {
		this.supportsCatalogsInProcedureCalls = supportsCatalogsInProcedureCalls;
	}

	/**
	 * Does the database support the use of schema name in procedure calls
	 */
	protected boolean isSupportsSchemasInProcedureCalls() {
		return supportsSchemasInProcedureCalls;
	}

	/**
	 * Specify whether the database supports the use of schema name in procedure calls
	 */
	protected void setSupportsSchemasInProcedureCalls(boolean supportsSchemasInProcedureCalls) {
		this.supportsSchemasInProcedureCalls = supportsSchemasInProcedureCalls;
	}

	/**
	 * Does the database use upper case for identifiers
	 */
	protected boolean isStoresUpperCaseIdentifiers() {
		return storesUpperCaseIdentifiers;
	}

	/**
	 * Specify whether the database uses upper case for identifiers
	 */
	protected void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
	}

	/**
	 * Does the database use lower case for identifiers
	 */
	protected boolean isStoresLowerCaseIdentifiers() {
		return storesLowerCaseIdentifiers;
	}

	/**
	 * Specify whether the database uses lower case for identifiers
	 */
	protected void setStoresLowerCaseIdentifiers(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	/**
	 * Process the procedure column metadata
	 */
	private void processProcedureColumns(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String procedureName) {
		ResultSet procs = null;
		String metaDataCatalogName = metaDataCatalogNameToUse(catalogName);
		String metaDataSchemaName = metaDataSchemaNameToUse(schemaName);
		String metaDataProcedureName = procedureNameToUse(procedureName);
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving metadata for " + metaDataCatalogName + "/" +
					metaDataSchemaName + "/" + metaDataProcedureName);
		}
		try {
			procs = databaseMetaData.getProcedureColumns(
					metaDataCatalogName,
					metaDataSchemaName,
					metaDataProcedureName,
					null);
			while (procs.next()) {
				CallParameterMetaData meta = new CallParameterMetaData(
						procs.getString("COLUMN_NAME"),
						procs.getInt("COLUMN_TYPE"),
						procs.getInt("DATA_TYPE"),
						procs.getString("TYPE_NAME"),
						procs.getBoolean("NULLABLE")
				);
				callParameterMetaData.add(meta);
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieved metadata: "
						+ meta.getParameterName() +
						" " + meta.getParameterType() +
						" " + meta.getSqlType() +
						" " + meta.getTypeName() +
						" " + meta.isNullable()
					);
				}
			}
		}
		catch (SQLException se) {
			logger.warn("Error while retreiving metadata for procedure columns: " + se.getMessage());
		}
		finally {
			try {
				if (procs != null)
					procs.close();
			}
			catch (SQLException se) {
				logger.warn("Problem closing resultset for procedure column metadata " + se.getMessage());
			}
		}

	}

}
