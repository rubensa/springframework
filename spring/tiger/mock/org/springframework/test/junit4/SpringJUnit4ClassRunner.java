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
package org.springframework.test.junit4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.context.TestContextManager;

/**
 * <p>
 * SpringJUnit4ClassRunner is a custom extension of {@link JUnit4ClassRunner}
 * which provides Spring testing functionality to standard JUnit 4.4+ tests by
 * means of the {@link TestContextManager} and associated support classes and
 * annotations.
 * </p>
 *
 * @see TestContextManager
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
public class SpringJUnit4ClassRunner<T> extends JUnit4ClassRunner {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log			LOG	= LogFactory.getLog(SpringJUnit4ClassRunner.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestContextManager<T>	testContextManager;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new <code>SpringJUnit4ClassRunner</code> and initializes a
	 * {@link TestContextManager} to provide Spring testing functionality to
	 * standard JUnit tests.
	 *
	 * @param clazz the Class object corresponding to the test class to be run.
	 * @see #createTestContextManager(Class)
	 * @throws InitializationError if an error occurs while initializing the
	 *         runner.
	 */
	public SpringJUnit4ClassRunner(final Class<T> clazz) throws InitializationError {

		super(clazz);

		if (LOG.isDebugEnabled()) {
			LOG.debug("SpringJUnit4ClassRunner constructor called with [" + clazz + "].");
		}

		try {
			this.testContextManager = createTestContextManager(clazz);
		}
		catch (final Exception e) {
			LOG.error("Caught an exception while attempting to instantiate a new TestContextManager for test class ["
					+ clazz + "].", e);
			throw new InitializationError(e);
		}
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Delegates to {@link JUnit4ClassRunner#createTest()} to create the test
	 * instance and then to a {@link TestContextManager} to
	 * {@link TestContextManager#prepareTestInstance(Object) prepare} the test
	 * instance for Spring testing functionality.
	 *
	 * @see JUnit4ClassRunner#createTest()
	 * @see TestContextManager#prepareTestInstance(Object)
	 * @return A new test instance.
	 * @throws Exception if an error occurs while creating or preparing the test
	 *         instance.
	 */
	@Override
	protected T createTest() throws Exception {

		@SuppressWarnings("unchecked")
		final T testInstance = (T) super.createTest();
		getTestContextManager().prepareTestInstance(testInstance);
		return testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Creates a new {@link TestContextManager}. Can be overridden by
	 * subclasses.
	 *
	 * @param clazz the Class object corresponding to the test class to be
	 *        managed.
	 * @return A new TestContextManager
	 * @throws Exception if an error occurs while creating a new
	 *         TestContextManager.
	 */
	protected TestContextManager<T> createTestContextManager(final Class<T> clazz) throws Exception {

		return new TestContextManager<T>(clazz);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link TestContextManager} associated with this runner.
	 *
	 * @return The TestContextManager.
	 */
	protected final TestContextManager<T> getTestContextManager() {

		return this.testContextManager;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Invokes the supplied {@link Method test method} and notifies the supplied
	 * {@link RunNotifier} of the appropriate events.
	 *
	 * @see #createTest()
	 * @see JUnit4ClassRunner#invokeTestMethod(Method, RunNotifier)
	 */
	@Override
	protected void invokeTestMethod(final Method method, final RunNotifier notifier) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Invoking test method [" + method.toGenericString() + "].");
		}

		// ---------------------------------------------------------------------
		// The following is a 1-to-1 copy of the original JUnit 4.4 code, except
		// that we use custom implementations for TestMethod and MethodRoadie.

		final Description description = methodDescription(method);
		T testInstance;
		try {
			testInstance = createTest();
		}
		catch (final InvocationTargetException e) {
			notifier.testAborted(description, e.getCause());
			return;
		}
		catch (final Exception e) {
			notifier.testAborted(description, e);
			return;
		}

		final SpringTestMethod testMethod = new SpringTestMethod(method, getTestClass());
		new SpringMethodRoadie<T>(getTestContextManager(), testInstance, testMethod, notifier, description).run();
	}

	// ------------------------------------------------------------------------|

}
