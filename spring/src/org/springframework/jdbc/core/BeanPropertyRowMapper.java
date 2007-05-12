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

package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.sql.Types;

/**
 * Generic RowMapper implementation that converts a row into a new instance
 * of the specified mapped target class.  The mapped target class must be a top-level class
 * and it must have a default or no-arg constructor.
 *
 * Column values are mapped based on matching the column name as obtained from result set
 * metadata to public setters for the corresponding properties.  The names are matched either
 * directly or by transforming a name separating the parts with underscores to the same name
 * using "camel" case.
 *
 * Mapping is provided for fields in the target class that are defined as any of the
 * following types: String, byte, Byte, short, Short, int, Integer, long, Long, float, Float,
 * double, Double, BigDecimal, boolean, Boolean and java.util.Date.
 *
 * To facilitate mapping between columns and fields that don't have matching names, try using column 
 * aliases in the SQL statement like "select fname as first_name from customer".
 *
 * Please note that this class is designed to provide convenience rather than high performance.
 * For best performance consider using a custom RowMapper.
 *
 * @author Thomas Risberg
 * @since 2.1
 * @see RowMapper
 */
public class BeanPropertyRowMapper implements RowMapper {

	/** Logger instance */
	private final Log logger = LogFactory.getLog(getClass());

	/** The class we are mapping to */
	private Class mappedClass;

	/** The default or no-arg constructor for the mapped class */
	private Constructor defaultConstruct;

	/** Map of the fields we provide mapping for */
	private Map mappedFields;


	/**
	 * Create a new BeanPropertyRowMapper.
	 * @see #setMappedClass
	 */
	public BeanPropertyRowMapper() {
	}

	/**
	 * Create a new BeanPropertyRowMapper.
	 * @param mappedClass the class that each row should be mapped to.
	 */
	public BeanPropertyRowMapper(Class mappedClass) {
		setMappedClass(mappedClass);
	}

	/**
	 * Set the class that each row should be mapped to.
	 */
	public void setMappedClass(Class mappedClass) {
		if (this.mappedClass == null) {
			this.mappedClass = mappedClass;
			try {
				this.defaultConstruct = mappedClass.getConstructor((Class[]) null);
			}
			catch (NoSuchMethodException ex) {
				throw new DataAccessResourceFailureException(new StringBuffer().
						append("Failed to access default or no-arg constructor of ").
						append(mappedClass.getName()).toString(), ex);
			}
			this.mappedFields = new HashMap();
			Field[] f = mappedClass.getDeclaredFields();
			for (int i = 0; i < f.length; i++) {
				PersistentField pf = new PersistentField();
				pf.setFieldName(f[i].getName());
				pf.setJavaType(f[i].getType());
				this.mappedFields.put(f[i].getName().toLowerCase(), pf);
				String underscoredName = underscoreName(f[i].getName());
				if (!f[i].getName().toLowerCase().equals(underscoredName)) {
					this.mappedFields.put(underscoredName, pf);
				}
			}
		}
		else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
				mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}


	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */
	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		if (this.mappedClass == null)
			throw new InvalidDataAccessApiUsageException("Target class was not specified - it is mandatory");
		Object result;
		try {
			result = this.defaultConstruct.newInstance((Object[]) null);
		}
		catch (IllegalAccessException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		catch (InvocationTargetException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		catch (InstantiationException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			String column = meta.getColumnName(i).toLowerCase();
			PersistentField fieldMeta = (PersistentField) this.mappedFields.get(column);
			if (fieldMeta != null) {
				BeanWrapper bw = new BeanWrapperImpl(mappedClass);
				bw.setWrappedInstance(result);
				fieldMeta.setSqlType(meta.getColumnType(i));
				Object value = null;
				Class fieldType = fieldMeta.getJavaType();
				if (fieldType.equals(String.class)) {
					value = rs.getString(column);
				}
				else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
					value = new Byte(rs.getByte(column));
				}
				else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
					value = new Short(rs.getShort(column));
				}
				else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
					value = new Integer(rs.getInt(column));
				}
				else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
					value = new Long(rs.getLong(column));
				}
				else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
					value = new Float(rs.getFloat(column));
				}
				else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
					value = new Double(rs.getDouble(column));
				}
				else if (fieldType.equals(BigDecimal.class)) {
					value = rs.getBigDecimal(column);
				}
				else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
					value = (rs.getBoolean(column)) ? Boolean.TRUE : Boolean.FALSE;
				}
				else if (fieldType.equals(java.util.Date.class)) {
					if (fieldMeta.getSqlType() == Types.DATE) {
						value = rs.getDate(column);
					}
					else if (fieldMeta.getSqlType() == Types.TIME) {
						value = rs.getTime(column);
					}
					else {
						value = rs.getTimestamp(column);
					}
				}
				if (value != null) {
					try {
						if (logger.isDebugEnabled() && rowNumber == 0) {
							logger.debug(
									"Mapping column named \"" + column + "\"" +
									" containing values of SQL type " + fieldMeta.getSqlType() +
									" to property \"" + fieldMeta.getFieldName() + "\"" +
									" of type " + fieldMeta.getJavaType());
						}
						bw.setPropertyValue(fieldMeta.getFieldName(), value);
					}
					catch (NotWritablePropertyException ignore) {
						if (rowNumber == 0)
						{
							logger.warn("Unable to access the setter for " + fieldMeta.getFieldName() +
									".  Check that " + "set" + StringUtils.capitalize(fieldMeta.getFieldName()) +
									" is declared and has public access.");
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case.  Any upper case letters are
	 * converted to lower case with a preceding underscore.
	 * @param name The string containing original name
	 * @return The name converted
	 */
	public static String underscoreName(String name) {
		StringBuffer result = new StringBuffer();
		if (name != null && name.length() > 0) {
			result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals(s.toUpperCase())) {
					result.append("_");
					result.append(s.toLowerCase());
				}
				else {
					result.append(s);
				}
			}
		}
		return result.toString();
	}

	/**
	 * A PersistentField represents the info we are interested in knowing about
	 * the fields and their relationship to the columns of the database table.
	 */
	private class PersistentField {

		private String fieldName;

		private Class javaType;

		private int sqlType;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public Class getJavaType() {
			return javaType;
		}

		public void setJavaType(Class javaType) {
			this.javaType = javaType;
		}

		public int getSqlType() {
			return sqlType;
		}

		public void setSqlType(int sqlType) {
			this.sqlType = sqlType;
		}
	}

}
