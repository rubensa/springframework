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

package org.springframework.beans.factory.groovy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.script.AbstractScriptFactory;
import org.springframework.beans.factory.script.Script;

/**
 * Class containing static factory methods for wrapping
 * Groovy bean definitions.
 * These methods are intended for use with the Spring 1.1
 * IoC container's "factory-method" feature.
 * <p>The dynamicObject() methods return objects that can be cast to
 * DynamicScript to allow query and manipulation of the script status.
 * <p>
 * Groovy objects must have a no-arg constructor. You can use
 * Setter Injection on them, not Constructor or Method
 * Injection.
 * 
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14
 *          16:20:14 trisberg Exp $
 */
public class GroovyFactory extends AbstractScriptFactory {

	/**
	 * @see org.springframework.beans.factory.script.AbstractScriptFactory#createScript(java.lang.String, org.springframework.core.io.ResourceLoader)
	 */
	protected Script createScript(String location) throws BeansException {
		return new GroovyScript(location, this);
	}

}