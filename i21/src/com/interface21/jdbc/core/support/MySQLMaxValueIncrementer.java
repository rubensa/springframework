package com.interface21.jdbc.core.support;

import javax.sql.DataSource;

import com.interface21.beans.factory.InitializingBean;
import com.interface21.jdbc.core.DataSourceUtils;
import com.interface21.jdbc.object.SqlFunction;
import com.interface21.jdbc.object.SqlUpdate;

/**
 * Class to inceremnet maximum value of a given MySQL table with an auto-increment column.
 * The sequence is kept in a table; it is up to the user whether there is one sequence table
 * per table to be used, or whether multiple sequence values are kept in one table.
 * <p>
 * Thus you could have
 * <code>
 * create table sequences (
 *   seq1 int unsigned not null,
 *   seq2 int unsigned not null,
 * unique (seq1),
 * unique(seq2));
 * insert into sequences values(0, 0);
 * </code>
 * The table name in this case is "sequences", the column names for the auto-increment 
 * fields "seq1" or "seq2".
 * Alternatively you could have
 * <code>
 * create table sequence1 (
 *   seq1 int unsigned not null primary key
 * );
 * insert into sequence1 values(0);
 * <br>
 * create table sequence2 (
 *   seq2 int unsigned not null primary key
 * );
 * insert into sequence2 values(0);
 * </code>
 * The table names in this case are "sequence1" and "sequence2", the column names 
 * for the auto-increment fields respectively "seq1" or "seq2".
 * <p>
 * @author <a href="mailto:isabelle@meta-logix.com">Isabelle Muszynski</a>
 * @version $Id$
 */
public class MySQLMaxValueIncrementer
    extends AbstractDataFieldMaxValueIncrementer
    implements InitializingBean {

    //-----------------------------------------------------------------
    // Instance data
    //-----------------------------------------------------------------
    private DataSource ds;

    /** The name of the table containing the sequence */
    private String tableName;

    /** The name of the column to use for this sequence */
    private String columnName;

    /** Should the string result pre pre-pended with zeroes */
    private boolean prefixWithZero;

    /** The length to which the string result should be pre-pended with zeroes */
    private int paddingLength;

    private NextMaxValueProvider nextMaxValueProvider;

    /**
     * Default constructor 
     **/
    public MySQLMaxValueIncrementer() {
	this.nextMaxValueProvider = new NextMaxValueProvider();
    }

    /**
     * Constructor 
     * @param ds the datasource to use
     * @param tableName the name of the sequence table to use
     * @param columnName the name of the column in the sequence table to use
     **/
    public MySQLMaxValueIncrementer(DataSource ds, String tableName, String columnName) {
	this.ds = ds;
	this.tableName = tableName;
	this.columnName = columnName;
	this.nextMaxValueProvider = new NextMaxValueProvider();
    }

    /**
     * Constructor 
     * @param ds the datasource to use
     * @param tableName the name of the sequence table to use
     * @param columnName the name of the column in the sequence table to use
     * @param prefixWithZero in case of a String return value, should the string be prefixed with zeroes
     * @param padding the length to which the string return value should be padded with zeroes
     **/
    public MySQLMaxValueIncrementer(DataSource ds, String tableName, String columnName, boolean prefixWithZero, int padding) {
	this.ds = ds;
	this.tableName = tableName;
	this.columnName = columnName;
	this.prefixWithZero = prefixWithZero;
	this.paddingLength = padding;
	this.nextMaxValueProvider = new NextMaxValueProvider();
    }

    /**
     * @see com.interface21.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementIntValue()
     */
    protected int incrementIntValue() {
	return nextMaxValueProvider.getNextIntValue();
    }

    /**
     * @see com.interface21.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementDoubleValue()
     */
    protected double incrementDoubleValue() {
	return nextMaxValueProvider.getNextDoubleValue();
    }

    /**
     * @see com.interface21.jdbc.core.support.AbstractDataFieldMaxValueIncrementer#incrementStringValue()
     */
    protected String incrementStringValue() {
	return nextMaxValueProvider.getNextStringValue();
    }

    // Private class that does the actual
    // job of getting the sequence.nextVal value
    private class NextMaxValueProvider {

	public int getNextIntValue() {
	    SqlUpdate sqlup = new SqlUpdate(ds, "update " + tableName + " set " + columnName + " = last_insert_id(" + columnName + " + 1)");
	    sqlup.compile();
	    sqlup.update();
	    SqlFunction sqlf = new SqlFunction(ds, "select last_insert_id()");
	    sqlf.compile();
	    return sqlf.run();
	}

	private double getNextDoubleValue() {
	    SqlUpdate sqlup = new SqlUpdate(ds, "update " + tableName + " set " + columnName + " = last_insert_id(" + columnName + " + 1)");
	    sqlup.update();
	    sqlup.compile();
	    SqlFunction sqlf = new SqlFunction(ds, "select last_insert_id()");
	    return ((Double)sqlf.runGeneric()).doubleValue();
	}

	private String getNextStringValue() {
	    String s = new Integer(nextIntValue()).toString();
	    if (prefixWithZero) {
		int len = s.length();
		if (len < paddingLength + 1) {
		    StringBuffer buff = new StringBuffer(paddingLength);
		    for (int i = 0; i < paddingLength - len; i++)
			buff.append("0");
		    buff.append(s);
		    s = buff.toString();
		}
	    }

	    return s;
	}
    }

    /**
     * Sets the data source.
     * @param ds The data source to set
     */
    public void setDataSource(DataSource ds) {
	this.ds = ds;
    }

    /**
     * @see com.interface21.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
	if (ds == null || tableName == null || columnName == null)
	    throw new Exception("dsName, sequenceName properties must be set on " + getClass().getName());
    }

    /**
     * Sets the prefixWithZero.
     * @param prefixWithZero The prefixWithZero to set
     */
    public void setPrefixWithZero(boolean prefixWithZero, int length) {
	this.prefixWithZero = prefixWithZero;
	this.paddingLength = length;
    }

    /**
     * Sets the tableName.
     * @param tableName The tableName to set
     */
    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    /**
     * Sets the columnName.
     * @param columnName The columnName to set
     */
    public void setColumnName(String columnName) {
	this.columnName = columnName;
    }
}
 
