/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.test.context.junit4;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import junit.framework.JUnit4TestAdapter;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.TestClass;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.TestContextManager;

/**
 * Unit test for {@link SpringMethodRoadie} which focuses on proper support of
 * the {@link Repeat @Repeat} annotation.
 *
 * @author Sam Brannen
 * @since 2.1
 */
@RunWith(JUnit4ClassRunner.class)
public class RepeatedSpringMethodRoadieTests {

	protected final MockControl			notifierMockControl				= MockClassControl.createNiceControl(RunNotifier.class);

	protected final RunNotifier			notifier						= (RunNotifier) this.notifierMockControl.getMock();

	protected final MockControl			descriptionMockControl			= MockClassControl.createNiceControl(Description.class);

	protected final Description			description						= (Description) this.descriptionMockControl.getMock();

	protected final MockControl			testContextManagerMockControl	= MockClassControl.createNiceControl(TestContextManager.class);

	protected final TestContextManager	testContextManager				= (TestContextManager) this.testContextManagerMockControl.getMock();

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(RepeatedSpringMethodRoadieTests.class);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	private void assertRepetitions(final String methodName, final int expectedNumInvocations) throws Exception {

		final Class<RepeatedTestCase> clazz = RepeatedTestCase.class;
		final TestClass testClass = new TestClass(clazz);
		final RepeatedTestCase testInstance = clazz.newInstance();
		final Method method = clazz.getMethod(methodName, (Class[]) null);
		final SpringTestMethod testMethod = new SpringTestMethod(method, testClass);

		new SpringMethodRoadie(this.testContextManager, testInstance, testMethod, this.notifier, this.description).run();

		assertEquals("Verifying number of @Test invocations for test method [" + methodName + "].",
				expectedNumInvocations, testInstance.invocationCount);
		assertEquals("Verifying number of @Before invocations for test method [" + methodName + "].",
				expectedNumInvocations, testInstance.beforeCount);
		assertEquals("Verifying number of @After invocations for test method [" + methodName + "].",
				expectedNumInvocations, testInstance.afterCount);
	}

	@Test
	public void testRepeatAnnotationSupport() throws Exception {

		assertRepetitions("testNonAnnotated", 1);
		assertRepetitions("testNegativeRepeatValue", 1);
		assertRepetitions("testDefaultRepeatValue", 1);
		assertRepetitions("testRepeatedFiveTimes", 5);
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static class RepeatedTestCase {

		int	beforeCount		= 0;

		int	afterCount		= 0;

		int	invocationCount	= 0;

		@Before
		protected void setUp() throws Exception {

			this.beforeCount++;
		}

		@After
		protected void tearDown() throws Exception {

			this.afterCount++;
		}

		@Test
		@Timed(millis = 10000)
		public void testNonAnnotated() {

			this.invocationCount++;
		}

		@Test
		@Repeat(-5)
		@Timed(millis = 10000)
		public void testNegativeRepeatValue() {

			this.invocationCount++;
		}

		@Test
		@Repeat
		@Timed(millis = 10000)
		public void testDefaultRepeatValue() {

			this.invocationCount++;
		}

		@Test
		@Repeat(5)
		@Timed(millis = 10000)
		public void testRepeatedFiveTimes() {

			this.invocationCount++;
		}
	}

	// ------------------------------------------------------------------------|

}
