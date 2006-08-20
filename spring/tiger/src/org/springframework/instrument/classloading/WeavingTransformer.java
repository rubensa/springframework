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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassFileTransformer-based weaver, allowing for a list of transformers to be
 * applied on a class byte array. Normally used inside class loaders.
 *
 * <p>Note: This class is deliberately implemented for minimal external dependencies,
 * since it is included in weaver jars (to be deployed into application servers).
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
public class WeavingTransformer {

	private final ClassLoader classLoader;

	private final List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();


	/**
	 * Create a new WeavingTransformer for the current context class loader.
	 */
	public WeavingTransformer() {
		this.classLoader = getDefaultClassLoader();
	}

	/**
	 * Create a new WeavingTransformer for the given class loader.
	 * @param classLoader the ClassLoader to build a transformer for
	 */
	public WeavingTransformer(ClassLoader classLoader) {
		if (classLoader == null) {
			throw new IllegalArgumentException("ClassLoader must not be null");
		}
		this.classLoader = classLoader;
	}


	public void addTransformer(ClassFileTransformer transformer) {
		if (transformer == null) {
			throw new IllegalArgumentException("Transformer must not be null");
		}
		this.transformers.add(transformer);
	}

	public byte[] transformIfNecessary(String className, byte[] bytes) {
		String internalName = className.replace(".", "/");
		return transformIfNecessary(className, internalName, bytes, null);
	}

	public byte[] transformIfNecessary(String className, String internalName, byte[] bytes, ProtectionDomain pd) {
		byte[] result = bytes;
		for (ClassFileTransformer cft : this.transformers) {
			try {
				byte[] transformed = cft.transform(this.classLoader, internalName, null, pd, result);
				if (transformed != null) {
					result = transformed;
				}
			}
			catch (IllegalClassFormatException ex) {
				throw new IllegalStateException("Class file transformation failed", ex);
			}
		}
		return result;
	}


	/**
	 * See ClassUtils. We don't depend on that to avoid pulling in more of Spring.
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = WeavingTransformer.class.getClassLoader();
		}
		return cl;
	}

}
