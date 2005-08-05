/*
 * Copyright 2002-2005 the original author or authors.
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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.easymock.MockControl;

import org.springframework.jdbc.AbstractJdbcTests;

/**
 * @author Juergen Hoeller
 * @since 19.12.2004
 */
public class JdbcTemplateQueryTests extends AbstractJdbcTests {

	private MockControl ctrlStatement;
	private Statement mockStatement;
	private MockControl ctrlPreparedStatement;
	private PreparedStatement mockPreparedStatement;
	private MockControl ctrlResultSet;
	private ResultSet mockResultSet;
	private MockControl ctrlResultSetMetaData;
	private ResultSetMetaData mockResultSetMetaData;

	protected void setUp() throws Exception {
		super.setUp();

		ctrlStatement = MockControl.createControl(Statement.class);
		mockStatement = (Statement) ctrlStatement.getMock();
		ctrlPreparedStatement = MockControl.createControl(PreparedStatement.class);
		mockPreparedStatement = (PreparedStatement) ctrlPreparedStatement.getMock();
		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
		ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
		mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
	}

	protected void replay() {
		super.replay();
		ctrlStatement.replay();
		ctrlPreparedStatement.replay();
		ctrlResultSet.replay();
		ctrlResultSetMetaData.replay();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (shouldVerify()) {
			ctrlStatement.verify();
			ctrlPreparedStatement.verify();
			ctrlResultSet.verify();
			ctrlResultSetMetaData.verify();
		}
	}

	public void testQueryForList() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1, 2);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 2);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData, 2);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(12));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql);
		assertEquals("All rows returned", 2, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
		assertEquals("Second row is Integer", 12, ((Integer)((Map)li.get(1)).get("age")).intValue());
	}

	public void testQueryForListWithEmptyResult() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		List li = template.queryForList(sql);
		assertEquals("All rows returned", 0, li.size());
	}

	public void testQueryForListWithSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql);
		assertEquals("All rows returned", 1, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
	}

	public void testQueryForListWithIntegerElement() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, Integer.class);
		assertEquals("All rows returned", 1, li.size());
		assertEquals("Element is Integer", 11, ((Integer) li.get(0)).intValue());
	}

	public void testQueryForMapWithSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Map map = template.queryForMap(sql);
		assertEquals("Wow is Integer", 11, ((Integer) map.get("age")).intValue());
	}


	public void testQueryForObjectWithRowMapper() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt(1);
		ctrlResultSet.setReturnValue(22);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Integer(rs.getInt(1));
			}
		});
		assertTrue("Correct result type", o instanceof Integer);
	}

	public void testQueryForObjectWithInteger() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(22));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, Integer.class);
		assertTrue("Correct result type", o instanceof Integer);
	}

	public void testQueryForInt() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(22));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql);
		assertEquals("Return of an int", 22, i);
	}

	public void testQueryForIntValueNotFound() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = -3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(null);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql);
		assertEquals("Return of an int", 0, i);
	}

	public void testQueryForLong() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new BigDecimal(87.0d));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		long l = template.queryForLong(sql);
		assertEquals("Return of a long", 87, l);
	}

	public void testQueryForListWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1, 2);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 2);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData, 2);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(12));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)});
		assertEquals("All rows returned", 2, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
		assertEquals("Second row is Integer", 12, ((Integer)((Map)li.get(1)).get("age")).intValue());
	}

	public void testQueryForListWithArgsAndEmptyResult() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)});
		assertEquals("All rows returned", 0, li.size());
	}

	public void testQueryForListWithArgsAndSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)});
		assertEquals("All rows returned", 1, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
	}

	public void testQueryForListWithArgsAndIntegerElementAndSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)}, Integer.class);
		assertEquals("All rows returned", 1, li.size());
		assertEquals("First row is Integer", 11, ((Integer) li.get(0)).intValue());
	}

	public void testQueryForMapWithArgsAndSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);
		mockResultSetMetaData.getColumnName(1);
		ctrlResultSetMetaData.setReturnValue("age", 1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(11));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Map map = template.queryForMap(sql, new Object[] {new Integer(3)});
		assertEquals("Row is Integer", 11, ((Integer) map.get("age")).intValue());
	}

	public void testQueryForObjectWithArgsAndRowMapper() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt(1);
		ctrlResultSet.setReturnValue(22);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, new Object[] {new Integer(3)}, new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Integer(rs.getInt(1));
			}
		});
		assertTrue("Correct result type", o instanceof Integer);
	}

	public void testQueryForObjectWithArgsAndInteger() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(22));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, new Object[] {new Integer(3)}, Integer.class);
		assertTrue("Correct result type", o instanceof Integer);
	}

	public void testQueryForIntWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new Integer(22));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql, new Object[] {new Integer(3)});
		assertEquals("Return of an int", 22, i);
	}

	public void testQueryForIntWithArgsAndValueNotFound() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(null);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(-3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql, new Object[] {new Integer(-3)});
		assertEquals("Return of an int", 0, i);
	}

	public void testQueryForLongWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		mockResultSetMetaData.getColumnCount();
		ctrlResultSetMetaData.setReturnValue(1);

		mockResultSet.getMetaData();
		ctrlResultSet.setReturnValue(mockResultSetMetaData);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getObject(1);
		ctrlResultSet.setReturnValue(new BigDecimal(87.0d));
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(3));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		long l = template.queryForLong(sql, new Object[] {new Integer(3)});
		assertEquals("Return of a long", 87, l);
	}

}
