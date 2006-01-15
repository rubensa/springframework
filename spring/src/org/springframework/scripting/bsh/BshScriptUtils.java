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

package org.springframework.scripting.bsh;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.XThis;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utility methods for handling BeanShell-scripted objects.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class BshScriptUtils {

	/**
	 * Create a new BeanShell-scripted object from the given script source.
	 * @param scriptSource the script source text
	 * @param interfaces the interfaces that the scripted Java object
	 * is supposed to implement
	 * @return the scripted Java object
	 * @throws EvalError in case of BeanShell parsing failure
	 */
	public static Object createBshObject(String scriptSource, Class[] interfaces) throws EvalError {
		Assert.hasText(scriptSource, "Script source must not be empty");
		Assert.notEmpty(interfaces, "At least one script interface is required");
		Interpreter interpreter = new Interpreter();
		interpreter.eval(scriptSource);
		XThis xt = (XThis) interpreter.eval("return this");
		return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
				interfaces, new BshObjectInvocationHandler(xt));
	}


	/**
	 * InvocationHandler that invokes a BeanShell script method.
	 */
	private static class BshObjectInvocationHandler implements InvocationHandler {

		private final XThis xt;

		public BshObjectInvocationHandler(XThis xt) {
			this.xt = xt;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				return this.xt.invokeMethod(method.getName(), args);
			}
			catch (EvalError ex) {
				throw new BshExecutionException(ex);
			}
		}
	}


	/**
	 * Exception to be thrown on script execution failure.
	 */
	public static class BshExecutionException extends NestedRuntimeException {

		private BshExecutionException(EvalError ex) {
			super("BeanShell script execution failed", ex);
		}
	}

}
