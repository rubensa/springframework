/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;
import org.springframework.util.ClassUtils;

/**
 * Invoker and cache for dispatch methods that all share the same target object.
 * The dispatch methods typically share the same form, but multiple exist per
 * target object, and they only differ in name.
 * 
 * @author Keith Donald
 */
public class DispatchMethodInvoker {

	/**
	 * The target object to dispatch to.
	 */
	private Object target;

	/**
	 * The parameter types describing the dispatch method signature.
	 */
	private Class[] parameterTypes;

	/**
	 * The resolved method cache.
	 */
	private Map methodCache = new CachingMapDecorator() {
		public Object create(Object key) {
			String methodName = (String)key;
			try {
				return target.getClass().getMethod(methodName, parameterTypes);
			}
			catch (NoSuchMethodException e) {
				throw new MethodLookupException("Unable to resolve dispatch method with name '" + methodName
						+ "' and signature '" + getSignatureString(methodName)
						+ "'; make sure the method name is correct " + "and such a method is defined on targetClass "
						+ target.getClass().getName(), e);
			}
		}
	};

	/**
	 * Creates a dispatch method invoker.
	 * @param target the target to dispatch to.
	 * @param parameterTypes the parameter types defining the argument signature
	 * of the dispatch methods
	 */
	public DispatchMethodInvoker(Object target, Class[] parameterTypes) {
		Assert.notNull(target, "The target of a dispatch method invocation is required");
		this.target = target;
		this.parameterTypes = parameterTypes;
	}

	/**
	 * Dispatch a call with given arguments to named dispatcher method.
	 * @param methodName the name of the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @return the result of the method invokation
	 * @throws MethodLookupException when the method cannot be resolved
	 * @throws Exception when the invoked method throws an exception
	 */
	public Object invoke(String methodName, Object[] arguments) throws MethodLookupException, Exception {
		try {
			Method dispatchMethod = getDispatchMethod(methodName);
			return dispatchMethod.invoke(target, arguments);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof Exception) {
				throw (Exception)e.getTargetException();
			}
			else {
				throw (Error)e.getTargetException();
			}
		}
	}

	/**
	 * Get a handle to the method of the specified name, with the signature
	 * defined by the configured parameter types and return type.
	 * @param methodName the method name
	 * @return the method
	 * @throws MethodLookupException when the method cannot be resolved
	 */
	private Method getDispatchMethod(String methodName) throws MethodLookupException {
		return (Method)methodCache.get(methodName);
	}

	/**
	 * Returns the signature of the dispatch methods invoked by this class.
	 * @param methodName name of the dispatch method
	 */
	private String getSignatureString(String methodName) {
		return methodName + "(" + getParameterTypesString() + ");";
	}

	/**
	 * Convenience method that returns the parameter types describing the
	 * signature of the dispatch method as a string.
	 */
	private String getParameterTypesString() {
		StringBuffer parameterTypesString = new StringBuffer();
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesString.append(ClassUtils.getShortName(parameterTypes[i]));
			if (i < parameterTypes.length - 1) {
				parameterTypesString.append(',');
			}
		}
		return parameterTypesString.toString();
	}

	/**
	 * Thrown when a method could not be resolved.
	 */
	public static class MethodLookupException extends NestedRuntimeException {

		/**
		 * Create a new method lookup exception.
		 * @param msg a descriptive message
		 * @param ex the underlying cause of this exception
		 */
		public MethodLookupException(String msg, Throwable ex) {
			super(msg, ex);
		}
	}
}