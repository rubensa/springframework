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

package org.springframework.jdbc.core.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author trisberg
 */
public abstract class AbstractJdbcCall {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Lower-level class used to execute SQL */
	private JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/** List of SqlParameter objects */
	private final List<SqlParameter> declaredParameters = new ArrayList<SqlParameter>();


	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled = false;

	private String callString;

	private CallMetaDataContext callMetaDataContext = new CallMetaDataContext();

	/**
	 * Object enabling us to create CallableStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private CallableStatementCreatorFactory callableStatementFactory;

	protected AbstractJdbcCall(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	protected AbstractJdbcCall(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public String getProcedureName() {
		return callMetaDataContext.getProcedureName();
	}

	public void setProcedureName(String procedureName) {
		callMetaDataContext.setProcedureName(procedureName);
	}

	public String getCatalogName() {
		return callMetaDataContext.getCatalogName();
	}

	public void setCatalogName(String catalogName) {
		callMetaDataContext.setCatalogName(catalogName);
	}

	public String getSchemaName() {
		return callMetaDataContext.getSchemaName();
	}

	public void setSchemaName(String schemaName) {
		callMetaDataContext.setSchemaName(schemaName);
	}

	public boolean isFunction() {
		return callMetaDataContext.isFunction();
	}

	public void setFunction(boolean b) {
		this.callMetaDataContext.setFunction(b);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	protected CallableStatementCreatorFactory getCallableStatementFactory() {
		return callableStatementFactory;
	}

	public void addDeclaredParameter(SqlParameter parameter) {
		this.declaredParameters.add(parameter);
		if (logger.isDebugEnabled()) {
			logger.debug("Added declared parameter for [" + getProcedureName() + "]: " + parameter.getName());
		}
	}
	
	public String getCallString() {
		return callString;
	}

	public AbstractJdbcCall setAccessCallParameterMetaData(boolean accessCallParameterMetaData) {
		this.callMetaDataContext.setAccessCallParameterMetaData(accessCallParameterMetaData);
		return this;
	}

	/**
	 * Compile this query.
	 * Ignores subsequent attempts to compile.
	 * @throws org.springframework.dao.InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getProcedureName() == null) {
				throw new InvalidDataAccessApiUsageException("Procedure or Function name is required");
			}

			try {
				this.jdbcTemplate.afterPropertiesSet();
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage());
			}

			compileInternal();
			this.compiled = true;

			if (logger.isDebugEnabled()) {
				logger.debug("SqlCall for " + (isFunction() ? "function" : "procedure") + " [" + getProcedureName() + "] compiled");
			}
		}
	}

	/**
	 * Overridden method to configure the CallableStatementCreatorFactory
	 * based on our declared parameters.
	 * @see org.springframework.jdbc.object.RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {

		callMetaDataContext.processMetaData(getJdbcTemplate().getDataSource(), declaredParameters);

		callString = callMetaDataContext.createCallString();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Compiled stored procedure. Call string is [" + getCallString() + "]");
		}

		this.callableStatementFactory =
				new CallableStatementCreatorFactory(getCallString(), callMetaDataContext.getCallParameters());
		//this.callableStatementFactory.setResultSetType(getResultSetType());
		//this.callableStatementFactory.setUpdatableResults(isUpdatableResults());
		this.callableStatementFactory.setNativeJdbcExtractor(getJdbcTemplate().getNativeJdbcExtractor());

		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

	/**
	 * Is this operation "compiled"? Compilation, as in JDO,
	 * means that the operation is fully configured, and ready to use.
	 * The exact meaning of compilation will vary between subclasses.
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * Check whether this operation has been compiled already;
	 * lazily compile it if not already compiled.
	 * <p>Automatically called by <code>validateParameters</code>.
	 */
	protected void checkCompiled() {
		if (!isCompiled()) {
			logger.debug("SQL call not compiled before execution - invoking compile");
			compile();
		}
	}

	protected String getScalarOutParameterName() {
		return callMetaDataContext.getScalarOutParameterName();
	}

	protected Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> args) {
		return callMetaDataContext.matchInParameterValuesWithCallParameters(args);
	}

	protected List<SqlParameter> getCallParameters() {
		return callMetaDataContext.getCallParameters();
	}
}
