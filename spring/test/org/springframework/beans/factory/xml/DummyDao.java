/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

import javax.sql.DataSource;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class DummyDao {
	
	DataSource ds;

	public DummyDao(DataSource ds) {
		this.ds = ds;
	}

}
