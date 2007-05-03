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

package org.springframework.instrument.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * ClassLoader decorator that shadows an enclosing ClassLoader, applying
 * registered transformers to all affected classes.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 * @see #addTransformer
 * @see #isClassNameExcludedFromShadowing(String)
 */
public class ShadowingClassLoader extends ClassLoader {

	/** Packages that are excluded by default */
	public static final String[] DEFAULT_EXCLUDED_PACKAGES = new String[] {"java.", "javax.", "sun.", "com.sun.",
			"org.w3c.", "org.xml.", "org.dom4j.", "org.aspectj.", "org.apache.xerces.", "org.apache.commons.logging."};


	private final ClassLoader enclosingClassLoader;

	private final List<ClassFileTransformer> classFileTransformers = new LinkedList<ClassFileTransformer>();

	private final Map<String, Class> classCache = new HashMap<String, Class>();


	/**
	 * Create a new ShadowingClassLoader, decorating the given ClassLoader.
	 * @param enclosingClassLoader the ClassLoader to decorate
	 */
	public ShadowingClassLoader(ClassLoader enclosingClassLoader) {
		Assert.notNull(enclosingClassLoader, "Enclosing ClassLoader must not be null");
		this.enclosingClassLoader = enclosingClassLoader;
	}


	/**
	 * Add the given ClassFileTransformer to the list of transformers that this
	 * ClassLoader will apply.
	 * @param transformer the ClassFileTransformer
	 */
	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		this.classFileTransformers.add(transformer);
	}

	/**
	 * Copy all ClassFileTransformers from the given ClassLoader to the list of
	 * transformers that this ClassLoader will apply.
	 * @param other the ClassLoader to copy from
	 */
	public void copyTransformers(ShadowingClassLoader other) {
		Assert.notNull(other, "Other ClassLoader must not be null");
		this.classFileTransformers.addAll(other.classFileTransformers);
	}


	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (shouldShadow(name)) {
			Class cls = this.classCache.get(name);
			if (cls != null) {
				return cls;
			}
			return doLoadClass(name);
		}
		else {
			return this.enclosingClassLoader.loadClass(name);
		}
	}

	/**
	 * Determine whether the given class should be excluded from shadowing.
	 * @param className the name of the class
	 * @return whether the specified class should be shadowed
	 */
	private boolean shouldShadow(String className) {
		return (!className.equals(getClass().getName()) && !className.endsWith("ShadowingClassLoader")
				&& !isExcludedPackage(className) && !isClassNameExcludedFromShadowing(className));
	}

	/**
	 * Determine whether the given class is defined in an excluded package.
	 * @param className the name of the class
	 * @return whether the specified package is excluded
	 */
	private boolean isExcludedPackage(String className) {
		for (int i = 0; i < DEFAULT_EXCLUDED_PACKAGES.length; i++) {
			if (className.startsWith(DEFAULT_EXCLUDED_PACKAGES[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Subclasses can override this method to specify whether or not a
	 * particular class should be excluded from shadowing.
	 * @param className the class name to test
	 * @return whether the specified class is excluded
	 */
	protected boolean isClassNameExcludedFromShadowing(String className) {
		return false;
	}


	private Class doLoadClass(String name) throws ClassNotFoundException {
		String internalName = StringUtils.replace(name, ".", "/") + ".class";
		InputStream is = this.enclosingClassLoader.getResourceAsStream(internalName);
		if (is == null) {
			throw new ClassNotFoundException(name);
		}
		try {
			byte[] bytes = FileCopyUtils.copyToByteArray(is);
			bytes = applyTransformers(name, bytes);
			Class cls = defineClass(name, bytes, 0, bytes.length);
			// Additional check for defining the package, if not defined yet.
			if (cls.getPackage() == null) {
				String packageName = name.substring(0, name.lastIndexOf('.'));
				definePackage(packageName, null, null, null, null, null, null, null);
			}
			this.classCache.put(name, cls);
			return cls;
		}
		catch (IOException ex) {
			throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
		}
	}

	private byte[] applyTransformers(String name, byte[] bytes) {
		String internalName = StringUtils.replace(name, ".", "/");
		try {
			for (ClassFileTransformer transformer : this.classFileTransformers) {
				byte[] transformed = transformer.transform(this, internalName, null, null, bytes);
				bytes = (transformed != null ? transformed : bytes);
			}
			return bytes;
		}
		catch (IllegalClassFormatException ex) {
			throw new IllegalStateException(ex);
		}
	}


	public URL getResource(String name) {
		return this.enclosingClassLoader.getResource(name);
	}

	public InputStream getResourceAsStream(String name) {
		return this.enclosingClassLoader.getResourceAsStream(name);
	}

	public Enumeration<URL> getResources(String name) throws IOException {
		return this.enclosingClassLoader.getResources(name);
	}

}
