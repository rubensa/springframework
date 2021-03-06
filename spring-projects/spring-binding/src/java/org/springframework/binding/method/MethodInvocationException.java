/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.method;

import java.lang.reflect.InvocationTargetException;

import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.StylerUtils;

/**
 * Base class for exceptions that report a method invocation failure.
 * @author Keith Donald
 */
public class MethodInvocationException extends NestedRuntimeException {

	/**
	 * The method key.
	 */
	private ClassMethodKey methodKey;

	/**
	 * The method invocation argument values.
	 */
	private Object[] arguments;

	/**
	 * Signals that the method with the specified signature could not be invoked
	 * with the provided arguments.
	 * @param methodKey the method sig
	 * @param arguments the arguments
	 * @param cause the root cause
	 */
	public MethodInvocationException(ClassMethodKey methodKey, Object[] arguments, Exception cause) {
		super("Unable to invoke method " + methodKey + " with arguments " + StylerUtils.style(arguments), cause);
	}

	/**
	 * Returns the invoked method's signature.
	 */
	public ClassMethodKey getMethodKey() {
		return methodKey;
	}

	/**
	 * Returns the method invocation arguments;
	 */
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * Returns the target root cause exception of the method invocation failure.
	 * @returns the target throwable
	 */
	public Throwable getTargetException() {
		if (getCause() instanceof InvocationTargetException) {
			return ((InvocationTargetException)getCause()).getTargetException();
		}
		else {
			return getCause();
		}
	}
}