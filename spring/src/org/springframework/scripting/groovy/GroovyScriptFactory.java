/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.scripting.groovy;

import java.io.IOException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.scripting.ScriptFactory} implementation
 * for a Groovy script.
 *
 * <p>Typically used in combination with a
 * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};
 * see the latter's javadoc} for a configuration example.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0
 * @see groovy.lang.GroovyClassLoader
 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor
 */
public class GroovyScriptFactory implements ScriptFactory, BeanClassLoaderAware {

	private final String scriptSourceLocator;
	
	private final GroovyObjectCustomizer groovyObjectCustomizer;

	private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(ClassUtils.getDefaultClassLoader());

	private Class scriptClass;

	private Class scriptResultClass;

	private final Object scriptClassMonitor = new Object();


	/**
	 * Create a new GroovyScriptFactory for the given script source.
	 * <p>We don't need to specify script interfaces here, since
	 * a Groovy script defines its Java interfaces itself.
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 */
	public GroovyScriptFactory(String scriptSourceLocator) {
		this(scriptSourceLocator, null);
	}

	/**
	 * Create a new GroovyScriptFactory for the given script source,
	 * specifying a strategy interface that can create a custom MetaClass
	 * to supply missing methods and otherwise change the behavior of the object.
	 * <p>We don't need to specify script interfaces here, since
	 * a Groovy script defines its Java interfaces itself.
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 * @param groovyObjectCustomizer a customizer that can set a custom metaclass
	 * or make other changes to the GroovyObject created by this factory
	 * (may be <code>null</code>)
	 */
	public GroovyScriptFactory(String scriptSourceLocator, GroovyObjectCustomizer groovyObjectCustomizer) {
		Assert.hasText(scriptSourceLocator, "'scriptSourceLocator' must not be empty");
		this.scriptSourceLocator = scriptSourceLocator;
		this.groovyObjectCustomizer = groovyObjectCustomizer;
	}


	public void setBeanClassLoader(ClassLoader classLoader) {
		this.groovyClassLoader = new GroovyClassLoader(classLoader);
	}

	/**
	 * Return the GroovyClassLoader used by this script factory.
	 */
	public GroovyClassLoader getGroovyClassLoader() {
		return this.groovyClassLoader;
	}


	public String getScriptSourceLocator() {
		return this.scriptSourceLocator;
	}

	/**
	 * Groovy scripts determine their interfaces themselves,
	 * hence we don't need to explicitly expose interfaces here.
	 * @return <code>null</code> always
	 */
	public Class[] getScriptInterfaces() {
		return null;
	}

	/**
	 * Groovy scripts do not need a config interface,
	 * since they expose their setters as public methods.
	 */
	public boolean requiresConfigInterface() {
		return false;
	}


	/**
	 * Loads and parses the Groovy script via the GroovyClassLoader.
	 * @see groovy.lang.GroovyClassLoader
	 */
	public Object getScriptedObject(ScriptSource scriptSource, Class[] actualInterfaces)
			throws IOException, ScriptCompilationException {

		try {
			Class scriptClassToExecute = null;

			synchronized (this.scriptClassMonitor) {
				if (this.scriptClass == null || scriptSource.isModified()) {
					this.scriptClass = getGroovyClassLoader().parseClass(scriptSource.getScriptAsString());

					if (Script.class.isAssignableFrom(this.scriptClass)) {
						// A Groovy script, probably creating an instance: let's execute it.
						Object result = executeScript(this.scriptClass);
						this.scriptResultClass = (result != null ? result.getClass() : null);
						return result;
					}
					else {
						this.scriptResultClass = this.scriptClass;
					}
				}
				scriptClassToExecute = this.scriptClass;
			}

			// Process re-execution outside of the synchronized block.
			return executeScript(scriptClassToExecute);
		}
		catch (CompilationFailedException ex) {
			throw new ScriptCompilationException("Could not compile Groovy script: " + scriptSource, ex);
		}
	}

	public Class getScriptedObjectType(ScriptSource scriptSource)
			throws IOException, ScriptCompilationException {

		try {
			synchronized (this.scriptClassMonitor) {
				if (this.scriptClass == null || scriptSource.isModified()) {
					this.scriptClass = getGroovyClassLoader().parseClass(scriptSource.getScriptAsString());

					if (Script.class.isAssignableFrom(this.scriptClass)) {
						// A Groovy script, probably creating an instance: let's execute it.
						Object result = executeScript(this.scriptClass);
						this.scriptResultClass = (result != null ? result.getClass() : null);
					}
					else {
						this.scriptResultClass = this.scriptClass;
					}
				}
				return this.scriptResultClass;
			}
		}
		catch (CompilationFailedException ex) {
			throw new ScriptCompilationException("Could not compile Groovy script: " + scriptSource, ex);
		}
	}

	/**
	 * Instantiate the given Groovy script class and run it if necessary.
	 * @param scriptClass the Groovy script class
	 * @return the result object (either an instance of the script class
	 * or the result of running the script instance)
	 * @throws ScriptCompilationException in case of instantiation failure
	 */
	protected Object executeScript(Class scriptClass) throws ScriptCompilationException {
		try {
			GroovyObject goo = (GroovyObject) scriptClass.newInstance();

			if (this.groovyObjectCustomizer != null) {
				// Allow metaclass and other customization.
				this.groovyObjectCustomizer.customize(goo);
			}

			if (goo instanceof Script) {
				// A Groovy script, probably creating an instance: let's execute it.
				return ((Script) goo).run();
			}
			else {
				// An instance of the scripted class: let's return it as-is.
				return goo;
			}
		}
		catch (InstantiationException ex) {
			throw new ScriptCompilationException(
					"Could not instantiate Groovy script class: " + scriptClass.getName(), ex);
		}
		catch (IllegalAccessException ex) {
			throw new ScriptCompilationException(
					"Could not access Groovy script constructor: " + scriptClass.getName(), ex);
		}
	}


	public String toString() {
		return "GroovyScriptFactory: script source locator [" + this.scriptSourceLocator + "]";
	}

}
