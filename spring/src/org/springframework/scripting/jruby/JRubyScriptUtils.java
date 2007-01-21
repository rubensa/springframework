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

package org.springframework.scripting.jruby;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import org.jruby.IRuby;
import org.jruby.RubyException;
import org.jruby.RubyNil;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.exceptions.JumpException;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.util.ClassUtils;

/**
 * Utility methods for handling JRuby-scripted objects.
 *
 * <p>Note: As of Spring 2.0.2, this class requires JRuby 0.9.2 or higher.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public abstract class JRubyScriptUtils {

	/**
	 * Create a new JRuby-scripted object from the given script source,
	 * using the default {@link ClassLoader}.
	 * @param scriptSource the script source text
	 * @param interfaces the interfaces that the scripted Java object is to implement
	 * @return the scripted Java object
	 * @throws JumpException in case of JRuby parsing failure
	 * @see ClassUtils#getDefaultClassLoader()
	 */
	public static Object createJRubyObject(String scriptSource, Class[] interfaces) throws JumpException {
		return createJRubyObject(scriptSource, interfaces, ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Create a new JRuby-scripted object from the given script source.
	 * @param scriptSource the script source text
	 * @param interfaces the interfaces that the scripted Java object is to implement
	 * @param classLoader the {@link ClassLoader} to create the script proxy with
	 * @return the scripted Java object
	 * @throws JumpException in case of JRuby parsing failure
	 */
	public static Object createJRubyObject(String scriptSource, Class[] interfaces, ClassLoader classLoader)
			throws JumpException {

		IRuby ruby = initializeRuntime();

		Node scriptRootNode = ruby.parse(scriptSource, "", null);
		IRubyObject rubyObject = ruby.eval(scriptRootNode);

		if (rubyObject instanceof RubyNil) {
			String className = findClassName(scriptRootNode);
			rubyObject = ruby.evalScript("\n" + className + ".new");
		}
		// still null?
		if (rubyObject instanceof RubyNil) {
			throw new ScriptCompilationException("Compilation of JRuby script returned '" + rubyObject + "'.");
		}

		return Proxy.newProxyInstance(classLoader, interfaces, new RubyObjectInvocationHandler(rubyObject, ruby));
	}

	/**
	 * Initializes an instance of the {@link IRuby} runtime.
	 */
	private static IRuby initializeRuntime() {
		return JavaEmbedUtils.initialize(Collections.EMPTY_LIST);
	}

	/**
	 * Given the root {@link Node} in a JRuby AST will locate the name of the
	 * class defined by that AST.
	 * @throws IllegalArgumentException if no class is defined by the supplied AST
	 */
	private static String findClassName(Node rootNode) {
		ClassNode classNode = findClassNode(rootNode);
		if (classNode == null) {
			throw new IllegalArgumentException("Unable to determine class name for root node '" + rootNode + "'");
		}
		Colon2Node node = (Colon2Node) classNode.getCPath();
		return node.getName();
	}

	/**
	 * Find the first {@link ClassNode} under the supplied {@link Node}.
	 * @return the found <code>ClassNode</code>, or <code>null</code>
	 * if no {@link ClassNode} is found
	 */
	private static ClassNode findClassNode(Node node) {
		if (node instanceof ClassNode) {
			return (ClassNode) node;
		}
		List children = node.childNodes();
		for (int i = 0; i < children.size(); i++) {
			Node child = (Node) children.get(i);
			if (child instanceof ClassNode) {
				return (ClassNode) child;
			} else if (child instanceof NewlineNode) {
				NewlineNode nn = (NewlineNode) child;
				Node found = findClassNode(nn.getNextNode());
				if (found instanceof ClassNode) {
					return (ClassNode) found;
				}
			}
		}
		for (int i = 0; i < children.size(); i++) {
			Node child = (Node) children.get(i);
			Node found = findClassNode(child);
			if (found instanceof ClassNode) {
				return (ClassNode) found;
			}
		}
		return null;
	}


	/**
	 * InvocationHandler that invokes a JRuby script method.
	 */
	private static class RubyObjectInvocationHandler implements InvocationHandler {

		private final IRubyObject rubyObject;

		private final IRuby ruby;

		public RubyObjectInvocationHandler(IRubyObject rubyObject, IRuby ruby) {
			this.rubyObject = rubyObject;
			this.ruby = ruby;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (AopUtils.isToStringMethod(method)) {
				return "JRuby object [" + this.rubyObject + "]";
			}
			try {
				IRubyObject[] rubyArgs = convertToRuby(args);
				IRubyObject rubyResult =
						this.rubyObject.callMethod(this.ruby.getCurrentContext(), method.getName(), rubyArgs);
				return JavaEmbedUtils.rubyToJava(this.ruby, rubyResult, method.getReturnType());
			}
			catch (RaiseException ex) {
				throw new JRubyExecutionException(ex);
			}
		}

		private IRubyObject[] convertToRuby(Object[] javaArgs) {
			if (javaArgs == null || javaArgs.length == 0) {
				return new IRubyObject[0];
			}
			IRubyObject[] rubyArgs = new IRubyObject[javaArgs.length];
			for (int i = 0; i < javaArgs.length; ++i) {
				rubyArgs[i] = JavaEmbedUtils.javaToRuby(this.ruby, javaArgs[i]);
			}
			return rubyArgs;
		}
	}


	/**
	 * Exception thrown in response to a JRuby {@link RaiseException}
	 * being thrown from a JRuby method invocation.
	 * <p>Introduced because the <code>RaiseException</code> class does not
	 * have useful {@link Object#toString()}, {@link Throwable#getMessage()},
	 * and {@link Throwable#printStackTrace} implementations.
	 */
	public static class JRubyExecutionException extends NestedRuntimeException {

		/**
		 * Create a new <code>JRubyException</code>,
		 * wrapping the given JRuby <code>RaiseException</code>.
		 * @param exception the cause (must not be <code>null</code>)
		 */
		public JRubyExecutionException(RaiseException exception) {
			super(buildMessage(exception), exception);
		}

		private static String buildMessage(RaiseException exception) {
			RubyException rubyEx = exception.getException();
			return (rubyEx != null && rubyEx.message != null) ? rubyEx.message.toString() : "Unexpected JRuby error";
		}
	}

}
