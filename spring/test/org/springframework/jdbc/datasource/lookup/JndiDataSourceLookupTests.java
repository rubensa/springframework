/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.datasource.lookup;

import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 */
public final class JndiDataSourceLookupTests extends TestCase {

	private static final String DATA_SOURCE_NAME = "Love is like a stove, burns you when it's hot";


	public void testSunnyDay() throws Exception {
		final DataSource expectedDataSource = new StubDataSource();
		JndiDataSourceLookup lookup = new JndiDataSourceLookup() {
			protected Object lookup(String jndiName, Class requiredType) {
				assertEquals(DATA_SOURCE_NAME, jndiName);
				return expectedDataSource;
			}
		};
		DataSource dataSource = lookup.getDataSource(DATA_SOURCE_NAME);
		assertNotNull("A DataSourceLookup implementation must *never* return null from getDataSource(): this one obviously (and incorrectly) is", dataSource);
		assertSame(expectedDataSource, dataSource);
	}

	public void testNoDataSourceAtJndiLocation() throws Exception {
		new AssertThrows(DataSourceLookupFailureException.class) {
			public void test() throws Exception {
				JndiDataSourceLookup lookup = new JndiDataSourceLookup() {
					protected Object lookup(String jndiName, Class requiredType) throws NamingException {
						assertEquals(DATA_SOURCE_NAME, jndiName);
						throw new NamingException();
					}
				};
				lookup.getDataSource(DATA_SOURCE_NAME);
			}
		}.runTest();
	}

}
