/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.remoting.support;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Encapsulates a remote invocation, providing core method invocation properties
 * in a serializable fashion. Used for RMI and HTTP-based serialization invokers.
 *
 * <p>This is an SPI class, typically not used directly by applications.
 * Can be subclassed for additional invocation parameters.
 *
 * @author Juergen Hoeller
 * @since 25.02.2004
 * @see RemoteInvocationFactory
 * @see RemoteInvocationExecutor
 */
public class RemoteInvocation implements Serializable {

	private String methodName;

	private Class[] parameterTypes;

	private Object[] arguments;


	/**
	 * Create a new RemoteInvocation for use as JavaBean.
	 */
	public RemoteInvocation() {
	}

	/**
	 * Create a new RemoteInvocation for the given parameters.
	 * @param methodName the name of the method to invoke
	 * @param argumentTypes the argument types of the method
	 * @param arguments the arguments for the invocation
	 */
	public RemoteInvocation(String methodName, Class[] argumentTypes, Object[] arguments) {
		this.methodName = methodName;
		this.parameterTypes = argumentTypes;
		this.arguments = arguments;
	}

	/**
	 * Create a new RemoteInvocation for the given AOP method invocation.
	 * @param methodInvocation the AOP invocation to convert
	 */
	public RemoteInvocation(MethodInvocation methodInvocation) {
		this.methodName = methodInvocation.getMethod().getName();
		this.parameterTypes = methodInvocation.getMethod().getParameterTypes();
		this.arguments = methodInvocation.getArguments();
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Object[] getArguments() {
		return arguments;
	}


	/**
	 * Perform this invocation on the given target object.
	 * Typically called when a RemoteInvocation is received on the server.
	 * @param targetObject the target object to apply the invocation to
	 * @return the invocation result
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 * @see java.lang.reflect.Method#invoke
	 */
	public Object invoke(Object targetObject)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = targetObject.getClass().getMethod(this.methodName, this.parameterTypes);
		return method.invoke(targetObject, this.arguments);
	}

}
