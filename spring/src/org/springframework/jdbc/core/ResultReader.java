/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.util.List;


/**
 * Extension of RowCallbackHandler interfaces that saves the
 * accumulated results as a Collection.
 * @author Rod Johnson
 * @version $Id$
 */
public interface ResultReader extends RowCallbackHandler {
	 
	/**
	 * Return all results, disconnected from the JDBC ResultSet.
	 * @return all results, disconnected from the JDBC ResultSet.
	 * Never returns null; returns the empty collection if there
	 * were no results.
	 */
	List getResults();

}

