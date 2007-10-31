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

import org.junit.Test;

import org.springframework.test.context.TestContextManager;

/**
 * Unit tests for {@link SpringJUnit4ClassRunner}.
 *
 * @author Rick Evans
 * @author Sam Brannen
 * @since 2.5
 */
public class SpringJUnit4ClassRunnerTests {

	@Test(expected = Exception.class)
	public void checkThatExceptionsAreNotSilentlySwallowed() throws Exception {
		SpringJUnit4ClassRunner runner = new SpringJUnit4ClassRunner(getClass()) {

			@Override
			protected TestContextManager createTestContextManager(Class<?> clazz) {
				return new TestContextManager(clazz) {

					@Override
					public void prepareTestInstance(Object testInstance) throws Throwable {
						throw new RuntimeException("This RuntimeException should be caught and wrapped in an Exception.");
					}
				};
			}
		};
		runner.createTest();
	}
}
