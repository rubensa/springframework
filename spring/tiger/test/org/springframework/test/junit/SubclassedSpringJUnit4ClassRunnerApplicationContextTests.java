/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.junit;

import java.lang.annotation.Inherited;

import junit.framework.JUnit4TestAdapter;

import org.springframework.test.annotation.ContextConfiguration;

/**
 * Subclass of {@link SpringJUnit4ClassRunnerApplicationContextTests} which
 * verifies that configuration of the application context and dependency
 * injection of the test instance function as expected within a class hierarchy
 * since {@link ContextConfiguration} is defined as {@link Inherited}.
 *
 * @see SpringJUnit4ClassRunnerApplicationContextTests
 * @see DuplicateSpringJUnit4ClassRunnerApplicationContextTests
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.2
 */
public class SubclassedSpringJUnit4ClassRunnerApplicationContextTests extends
		SpringJUnit4ClassRunnerApplicationContextTests {

	/* all tests are in the parent class. */

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(SubclassedSpringJUnit4ClassRunnerApplicationContextTests.class);
	}

}
