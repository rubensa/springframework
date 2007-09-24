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

package org.springframework.test.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * JUnit 4 based unit test which verifies correct
 * {@link ContextCache application context caching} in conjunction with the
 * {@link SpringJUnit4ClassRunner} and the {@link DirtiesContext} annotation.
 *
 * @author Sam Brannen
 * @since 2.5
 */
@RunWith(SpringRunnerContextCacheTests.TestableSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/springframework/test/context/junit4/SpringJUnit4ClassRunnerAppCtxTests-context.xml" })
public class SpringRunnerContextCacheTests implements ApplicationContextAware {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	private static ApplicationContext dirtiedApplicationContext;

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected ApplicationContext applicationContext;


	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SpringRunnerContextCacheTests.class);
	}

	/**
	 * Asserts the statistics of the supplied context cache.
	 *
	 * @param usageScenario the scenario in which the statistics are used.
	 * @param expectedSize the expected number of contexts in the cache.
	 * @param expectedHitCount the expected hit count.
	 * @param expectedMissCount the expected miss count.
	 */
	public static final void assertContextCacheStatistics(final String usageScenario, final int expectedSize,
			final int expectedHitCount, final int expectedMissCount) {
		final ContextCache<String, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertEquals("Verifying number of contexts in cache (" + usageScenario + ").", expectedSize,
				contextCache.size());
		assertEquals("Verifying number of cache hits (" + usageScenario + ").", expectedHitCount,
				contextCache.getHitCount());
		assertEquals("Verifying number of cache misses (" + usageScenario + ").", expectedMissCount,
				contextCache.getMissCount());
	}

	@BeforeClass
	public static void verifyInitialCacheState() {
		dirtiedApplicationContext = null;
		final ContextCache<String, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		contextCache.clear();
		contextCache.clearStatistics();
		assertContextCacheStatistics("BeforeClass", 0, 0, 0);
	}

	@AfterClass
	public static void verifyFinalCacheState() {
		assertContextCacheStatistics("AfterClass", 1, 1, 2);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Sets the {@link ApplicationContext} to be used by this test instance,
	 * provided via {@link ApplicationContextAware} semantics.
	 *
	 * @param applicationContext The applicationContext to set.
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Test
	@DirtiesContext
	public void dirtyContext() {
		assertContextCacheStatistics("dirtyContext()", 1, 0, 1);
		assertNotNull("The application context should have been set due to ApplicationContextAware semantics.",
				this.applicationContext);
		SpringRunnerContextCacheTests.dirtiedApplicationContext = this.applicationContext;
	}

	@Test
	public void verifyContextWasDirtied() {
		assertContextCacheStatistics("verifyContextWasDirtied()", 1, 0, 2);
		assertNotNull("The application context should have been set due to ApplicationContextAware semantics.",
				this.applicationContext);
		assertNotSame("The application context should have been 'dirtied'.",
				SpringRunnerContextCacheTests.dirtiedApplicationContext, this.applicationContext);
		SpringRunnerContextCacheTests.dirtiedApplicationContext = this.applicationContext;
	}

	@Test
	public void verifyContextWasNotDirtied() {
		assertContextCacheStatistics("verifyContextWasNotDirtied()", 1, 1, 2);
		assertNotNull("The application context should have been set due to ApplicationContextAware semantics.",
				this.applicationContext);
		assertSame("The application context should NOT have been 'dirtied'.",
				SpringRunnerContextCacheTests.dirtiedApplicationContext, this.applicationContext);
	}


	// ------------------------------------------------------------------------|
	// --- STATIC CLASSES -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class TestableSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

		static TestableTestContextManager testableTestContextManager;


		public TestableSpringJUnit4ClassRunner(final Class<?> clazz) throws InitializationError {
			super(clazz);
		}

		@Override
		protected TestContextManager createTestContextManager(final Class<?> clazz) throws Exception {
			final TestableTestContextManager testableTestContextManager = new TestableTestContextManager(clazz);
			TestableSpringJUnit4ClassRunner.testableTestContextManager = testableTestContextManager;
			return testableTestContextManager;
		}
	}

	private static class TestableTestContextManager extends TestContextManager {

		public TestableTestContextManager(final Class<?> testClass) throws Exception {
			super(testClass);
		}

		ContextCache<String, ApplicationContext> getVisibleContextCache() {
			return super.getContextCache();
		}
	}

}
