/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests;

import junit.framework.TestCase;


/**
 * AbstractTestCase
 * 
 * @author Darren Davison
 * @version $Id$
 */
public abstract class AbstractTestCase extends TestCase {

	protected String testServer = "http://localhost:" + System.getProperty("autobuilds.server.port");
	
    /**
     * @param arg0
     */
    public AbstractTestCase(String arg0) {
        super(arg0);
    }

}
