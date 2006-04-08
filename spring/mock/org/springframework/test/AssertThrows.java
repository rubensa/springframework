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

package org.springframework.test;

import junit.framework.Assert;

/**
 * Simple method object encapsulation of the 'test-for-Exception'
 * scenario (for JUnit).
 * 
 * <p>Used like so:
 * 
 * <pre class="code">// the class under test
 *public class Foo {
 *
 *    public void someBusinessLogic(String name) {
 *        if(name == null) {
 *            throw new IllegalArgumentException("The 'name' argument is required");
 *        }
 *        // rest of business logic here...
 *    }
 *}
 * </pre>
 * 
 * <p>The test for the above bad argument path can be expressed using the
 * {@link AssertThrows} class like so:
 * 
 * <pre class="code">public final class FooTest {
 *
 *    public void testSomeBusinessLogicBadArgumentPath() {
 *        new AssertThrows(IllegalArgumentException.class) {
 *            public void test() {
 *                new Foo().someBusinessLogic(null);
 *            }
 *        }.runTest();
 *    }
 *}
 * </pre>
 * 
 * <p>This will result in the test passing if the <code>Foo.someBusinessLogic(..)</code>
 * method threw an {@link java.lang.IllegalArgumentException}; if it did not, the
 * test would fail with the following message:
 * 
 * <pre class="code">
 *    "Must have thrown a [class java.lang.IllegalArgumentException]"
 * </pre>
 * 
 * <p>If the <b>wrong</b> type of {@link java.lang.Exception} was thrown, the
 * test will also fail, this time with a message similar to the following:
 * 
 * <pre class="code">
 *    "junit.framework.AssertionFailedError: Was expecting a [class java.lang.UnsupportedOperationException] to be thrown, but instead a [class java.lang.IllegalArgumentException] was thrown"
 * </pre>
 * 
 * <p>The test for the correct {@link java.lang.Exception} respects polymorphism,
 * so you can test that any old {@link java.lang.Exception} is thrown like so:
 * 
 * <pre class="code">public final class FooTest {
 *
 *    public void testSomeBusinessLogicBadArgumentPath() {
 *        // any Exception will do...
 *        new AssertThrows(Exception.class) {
 *            public void test() {
 *                new Foo().someBusinessLogic(null);
 *            }
 *        }.runTest();
 *    }
 *}
 * </pre>
 * 
 * <p>You might want to compare this class with the
 * {@link junit.extensions.ExceptionTestCase} class.
 * 
 * @author Rick Evans
 */
public abstract class AssertThrows {

	private Class expectedException;
	private String failureMessage;


    /**
	 * Creates a new instance of the {@link AssertThrows} class. 
	 * @param expectedException the {@link java.lang.Exception} expected to be thrown during the execution of the surrounding test
	 * @throws IllegalArgumentException if the supplied <code>expectedException</code> is <code>null</code>; or if said argument is not an {@link java.lang.Exception}-derived class
	 */
	public AssertThrows(Class expectedException) {
		this(expectedException, null);
	}

	/**
	 * Creates a new instance of the {@link AssertThrows} class. 
	 * @param expectedException the {@link java.lang.Exception} expected to be thrown during the execution of the surrounding test
	 * @param failureMessage the extra, contextual failure message that will be included in the failure text if the text fails (can be <code>null</code>)
	 * @throws IllegalArgumentException if the supplied <code>expectedException</code> is <code>null</code>; or if said argument is not an {@link java.lang.Exception}-derived class
	 */
	public AssertThrows(Class expectedException, String failureMessage) {
		if(expectedException == null) {
			throw new IllegalArgumentException("The 'expectedException' argument is required.");
		}
		if(!Exception.class.isAssignableFrom(expectedException)) {
			throw new IllegalArgumentException("The 'expectedException' argument is not an Exception type (it obviously must be).");
		}
		this.expectedException = expectedException;
		this.failureMessage = failureMessage;
	}


    /**
	 * Gets the {@link java.lang.Exception} expected to be thrown during the execution of the surrounding test. 
	 * @return the {@link java.lang.Exception} expected to be thrown during the execution of the surrounding test
	 */
	protected Class getExpectedException() {
		return expectedException;
	}

	/**
	 * Gets the extra, contextual failure message that will be included in the failure text if the text fails.
	 * @return the extra, contextual failure message that will be included in the failure text if the text fails (might be <code>null</code>).
	 */
	protected String getFailureMessage() {
		return failureMessage;
	}

	/**
	 * Sets the extra, contextual failure message that will be included in the failure text if the text fails.
	 * @param failureMessage the extra, contextual failure message that will be included in the failure text if the text fails (can be <code>null</code>)
	 */
	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}


    /**
	 * Subclass must override this <code>abstract</code> method and provide
	 * the test logic.
	 * @throws Exception if an error occurs during the execution of the aformentioned test logic
	 */
	public abstract void test() throws Exception;

	/**
	 * The main template method that drives the running of the
	 * {@link #test() test logic} and the
	 * {@link #checkExceptionExpectations(Exception) checking} of the
	 * resulting (expected) {@link java.lang.Exception}.
	 * @see #test() 
	 * @see #doFail()
	 * @see #checkExceptionExpectations(Exception)
	 */
	public void runTest() {
		try {
			test();
			doFail();
		}
		catch (Exception actualException) {
			checkExceptionExpectations(actualException);
		}
	}

	/**
	 * Template method called when the test fails; i.e. the expected
	 * {@link java.lang.Exception} is <b>not</b> thrown.
	 * <p>The default implementation simply fails the test via a call to
	 * {@link junit.framework.Assert#fail(String)}.
	 * <p>If you want to customise the failure message, consider overriding
	 * {@link #createMessageForNoExceptionThrown()}, and / or supplying an
	 * extra, contextual failure message via the appropriate constructor overload.
	 * @see #getFailureMessage()
	 */
	protected void doFail() {
		Assert.fail(createMessageForNoExceptionThrown());
	}

	/**
	 * Creates the failure message used if the test fails
	 * (i.e. the expected exception is not thrown in the body of the test).
	 * @return the failure message used if the test fails
	 * @see #getFailureMessage()
	 */
	protected String createMessageForNoExceptionThrown() {
		StringBuffer buffer = new StringBuffer("Must have thrown a [")
				.append(this.getExpectedException())
				.append("]");
		if (this.getFailureMessage() != null) {
			buffer.append(" : ").append(this.getFailureMessage()).append(".");
		}
		return buffer.toString();
	}

	/**
	 * Does the donkey work of checking (verifying) that the
	 * {@link java.lang.Exception} that was thrown in the body of a test is
	 * an instance of the {@link #getExpectedException()} class (or an
	 * instance of a subclass).
	 * <p>If you want to customise the failure message, consider overriding
	 * {@link #createMessageForWrongThrownExceptionType(Exception)}.
	 * @param actualException the {@link java.lang.Exception} that has been thrown in the body of a test method (will never be <code>null</code>)
	 */
	protected void checkExceptionExpectations(Exception actualException) {
		Class actualExceptionType = actualException.getClass();
		Assert.assertTrue(
				createMessageForWrongThrownExceptionType(actualException),
				this.getExpectedException().isAssignableFrom(actualExceptionType));
	}

	/**
	 * Creates the failure message used if the wrong type
	 * of {@link java.lang.Exception} is thrown in the body of the test.
	 * @return the failure message used if the wrong type
	 * of {@link java.lang.Exception} is thrown in the body of the test.
	 */
	protected String createMessageForWrongThrownExceptionType(Exception actualException) {
		StringBuffer buffer = new StringBuffer()
				.append("Was expecting a [")
				.append(this.getExpectedException())
				.append("] to be thrown, but instead a [")
				.append(actualException.getClass())
				.append("] was thrown.");
		return buffer.toString();
	}

}
