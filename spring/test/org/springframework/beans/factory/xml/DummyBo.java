/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class DummyBo {
	
	DummyDao dao;

	public DummyBo(DummyDao dao) {
		this.dao = dao;
		
	}

}
