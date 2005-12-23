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

package org.springframework.aop.aspectj;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

/**
 * Implementation of AspectJ ProceedingJoinPoint interface
 * wrapping an AOP Alliance MethodInvocation.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint, StaticPart {
	
	private final MethodInvocation methodInvocation;

	/**
	 * Lazily initialized signature object.
	 */
	private Signature signature;
	
	/**
	 * Lazily initialized.
	 */
	private SourceLocation sourceLocation;


	public MethodInvocationProceedingJoinPoint(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
	}

	public void set$AroundClosure(AroundClosure aroundClosure) {
		throw new UnsupportedOperationException();
	}

	public Object proceed() throws Throwable {
		return methodInvocation.proceed();
	}

	public Object proceed(Object[] args) throws Throwable {
		Object[] oldArgs = methodInvocation.getArguments();
		for (int i = 0; i < oldArgs.length; i++) {
			oldArgs[i] = args[i];
		}
		return methodInvocation.proceed();
	}

	public String toShortString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public String toLongString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public Object getThis() {
		return methodInvocation.getThis();
	}

	public Object getTarget() {
		return methodInvocation.getThis();
	}

	public Object[] getArgs() {
		return methodInvocation.getArguments();
	}

	public synchronized Signature getSignature() {
		if (signature == null) {
			signature = new MethodSignatureImpl();
		}
		return signature;
	}

	public synchronized SourceLocation getSourceLocation() {
		if (sourceLocation == null) {
			sourceLocation = new SourceLocationImpl();
		}
		return sourceLocation;
	}

	public String getKind() {
		return ProceedingJoinPoint.METHOD_EXECUTION;
	}

	public StaticPart getStaticPart() {
		return this;
	}
	
	
	/**
	 * Lazily initialzed MethodSignature.
	 */
	private class MethodSignatureImpl implements Signature, MethodSignature {

		public String toShortString() {
			return methodInvocation.getMethod().getName();
		}

		public String toLongString() {
			return methodInvocation.getMethod().toString();
		}

		public String getName() {
			return methodInvocation.getMethod().getName();
		}

		public int getModifiers() {
			return methodInvocation.getMethod().getModifiers();
		}

		public Class getDeclaringType() {
			return methodInvocation.getMethod().getDeclaringClass();
		}

		public String getDeclaringTypeName() {
			return methodInvocation.getMethod().getDeclaringClass().getName();
		}

		public Class getReturnType() {
			return methodInvocation.getMethod().getReturnType();
		}

		public Method getMethod() {
			return methodInvocation.getMethod();
		}

		public Class[] getParameterTypes() {
			return methodInvocation.getMethod().getParameterTypes();
		}

		public String[] getParameterNames() {
			// TODO consider allowing use of ParameterNameDiscoverer, or tying into
			// parameter names exposed for argument binding
			throw new UnsupportedOperationException("Parameter names cannot be determined unless compiled by AspectJ compiler");
		}

		public Class[] getExceptionTypes() {
			return methodInvocation.getMethod().getExceptionTypes();
		}
	}
	
	private class SourceLocationImpl implements SourceLocation {

		public Class getWithinType() {
			if (methodInvocation.getThis() == null) {
				throw new UnsupportedOperationException("No source location joinpoint available: target is null");
			}
			return methodInvocation.getThis().getClass();
		}

		public String getFileName() {
			throw new UnsupportedOperationException();
		}

		public int getLine() {
			throw new UnsupportedOperationException();
		}

		public int getColumn() {
			throw new UnsupportedOperationException();
		}
	}

}
