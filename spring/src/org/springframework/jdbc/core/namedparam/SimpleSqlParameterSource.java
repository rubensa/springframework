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

package org.springframework.jdbc.core.namedparam;

import java.util.HashMap;
import java.util.Map;

/**
 * SqlParameterSource implementation that holds a given set of parameters.
 *
 * <p>This class is intended for passing in a simple set of parameter values
 * to methods of the NamedParameterJdbcTemplate class.
 *
 * <p>The <code>addValue</code> methods on this class will make adding several
 * values easier. The methods return a reference to the SimpleSqlParameterSource
 * itself, so you can chain several method calls together within a single statement.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see #addValue
 * @see #registerSqlType
 * @see NamedParameterJdbcTemplate
 */
public class SimpleSqlParameterSource extends AbstractSqlParameterSource {

	private final Map parameterValues = new HashMap();


	/**
	 * Create an empty SimpleSqlParameterSource,
	 * with values to be added via <code>addValue</code>.
	 * @see #addValue
	 */
	public SimpleSqlParameterSource() {
	}

	/**
	 * Create a new SimpleSqlParameterSource based on a Map.
	 * @param paramMap a Map holding existing parameter values
	 */
	public SimpleSqlParameterSource(Map paramMap) {
		this.parameterValues.putAll(paramMap);
	}


	/**
	 * Add a parameter to this parameter source.
	 * @param paramName the name of the parameter
	 * @param value the value of the parameter
	 * @return a reference of this parameter source,
	 * so it's possible to chain several calls together
	 */
	public SimpleSqlParameterSource addValue(String paramName, Object value) {
		this.parameterValues.put(paramName, value);
		return this;
	}

	/**
	 * Add a parameter to this parameter source.
	 * @param paramName the name of the parameter
	 * @param value the value of the parameter
	 * @param sqlType the SQL type of the parameter
	 * @return a reference of this parameter source,
	 * so it's possible to chain several calls together
	 */
	public SimpleSqlParameterSource addValue(String paramName, Object value, int sqlType) {
		this.parameterValues.put(paramName, value);
		registerSqlType(paramName, sqlType);
		return this;
	}


	public boolean hasValue(String paramName) {
		return this.parameterValues.containsKey(paramName);
	}

	public Object getValue(String paramName) {
		if (!this.parameterValues.containsKey(paramName)) {
			throw new IllegalArgumentException("No value registered for key '" + paramName + "'");
		}
		return this.parameterValues.get(paramName);
	}

}
