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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * SQL Server specific implementation for the {@link CallMetaDataProvider} interface.
 * This class is intended for internal use by the Simple JDBC classes.
 *
 * @author Thomas Risberg
 * @since 2.1
 */
public class SqlServerCallMetaDataProvider extends GenericCallMetaDataProvider {

	private static final String REMOVABLE_COLUMN_PREFIX = "@";
	private static final String RETURN_VALUE_NAME = "@RETURN_VALUE";

	public SqlServerCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	@Override
	public String parameterNameToUse(String parameterName) {
		if (parameterName == null)
			return null;
		if (parameterName.length() > 1 && parameterName.startsWith(REMOVABLE_COLUMN_PREFIX))
			return super.parameterNameToUse(parameterName.substring(1));
		else
			return super.parameterNameToUse(parameterName);
	}

	@Override
	public boolean byPassReturnParameter(String parameterName) {
		if (RETURN_VALUE_NAME.equals(parameterName))
			return true;
		else
			return super.byPassReturnParameter(parameterName);
	}
}
