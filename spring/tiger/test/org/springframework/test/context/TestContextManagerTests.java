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
package org.springframework.test.context;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.context.listeners.AbstractTestExecutionListener;
import org.springframework.test.context.listeners.TestExecutionListener;

/**
 * JUnit 4 based unit test for {@link TestContextManager}, which verifies
 * proper <em>execution order</em> of registered
 * {@link TestExecutionListener TestExecutionListeners}.
 *
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
@RunWith(JUnit4ClassRunner.class)
public class TestContextManagerTests {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	private static final String FIRST = "veni";

	private static final String SECOND = "vidi";

	private static final String THIRD = "vici";

	private static final List<String> afterTestMethodCalls = new ArrayList<String>();

	private static final List<String> beforeTestMethodCalls = new ArrayList<String>();

	/** Class Logger. */
	protected static final Log LOG = LogFactory.getLog(TestContextManagerTests.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	private TestContextManager<?> testContextManager = null;

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Asserts the <em>execution order</em> of 'before' and 'after' test
	 * method calls on {@link TestExecutionListener listeners} registered for
	 * the configured {@link TestContextManager}.
	 *
	 * @see #beforeTestMethodCalls
	 * @see #afterTestMethodCalls
	 * @param expectedBeforeTestMethodCalls
	 * @param expectedAfterTestMethodCalls
	 * @param usageContext
	 */
	private static void assertExecutionOrder(List<String> expectedBeforeTestMethodCalls,
			List<String> expectedAfterTestMethodCalls, final String usageContext) {

		if (expectedBeforeTestMethodCalls == null) {
			expectedBeforeTestMethodCalls = new ArrayList<String>();
		}
		if (expectedAfterTestMethodCalls == null) {
			expectedAfterTestMethodCalls = new ArrayList<String>();
		}

		if (LOG.isDebugEnabled()) {
			for (final String listenerName : beforeTestMethodCalls) {
				LOG.debug("'before' listener [" + listenerName + "] (" + usageContext + ").");
			}
			for (final String listenerName : afterTestMethodCalls) {
				LOG.debug("'after' listener [" + listenerName + "] (" + usageContext + ").");
			}
		}

		assertTrue("Verifying execution order of 'before' listeners' (" + usageContext + ").",
				CollectionUtils.isEqualCollection(expectedBeforeTestMethodCalls, beforeTestMethodCalls));
		assertTrue("Verifying execution order of 'after' listeners' (" + usageContext + ").",
				CollectionUtils.isEqualCollection(expectedAfterTestMethodCalls, afterTestMethodCalls));
	}

	// ------------------------------------------------------------------------|

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		beforeTestMethodCalls.clear();
		afterTestMethodCalls.clear();

		assertExecutionOrder(null, null, "BeforeClass");
	}

	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(TestContextManagerTests.class);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Verifies the expected {@link TestExecutionListener}
	 * <em>execution order</em> after all test methods have completed.
	 *
	 * @see #verifyListenerExecutionOrder()
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void verifyListenerExecutionOrderAfterClass() throws Exception {

		assertExecutionOrder(Arrays.<String> asList(FIRST, SECOND, THIRD),
				Arrays.<String> asList(THIRD, SECOND, FIRST), "AfterClass");
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Before
	public void setUpTestContextManager() throws Exception {

		final Method testMethod = ExampleTest.class.getDeclaredMethod("exampleTestMethod", (Class<?>[]) null);

		this.testContextManager = new TestContextManager<ExampleTest>(ExampleTest.class);
		this.testContextManager.registerTestExecutionListeners(new NamedTestExecutionListener(FIRST),
				new NamedTestExecutionListener(SECOND), new NamedTestExecutionListener(THIRD));

		this.testContextManager.beforeTestMethod(new ExampleTest(), testMethod);
	}

	/**
	 * Verifies the expected {@link TestExecutionListener}
	 * <em>execution order</em> within a test method.
	 *
	 * @see #verifyListenerExecutionOrderAfterClass()
	 */
	@Test
	public void verifyListenerExecutionOrderWithinTestMethod() {

		assertExecutionOrder(Arrays.<String> asList(FIRST, SECOND, THIRD), null, "Test");
	}

	@After
	public void tearDownTestContextManager() throws Exception {

		final Method testMethod = ExampleTest.class.getDeclaredMethod("exampleTestMethod", (Class<?>[]) null);

		this.testContextManager.afterTestMethod(new ExampleTest(), testMethod);
		this.testContextManager = null;
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	@ContextConfiguration
	static class ExampleTest {

		public void exampleTestMethod() {

			assertTrue(true);
		}
	}

	static class NamedTestExecutionListener extends AbstractTestExecutionListener {

		private final String name;

		public NamedTestExecutionListener(final String name) {

			this.name = name;
		}

		@Override
		public void afterTestMethod(final TestContext<?> testContext, final Throwable t) {

			afterTestMethodCalls.add(this.name);
		}

		@Override
		public void beforeTestMethod(final TestContext<?> testContext) {

			beforeTestMethodCalls.add(this.name);
		}

		@Override
		public String toString() {

			return new ToStringBuilder(this).append("name", this.name).toString();
		}
	}

	// ------------------------------------------------------------------------|

}
