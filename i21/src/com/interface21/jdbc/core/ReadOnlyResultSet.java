package com.interface21.jdbc.core;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.net.URL;

/**
 * <P>A class that implements java.sql.ResultSet which is used in the 
 * JdbcTemplateclass. This is the ResultSet instance which is passed as a 
 * parameter to the extract() callback method of the 
 * ManualExtractionSqlQueryWithParameters and ManualExtractionSqlQuery classes.
 * The objective is to be able to control that the user does not break the 
 * extraction mechanism by calling methods that disturb for instance the 
 * positioning of the cursor, such as next() or first(), or any other setter 
 * method. If such a call is made anyway, an InvalidDataAccessApiUsageException 
 * exception is thrown.</P>
 * 
 * <P>Overall, the class acts as a proxy to the actual ResultSet and filters 
 * out faulty calls to unauthorised methods behind the scenes. For that very
 * reason, the wrapped methods are not documented here. Please refer to the 
 * official ResultSet Javadoc for more information.</P>
 * 
 * @author Yann Caroff
 */
public class ReadOnlyResultSet implements ResultSet {
  
  /**
   * The ResultSet instance that is actually wrapped
   */
  private final ResultSet rs;
  
  /**
   * Constructor. Creates an instance of the ResultSet wrapper using the 
   * ResultSet to be wrapped as a parameter.
   * @param rs The wrapped ResultSet instance
   * @throws InvalidParameterException when the wrapped ResultSet instance
   *          is null.
   */
  public ReadOnlyResultSet(ResultSet rs) {
    if (rs == null) {
      throw new InvalidParameterException("rs", "null");
    }
    
    this.rs = rs;
  }
  
  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#next()
   */
  public boolean next() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("next");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#close()
   */
  public void close() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("close");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#wasNull()
   */
  public boolean wasNull() throws SQLException {
    return rs.wasNull();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getString(int columnIndex)
   */
  public String getString(int columnIndex) throws SQLException {
    return rs.getString(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBoolean(int columnIndex)
   */
  public boolean getBoolean(int columnIndex) throws SQLException {
    return rs.getBoolean(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getByte(int columnIndex)
   */
  public byte getByte(int columnIndex) throws SQLException {
    return rs.getByte(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getShort(int columnIndex)
   */
  public short getShort(int columnIndex) throws SQLException {
    return rs.getShort(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getInt(int columnIndex)
   */
  public int getInt(int columnIndex) throws SQLException {
    return rs.getInt(columnIndex);
  }

  /**
   * @see java.sql.ResultSet#getLong(int columnIndex)
   */
  public long getLong(int columnIndex) throws SQLException {
    return rs.getLong(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getFloat(int columnIndex)
   */
  public float getFloat(int columnIndex) throws SQLException {
    return rs.getFloat(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDouble(int columnIndex)
   */
  public double getDouble(int columnIndex) throws SQLException {
    return rs.getDouble(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBigDecimal(int columnIndex, int scale)
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    return rs.getBigDecimal(columnIndex, scale);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBytes(int columnIndex)
   */
  public byte[] getBytes(int columnIndex) throws SQLException {
    return rs.getBytes(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDate(int columnIndex)
   */
  public java.sql.Date getDate(int columnIndex) throws SQLException {
    return rs.getDate(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTime(int columnIndex)
   */
  public java.sql.Time getTime(int columnIndex) throws SQLException {
    return rs.getTime(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTimestamp(int columnIndex)
   */
  public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
    return rs.getTimestamp(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getAsciiStream(int columnIndex)
   */
  public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
    return rs.getAsciiStream(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getUnicodeStream(int columnIndex)
   */
  public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
    return rs.getUnicodeStream(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBinaryStream(int columnIndex)
   */
  public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
    return rs.getBinaryStream(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getString(int columnIndex)
   */
  public String getString(String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBoolean(int columnIndex)
   */
  public boolean getBoolean(String columnName) throws SQLException {
    return rs.getBoolean(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getByte(int columnIndex)
   */
  public byte getByte(String columnName) throws SQLException {
    return rs.getByte(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getShort(int columnIndex)
   */
  public short getShort(String columnName) throws SQLException {
    return rs.getShort(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getInt(int columnIndex)
   */
  public int getInt(String columnName) throws SQLException {
    return rs.getInt(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getLong(String columnName)
   */
  public long getLong(String columnName) throws SQLException {
    return rs.getLong(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getFloat(String columnName)
   */
  public float getFloat(String columnName) throws SQLException {
    return rs.getFloat(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDouble(String columnName)
   */
  public double getDouble(String columnName) throws SQLException {
    return rs.getDouble(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBigDecimal(String columnName, int scale)
   */
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
    return rs.getBigDecimal(columnName, scale);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBytes(String columnName)
   */
  public byte[] getBytes(String columnName) throws SQLException {
    return rs.getBytes(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDate(String columnName)
   */
  public java.sql.Date getDate(String columnName) throws SQLException {
    return rs.getDate(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTime(String columnName)
   */
  public java.sql.Time getTime(String columnName) throws SQLException {
    return rs.getTime(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTimestamp(String columnName)
   */
  public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
    return rs.getTimestamp(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getAsciiStream(String columnName)
   */
  public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
    return rs.getAsciiStream(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getUnicodeStream(String columnName)
   */
  public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
    return rs.getUnicodeStream(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBinaryStream(String columnName)
   */
  public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
    return rs.getBinaryStream(columnName);
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#getWarnings()
   */
  public SQLWarning getWarnings() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("getWarnings");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#clearWarnings()
   */
  public void clearWarnings() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("clearWarnings");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#getCursorName()
   */
  public String getCursorName() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("getCursorName");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getMetaData()
   */
  public ResultSetMetaData getMetaData() throws SQLException {
    return rs.getMetaData();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getObject(int columnIndex)
   */
  public Object getObject(int columnIndex) throws SQLException {
    return rs.getObject(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getObject(String columnName)
   */
  public Object getObject(String columnName) throws SQLException {
    return rs.getObject(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#findColumn(String columnName)
   */
  public int findColumn(String columnName) throws SQLException {
    return rs.findColumn(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getCharacterStream(int columnIndex)
   */
  public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
    return rs.getCharacterStream(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getCharacterStream(String columnName)
   */
  public java.io.Reader getCharacterStream(String columnName) throws SQLException {
    return rs.getCharacterStream(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBigDecimal(int columnIndex)
   */
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    return rs.getBigDecimal(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBigDecimal(String columnName)
   */
  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return rs.getBigDecimal(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#isBeforeFirst()
   */
  public boolean isBeforeFirst() throws SQLException {
    return rs.isBeforeFirst();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#isAfterLast()
   */
  public boolean isAfterLast() throws SQLException {
    return rs.isAfterLast();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#isFirst()
   */
  public boolean isFirst() throws SQLException {
    return rs.isFirst();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#isLast()
   */
  public boolean isLast() throws SQLException {
    return rs.isLast();
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#beforeFirst()
   */
  public void beforeFirst() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("beforeFirst");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#afterLast()
   */
  public void afterLast() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("afterLast");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#first()
   */
  public boolean first() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("first");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#last()
   */
  public boolean last() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("last");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getRow()
   */
  public int getRow() throws SQLException {
    return rs.getRow();
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#absolute(int row)
   */
  public boolean absolute(int row) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("absolute");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#relative(int rows)
   */
  public boolean relative(int rows) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("relative");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#previous()
   */
  public boolean previous() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("previous");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#setFetchDirection(int direction)
   */
  public void setFetchDirection(int direction) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("setFetchDirection");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getFetchDirection()
   */
  public int getFetchDirection() throws SQLException {
    return rs.getFetchDirection();
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#setFetchSize(int rows)
   */
  public void setFetchSize(int rows) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("setFetchSize");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getFetchSize()
   */
  public int getFetchSize() throws SQLException {
    return rs.getFetchSize();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getType()
   */
  public int getType() throws SQLException {
    return rs.getType();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getConcurrency()
   */
  public int getConcurrency() throws SQLException {
    return rs.getConcurrency();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#rowUpdated()
   */
  public boolean rowUpdated() throws SQLException {
    return rs.rowUpdated();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#rowInserted() 
   */
  public boolean rowInserted() throws SQLException {
    return rs.rowInserted();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#rowDeleted()
   */
  public boolean rowDeleted() throws SQLException {
    return rs.rowDeleted();
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateNull(int columnIndex)
   */
  public void updateNull(int columnIndex) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateNull");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBoolean(int columnIndex, boolean x)
   */
  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBoolean");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateByte(int columnIndex, byte x)
   */
  public void updateByte(int columnIndex, byte x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateByte");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateShort(int columnIndex, short x)
   */
  public void updateShort(int columnIndex, short x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateShort");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateInt(int columnIndex, int x)
   */
  public void updateInt(int columnIndex, int x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateInt");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateLong(int columnIndex, long x)
   */
  public void updateLong(int columnIndex, long x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateLong");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateFloat(int columnIndex, float x)
   */
  public void updateFloat(int columnIndex, float x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateFloat");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateDouble(int columnIndex, double x)
   */
  public void updateDouble(int columnIndex, double x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateDouble");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBigDecimal(int columnIndex, BigDecimal x)
   */
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBigDecimal");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateString(int columnIndex, String x)
   */
  public void updateString(int columnIndex, String x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateString");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBytes(int columnIndex, byte x[])
   */
  public void updateBytes(int columnIndex, byte x[]) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBytes");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateDate(int columnIndex, java.sql.Date x)
   */
  public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateDate");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateTime(int columnIndex, java.sql.Time x)
   */
  public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateTime");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateTimestamp(int columnIndex, java.sql.Timestamp x)
   */
  public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateTimestamp");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateAsciiStream(int columnIndex, java.io.InputStream x, int length)
   */
  public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateAsciiStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#(int columnIndex, java.io.InputStream x, int length)
   */
  public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBinaryStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateCharacterStream(int columnIndex, java.io.Reader x, int length)
   */
  public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateCharacterStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateObject(int columnIndex, Object x, int scale)
   */
  public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateObject");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateObject(int columnIndex, Object x)
   */
  public void updateObject(int columnIndex, Object x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateObject");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateNull(String columnName)
   */
  public void updateNull(String columnName) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateNull");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBoolean(String columnName, boolean x)
   */
  public void updateBoolean(String columnName, boolean x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBoolean");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateByte(String columnName, byte x)
   */
  public void updateByte(String columnName, byte x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateByte");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateShort(String columnName, short x)
   */
  public void updateShort(String columnName, short x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateShort");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateInt(String columnName, int x)
   */
  public void updateInt(String columnName, int x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateInt");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateLong(String columnName, long x)
   */
  public void updateLong(String columnName, long x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateLong");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateFloat(String columnName, float x)
   */
  public void updateFloat(String columnName, float x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateFloat");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateDouble(String columnName, double x)
   */
  public void updateDouble(String columnName, double x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateDouble");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBigDecimal(String columnName, BigDecimal x)
   */
  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBigDecimal");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateString(String columnName, String x)
   */
  public void updateString(String columnName, String x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateString");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBytes(String columnName, byte x[])
   */
  public void updateBytes(String columnName, byte x[]) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBytes");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateDate(String columnName, java.sql.Date x)
   */
  public void updateDate(String columnName, java.sql.Date x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateDate");
  }

  /**
    * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
  * @see java.sql.ResultSet#updateTime(String columnName, java.sql.Time x)
   */
  public void updateTime(String columnName, java.sql.Time x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateTime");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateTimestamp(String columnName, java.sql.Timestamp x)
   */
  public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateTimestamp");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateAsciiStream(String columnName, java.io.InputStream x, int length)
   */
  public void updateAsciiStream(String columnName, java.io.InputStream x, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateAsciiStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateBinaryStream(String columnName, java.io.InputStream x, int length)
   */
  public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateBinaryStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateCharacterStream(String columnName, java.io.Reader reader, int length)
   */
  public void updateCharacterStream(String columnName, java.io.Reader reader, int length) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateCharacterStream");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateObject(String columnName, Object x, int scale)
   */
  public void updateObject(String columnName, Object x, int scale) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateObject");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateObject(String columnName, Object x)
   */
  public void updateObject(String columnName, Object x) throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateObject");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#insertRow() 
   */
  public void insertRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("insertRow");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#updateRow() 
   */
  public void updateRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("updateRow");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#deleteRow()
   */
  public void deleteRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("deleteRow");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#refreshRow()
   */
  public void refreshRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("refreshRow");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#cancelRowUpdates()
   */
  public void cancelRowUpdates() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("cancelRowUpdates");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#moveToInsertRow()
   */
  public void moveToInsertRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("moveToInsertRow");
  }

  /**
   * Not authorised.
   * @throws InvalidResultSetMethodInvocationException
   * @see java.sql.ResultSet#moveToCurrentRow()
   */
  public void moveToCurrentRow() throws SQLException {
    throw new InvalidResultSetMethodInvocationException("moveToCurrentRow");
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getStatement()
   */
  public Statement getStatement() throws SQLException {
    return rs.getStatement();
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getObject(int columnIndex, java.util.Map map)
   */
  public Object getObject(int columnIndex, java.util.Map map) throws SQLException {
    return rs.getObject(columnIndex, map);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getRef(int columnIndex)
   */
  public Ref getRef(int columnIndex) throws SQLException {
    return rs.getRef(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBlob(int columnIndex)
   */
  public Blob getBlob(int columnIndex) throws SQLException {
    return rs.getBlob(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getClob(int columnIndex)
   */
  public Clob getClob(int columnIndex) throws SQLException {
    return rs.getClob(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getArray(int columnIndex)
   */
  public Array getArray(int columnIndex) throws SQLException {
    return rs.getArray(columnIndex);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getObject(String columnName, java.util.Map map)
   */
  public Object getObject(String columnName, java.util.Map map) throws SQLException {
    return rs.getObject(columnName, map);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getRef(String columnName)
   */
  public Ref getRef(String columnName) throws SQLException {
    return rs.getRef(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getBlob(String columnName) 
   */
  public Blob getBlob(String columnName) throws SQLException {
    return rs.getBlob(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getClob(String columnName)
   */
  public Clob getClob(String columnName) throws SQLException {
    return rs.getClob(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getArray(String columnName)
   */
  public Array getArray(String columnName) throws SQLException {
    return rs.getArray(columnName);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDate(int columnIndex, Calendar cal)
   */  public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
    return rs.getDate(columnIndex, cal);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getDate(String columnName, Calendar cal)
   */
  public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
    return rs.getDate(columnName, cal);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTime(int columnIndex, Calendar cal)
   */
  public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
    return rs.getTime(columnIndex, cal);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTime(String columnName, Calendar cal)
   */
  public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
    return rs.getTime(columnName, cal);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTimestamp(int columnIndex, Calendar cal)
   */
  public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    return rs.getTimestamp(columnIndex, cal);
  }

  /**
   * Authorised.
   * @see java.sql.ResultSet#getTimestamp(String columnName, Calendar cal)
   */
  public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
    return rs.getTimestamp(columnName, cal);
  }

	//---------------------------------------------------------------------
	// New ResultSet methods from JDBC 3.0 resp. J2SE 1.4.
	// Dummy implementations for compatibility.
	// TODO: Call underlying ResultSet as soon as Spring requires J2SE 1.4.
	//---------------------------------------------------------------------
	public URL getURL(int columnIndex) throws SQLException {
		return null;
	}

	public URL getURL(String columnName) throws SQLException {
		return null;
	}

	//---------------------------------------------------------------------
	// New ResultSet methods from JDBC 3.0 resp. J2SE 1.4.
	// Implementations for compatibility, not authorised anyway.
	//---------------------------------------------------------------------
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateRef");
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateRef");
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateBlob");
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateBlob");
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateClob");
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateClob");
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateArray");
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		throw new InvalidResultSetMethodInvocationException("updateArray");
	}
}