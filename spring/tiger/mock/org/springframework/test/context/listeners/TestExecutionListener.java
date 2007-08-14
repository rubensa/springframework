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
package org.springframework.test.context.listeners;

import java.lang.reflect.Method;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;

/**
 * <p>
 * TestExecutionListener defines a <em>listener</em> API for reacting to test
 * execution events published by a {@link TestContextManager} with which a
 * listener is registered.
 * </p>
 * <p>
 * Concrete implementations must provide a <code>public</code> no-args
 * constructor, so that listeners can be instantiated transparently by tools and
 * configuration mechanisms.
 * </p>
 * <p>
 * Spring provides the following out-of-the-box implementations:
 * </p>
 * <ul>
 * <li>{@link DependencyInjectionTestExecutionListener}</li>
 * <li>{@link DirtiesContextTestExecutionListener}</li>
 * <li>{@link TransactionalTestExecutionListener}</li>
 * </ul>
 *
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.2
 */
public interface TestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Prepares the {@link Object test instance} of the supplied
	 * {@link TestContext test context} (e.g., injecting dependencies).
	 * </p>
	 * <p>
	 * This method should be called immediately after instantiation but prior to
	 * any framework-specific lifecycle callbacks.
	 * </p>
	 *
	 * @param testInstance The test object to prepare.
	 * @throws Exception if an error occurs while preparing the test instance.
	 */
	public abstract void prepareTestInstance(final TestContext<?> testContext) throws Exception;

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Pre-processes a test just <em>before</em> execution of the
	 * {@link Method test method} in the supplied
	 * {@link TestContext test context}, for example for setting up test
	 * fixtures.
	 * </p>
	 *
	 * @param testContext The test context in which the test method will be
	 *        executed, not <code>null</code>.
	 */
	public abstract void beforeTestMethod(final TestContext<?> testContext);

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Post-processes a test just <em>after</em> execution of the
	 * {@link Method test method} in the supplied
	 * {@link TestContext test context}, for example for tearing down test
	 * fixtures.
	 * </p>
	 *
	 * @param testContext The test context in which the test method was
	 *        executed, not <code>null</code>.
	 * @param t The Throwable that was thrown during execution of the test
	 *        method, or <code>null</code> if none was thrown.
	 */
	public abstract void afterTestMethod(final TestContext<?> testContext, final Throwable t);

	// ------------------------------------------------------------------------|

}
