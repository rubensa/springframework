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
package org.springframework.test.context.junit38;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * <p>
 * Abstract {@link TestCase} which integrates the
 * <em>Spring Test Context Framework</em> with explicit
 * {@link ApplicationContext} testing support in a JUnit 3.8 environment.
 * </p>
 * <p>
 * Concrete subclasses must:
 * </p>
 * <ul>
 * <li>Declare a class-level {@link ContextConfiguration @ContextConfiguration}
 * annotation to configure the {@link ApplicationContext application context}
 * {@link ContextConfiguration#locations() resource locations}.</li>
 * <li>Declare public constructors which match the signatures of
 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests() AbstractJUnit38SpringContextTests()}
 * and
 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}
 * and delegate to <code>super();</code> and <code>super(name);</code>
 * respectively.</li>
 * </ul>
 *
 * @see TestContext
 * @see TestContextManager
 * @see TestExecutionListeners
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public class AbstractJUnit38SpringContextTests extends TestCase implements ApplicationContextAware {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private ApplicationContext			applicationContext;

	/** Logger available to subclasses. */
	protected final Log					logger	= LogFactory.getLog(getClass());

	private final TestContextManager	testContextManager;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Default <em>no argument</em> constructor which delegates to
	 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)},
	 * passing a value of <code>null</code> for the test name.
	 *
	 * @see TestCase#TestCase()
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractJUnit38SpringContextTests() throws Exception {

		this(null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new AbstractJUnit38SpringContextTests with the supplied
	 * <code>name</code>. Creates a new {@link TestContextManager} and calls
	 * {@link TestContextManager#prepareTestInstance(Object)} accordingly.
	 *
	 * @see TestCase#TestCase(String)
	 * @param name The name of the current test to execute.
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
		this.testContextManager = new TestContextManager(getClass());
		this.testContextManager.prepareTestInstance(this);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Overrides {@link TestCase#runBare()} by calling
	 * {@link TestContextManager#beforeTestMethod(Object, Method)} and
	 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable)} at
	 * the appropriate locations.
	 *
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {

		final Method testMethod = getClass().getMethod(getName(), (Class[]) null);

		Throwable exception = null;
		this.testContextManager.beforeTestMethod(this, testMethod);
		setUp();
		try {
			runTest();
		}
		catch (final Throwable running) {
			exception = running;
		}
		finally {
			try {
				tearDown();
			}
			catch (final Throwable tearingDown) {
				if (exception == null) {
					exception = tearingDown;
				}
			}
		}
		this.testContextManager.afterTestMethod(this, testMethod, exception);
		if (exception != null) {
			throw exception;
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Return the {@link ApplicationContext} that was injected into this test
	 * instance via {@link #setApplicationContext(ApplicationContext)}.
	 *
	 * @return The application context.
	 */
	public final ApplicationContext getApplicationContext() {

		return this.applicationContext;
	}

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

	// ------------------------------------------------------------------------|

}
