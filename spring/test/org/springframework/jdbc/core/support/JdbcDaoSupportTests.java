package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Juergen Hoeller
 * @since 30.07.2003
 */
public class JdbcDaoSupportTests extends TestCase {

	public void testJdbcDaoSupportWithDataSource() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getMetaData();
		conControl.setReturnValue(null, 1);
		con.close();
		conControl.setVoidCallable(1);
		dsControl.replay();
		conControl.replay();
		final List test = new ArrayList();
		JdbcDaoSupport dao = new JdbcDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setDataSource(ds);
		dao.afterPropertiesSet();
		assertEquals("Correct DataSource", ds, dao.getDataSource());
		assertEquals("Correct JdbcTemplate", ds, dao.getJdbcTemplate().getDataSource());
		assertEquals("initDao called", test.size(), 1);
		dsControl.verify();
		conControl.verify();
	}

	public void testJdbcDaoSupportWithJdbcTemplate() throws Exception {
		JdbcTemplate template = new JdbcTemplate();
		final List test = new ArrayList();
		JdbcDaoSupport dao = new JdbcDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setJdbcTemplate(template);
		dao.afterPropertiesSet();
		assertEquals("Correct JdbcTemplate", dao.getJdbcTemplate(), template);
		assertEquals("initDao called", test.size(), 1);
	}

}
