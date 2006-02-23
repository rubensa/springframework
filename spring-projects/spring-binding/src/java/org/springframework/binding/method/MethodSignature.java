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

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;

/**
 * A specification for a <code>Method</code>, consisting of the methodName
 * and an optional set of named arguments.
 * 
 * @author Keith Donald
 */
public class MethodSignature implements Serializable {

	/**
	 * The name of the method, e.g execute
	 */
	private String methodName;

	/**
	 * The parameter types of the method, e.g int param1
	 */
	private Parameters parameters;

	/**
	 * Creates a method key with no arguments
	 * 
	 * @param methodName the name of the method.
	 */
	public MethodSignature(String methodName) {
		this(methodName, Parameters.NONE);
	}

	/**
	 * Creates a method key with a single argument.
	 * @param methodName the name of the method
	 * @param parameter the method argument
	 */
	public MethodSignature(String methodName, Parameter parameter) {
		this(methodName, new Parameters(parameter));
	}

	/**
	 * Creates a method key with a list of arguments.
	 * @param methodName the name of the method
	 * @param parameters the method arguments
	 */
	public MethodSignature(String methodName, Parameters parameters) {
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MethodSignature)) {
			return false;
		}
		MethodSignature other = (MethodSignature)obj;
		return methodName.equals(methodName) && parameters.equals(other.parameters);
	}

	public int hashCode() {
		return methodName.hashCode() + parameters.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("methodName", methodName).append("parameters", parameters).toString();
	}
}