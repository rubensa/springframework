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

package org.springframework.jdbc.support.xml;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.transform.Result;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Implementation of the SqlXmlValue interface that will handle XML input in the form of
 * a <code>Result</code>.
 *
 * @author Thomas Risberg
 * @since 2.5.6
 * @see org.springframework.jdbc.support.xml.SqlXmlHandler
 */
public class ResultSqlXmlValue implements SqlXmlValue {

	private SQLXML xmlObject;
	
	@SuppressWarnings("unchecked")
	private Class resultClass;
	
	private XmlResultProvider provider;


	public ResultSqlXmlValue(Class resultClass, XmlResultProvider provider) {
		this.resultClass = resultClass;
		this.provider = provider;
	}


	@Override
	public void cleanup() {
		try {
			xmlObject.free();
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not free SQLXML object", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setTypeValue(PreparedStatement ps, int colIndex,
			int sqlType, String typeName) throws SQLException {
		xmlObject = ps.getConnection().createSQLXML();
		Result result = xmlObject.setResult(resultClass);
		provider.provideXml(result);
		ps.setSQLXML(colIndex, xmlObject);
	}

}
