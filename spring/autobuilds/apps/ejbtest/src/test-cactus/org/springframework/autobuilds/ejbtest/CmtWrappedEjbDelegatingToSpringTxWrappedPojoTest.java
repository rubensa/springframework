/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.autobuilds.ejbtest;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.ServletTestSuite;
import org.springframework.autobuilds.ejbtest.Constants;
import org.springframework.autobuilds.ejbtest.simple.SimpleService;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Test usage of EJB Remote proxy with and without a cached home. Also test EJB Local proxy (home
 * cache is not changed for this, since there is no difference in behaviour from remote).
 * 
 * @author colin sampaleanu
 * @version $Id$
 */
public class CmtWrappedEjbDelegatingToSpringTxWrappedPojoTest extends TestCase {

	// --- statics
	public static final String SERVICE_ID_LOCAL_PROXY = "simpleEjbLocalProxy";

	// --- attributes

	BeanFactoryReference bfr;
	
	// --- methods

	public static Test suite() {
		ServletTestSuite suite = new ServletTestSuite();
		suite.addTestSuite(CmtWrappedEjbDelegatingToSpringTxWrappedPojoTest.class);
		return suite;
	}

	protected void setUp() throws Exception {
		bfr = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(
				Constants.SERVICE_LAYER_CONTEXT_ID);
	}

	protected void tearDown() throws Exception {
		bfr.release();
	}

	public void testLocalInvocations() {
		SimpleService ejb = (SimpleService) bfr.getFactory().getBean(SERVICE_ID_LOCAL_PROXY);
		ejb.echo("hello");
		ejb.echo("hello2");
		ejb.echo2("whatever");
		ejb.echo2("whatever2");
	}
}
