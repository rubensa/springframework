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
package org.springframework.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Implementation of ParameterNameDiscover that uses the 
 * LocalVariableTable information in the method attributes
 * to discover parameter names.
 * 
 * Returns null if the class file was compiled without debug
 * information.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class LocalVariableTableParameterNameDiscover implements
		ParameterNameDiscoverer {

	/** Logger available to subclasses */
	private final Log logger = LogFactory.getLog(getClass());

	/* (non-Javadoc)
	 * @see org.springframework.core.ParameterNameDiscoverer#getParameterNames(java.lang.reflect.Method, java.lang.Class)
	 */
	public String[] getParameterNames(Method m, Class clazz) {
		ParameterNameDiscoveringVisitor visitor = null;
		try {
			visitor = visitMethod(m);
			if (visitor.foundTargetMember()) {
				return visitor.getParameterNames();
			} 
			else {
				return null;
			}
		} 
		catch (IOException ex) {
			// we couldn't load the class file, which is not
			// fatal as it simply means this method of discovering
			// parameter names won't work.
			if (logger.isInfoEnabled()) {
				logger.info("IOException whilst attempting to read .class file for class " +
						m.getDeclaringClass().getName() + 
						" - unable to determine parameter names for method " +
						m.getName(),ex);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.core.ParameterNameDiscoverer#getParameterNames(java.lang.reflect.Constructor)
	 */
	public String[] getParameterNames(Constructor ctor) {
		ParameterNameDiscoveringVisitor visitor = null;
		try {
			visitor = visitConstructor(ctor);
			if (visitor.foundTargetMember()) {
				return visitor.getParameterNames();
			} 
			else {
				return null;
			}
		} 
		catch (IOException ex) {
			// we couldn't load the class file, which is not
			// fatal as it simply means this method of discovering
			// parameter names won't work.
			if (logger.isInfoEnabled()) {
				logger.info("IOException whilst attempting to read .class file for class " +
						ctor.getDeclaringClass().getName() + 
						" - unable to determine parameter names for constructor",ex);
			}
		}
		return null;
	}

	/**
	 * visit the given method and discover its parameter names
	 * @param m
	 * @return
	 * @throws IOException
	 */
	private ParameterNameDiscoveringVisitor visitMethod(Method m) throws IOException {
		ClassReader reader = new ClassReader(m.getDeclaringClass().getName());
		FindMethodParamNamesClassVisitor classVisitor = new FindMethodParamNamesClassVisitor(m);
		reader.accept(classVisitor,false);
		return classVisitor;
	}

	/**
	 * visit the given constructor and discover its parameter names
	 * @param c
	 * @return
	 * @throws IOException
	 */
	private ParameterNameDiscoveringVisitor visitConstructor(Constructor c) throws IOException {
		ClassReader reader = new ClassReader(c.getDeclaringClass().getName());
		FindConstructorParamNamesClassVisitor classVisitor = new FindConstructorParamNamesClassVisitor(c);
		reader.accept(classVisitor,false);
		return classVisitor;
	}

	/**
	 * Helper class that looks for a given member name and descriptor, and then
	 * attempts to find the parameter names for that member.
	 * @author Adrian
	 *
	 */
	private static abstract class ParameterNameDiscoveringVisitor extends EmptyVisitor {
		private String methodNameToMatch;
		private String descriptorToMatch;
		private int numParamsExpected;
		private boolean foundTargetMember = false;
		private String[] parameterNames;
		
		public ParameterNameDiscoveringVisitor(String name,int numParams) {
			this.methodNameToMatch = name;
			this.numParamsExpected = numParams;
		}
		
		public void setDescriptorToMatch(String descriptor) {
			this.descriptorToMatch = descriptor;			
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (name.equals(this.methodNameToMatch) &&
				desc.equals(this.descriptorToMatch)) {
				this.foundTargetMember = true;
				return new LocalVariableTableVisitor(this,this.numParamsExpected);	
			} 
			else {
				// not interested in this method...
				return null;
			}
		}

		public boolean foundTargetMember() {
			return this.foundTargetMember;
		}
		
		public String[] getParameterNames() {
			if (!foundTargetMember()) {
				throw new IllegalStateException("Can't ask for parameter names when target member has not been found");
			}
			
			return this.parameterNames;
		}
		
		public void setParameterNames(String[] names) {
			this.parameterNames = names;
		}
	}
	
	private static class FindMethodParamNamesClassVisitor extends ParameterNameDiscoveringVisitor {
		
		public FindMethodParamNamesClassVisitor(Method method) {
			super(method.getName(),method.getParameterTypes().length);
			setDescriptorToMatch(Type.getMethodDescriptor(method));
		}
	}
	
	private static class FindConstructorParamNamesClassVisitor extends ParameterNameDiscoveringVisitor {
		
		public FindConstructorParamNamesClassVisitor(Constructor cons) {
			super("<init>",cons.getParameterTypes().length);
			Type[] pTypes = new Type[cons.getParameterTypes().length];
			for (int i = 0; i < pTypes.length; i++) {
				pTypes[i] = Type.getType(cons.getParameterTypes()[i]);
			}
			setDescriptorToMatch(Type.getMethodDescriptor(Type.VOID_TYPE,pTypes));
		}
	}
	
	private static class LocalVariableTableVisitor extends EmptyVisitor {
		private ParameterNameDiscoveringVisitor memberVisitor;
		private int numParameters;
		private String[] parameterNames;
		private boolean hasLVTInfo = false;
		
		public LocalVariableTableVisitor(ParameterNameDiscoveringVisitor memberVisitor, int numParams) {
			this.numParameters = numParams;
			this.parameterNames = new String[this.numParameters];
			this.memberVisitor = memberVisitor;
		}
		
		public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
			this.hasLVTInfo = true;
			if ( (index > 0) && (index <= this.numParameters)) {
				this.parameterNames[index-1] = name;
			}
		}
		
		public void visitEnd() {
			if (this.hasLVTInfo) {
				this.memberVisitor.setParameterNames(this.parameterNames);
			}
		}
	}
}
