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

package org.springframework.core;

/**
 * Handy class for wrapping checked <code>Exceptions</code> with a root cause.
 *
 * <p>This time-honored technique is no longer necessary in Java 1.4, which
 * finally provides built-in support for exception nesting. Thus exceptions in
 * applications written to use Java 1.4 need not extend this class. To ease
 * migration, this class mirrors Java 1.4's nested exceptions as closely as possible.
 *
 * <p>This class is <code>abstract</code> to force the programmer to extend
 * the class. <code>getMessage</code> will include nested exception
 * information; <code>printStackTrace</code> and other like methods will
 * delegate to the wrapped exception, if any.
 *
 * <p>The similarity between this class and the {@link NestedRuntimeException}
 * class is unavoidable, as Java forces these two classes to have different
 * superclasses (ah, the inflexibility of concrete inheritance!).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getMessage
 * @see #printStackTrace
 * @see NestedRuntimeException
 */
public abstract class NestedCheckedException extends Exception {

	/** Use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = 7100714597678207546L;


	/**
	 * Construct a <code>NestedCheckedException</code> with the specified detail message.
	 * @param msg the detail message
	 */
	public NestedCheckedException(String msg) {
		super(msg);
	}

	/**
	 * Construct a <code>NestedCheckedException</code> with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public NestedCheckedException(String msg, Throwable cause) {
		super(msg, cause);
	}


	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
	public String getMessage() {
		return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
	}


	/**
	 * Retrieve the innermost cause of this exception, if any.
	 * @return the innermost exception, or <code>null</code> if none
	 */
	public Throwable getRootCause() {
		Throwable rootCause = getCause();
		if (rootCause != null) {
			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}
		}
		return rootCause;
	}

	/**
	 * Retrieve the most specific cause of this exception, that is,
	 * either the innermost cause (root cause) or this exception itself.
	 * <p>Differs from {@link #getRootCause()} in that it falls back
	 * to the present exception if there is no root cause.
	 * @return the most specific cause (never <code>null</code>)
	 * @since 2.0.3
	 */
	public Throwable getMostSpecificCause() {
		Throwable rootCause = getRootCause();
		return (rootCause != null ? rootCause : this);
	}

	/**
	 * Check whether this exception contains an exception of the given type:
	 * either it is of the given class itself or it contains a nested cause
	 * of the given type.
	 * <p>Currently just traverses <code>NestedCheckedException</code> causes.
	 * Will use the JDK 1.4 exception cause mechanism once Spring requires JDK 1.4.
	 * @param exType the exception type to look for
	 * @return whether there is a nested exception of the specified type
	 */
	public boolean contains(Class exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		Throwable cause = getCause();
		if (cause instanceof NestedCheckedException) {
			return ((NestedCheckedException) cause).contains(exType);
		}
		else {
			return (cause != null && exType.isInstance(cause));
		}
	}

}
