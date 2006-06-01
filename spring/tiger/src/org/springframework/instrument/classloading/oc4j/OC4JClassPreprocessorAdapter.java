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
package org.springframework.instrument.classloading.oc4j;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import oracle.classloader.util.ClassPreprocessor;

/**
 * ClassPreprocessor adapter for OC4J. Many thanks to Mike Keith for assistance.
 * 
 * @author Costin Leau
 * 
 */
public class OC4JClassPreprocessorAdapter implements ClassPreprocessor {

	private ClassFileTransformer transformer;

	public OC4JClassPreprocessorAdapter(ClassFileTransformer transformer) {
		this.transformer = transformer;
	}

	public ClassPreprocessor initialize(ClassLoader loader) {
		return this;
	}

	public byte[] processClass(String className, byte origClassBytes[], int offset, int length, ProtectionDomain pd,
			ClassLoader loader) {
		try {
			byte[] result = transformer.transform(loader, className, null, pd, origClassBytes);
			return (result == null ? origClassBytes : result);
		}
		catch (IllegalClassFormatException ex) {
			throw new RuntimeException("Cannot transform", ex);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OC4JClassPreProcessorAdapter for:");
		builder.append(transformer);
		return builder.toString();
	}

}
