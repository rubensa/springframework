package org.springframework.jdbc.object;

import java.sql.Types;

import javax.sql.DataSource;

import com.mockobjects.sql.MockDataSource;
import junit.framework.TestCase;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * @author Trevor D. Cook
 */
public class RdbmsOperationTestSuite extends TestCase {

	public void testEmptySql() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		try {
			operation.compile();
			fail("Shouldn't allow compiling without sql statement");
 
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testSetTypeAfterCompile() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		operation.setDataSource(new MockDataSource());
		operation.setSql("select * from mytable");
		operation.compile();
		try {
			operation.setTypes(new int[] {Types.INTEGER });
			fail("Shouldn't allow setting parameters after compile");

		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testDeclareParameterAfterCompile() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		operation.setDataSource(new MockDataSource());
		operation.setSql("select * from mytable");
		operation.compile();
		try {
			operation.declareParameter(new SqlParameter(Types.INTEGER));
			fail("Shouldn't allow setting parameters after compile");
 
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testTooFewParameters() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		operation.setSql("select * from mytable");
		operation.setTypes(new int[] { Types.INTEGER });
		try {
			operation.validateParameters((Object[]) null);
			fail("Shouldn't validate without enough parameters"); 
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}
	
	
	public void testOperationConfiguredViaJdbcTemplateMustGetDataSource() throws Exception {
		JdbcTemplate t = new JdbcTemplate();
		try {
			TestRdbmsOperation operation = new TestRdbmsOperation();
			operation.setSql("foo");
			operation.compile();
			fail("Can't compile without providing a DataSource for the JdbcTemplate");
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// Check for helpful error message. Omit leading character
			// so as not to be fussy about case
			assertTrue(ex.getMessage().indexOf("ataSource") != -1);
		}
	}

	public void testTooManyParameters() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		operation.setSql("select * from mytable");
		try {
			operation.validateParameters(new Object[] { new Integer(1), new Integer(2) });
			fail("Shouldn't validate with too many parameters"); 
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testCompileTwice() {
		TestRdbmsOperation operation = new TestRdbmsOperation();
		operation.setDataSource(new MockDataSource());
		operation.setSql("select * from mytable");
		operation.setTypes(null);
		operation.compile();
		operation.compile();
	}

	public void testEmptyDataSource() {
		SqlOperation operation = new SqlOperation() {
		};
		operation.setSql("select * from mytable");
		try {
		operation.compile();
		fail("Shouldn't allow compiling without data source");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testParameterPropagation() {
		SqlOperation operation = new SqlOperation() {
		};
		DataSource ds = new DriverManagerDataSource();
		operation.setDataSource(ds);
		JdbcTemplate jt = operation.getJdbcTemplate();
		assertEquals(ds, jt.getDataSource());
	}


	private static class TestRdbmsOperation extends RdbmsOperation {

		protected void compileInternal() {
		}
	}

}
