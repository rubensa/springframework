/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.support;

import java.util.Properties;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * <p>
 * Concrete implementation of {@link AbstractGenericContextLoader} which reads
 * bean definitions from Java {@link Properties} resources.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
public class GenericPropertiesContextLoader extends AbstractGenericContextLoader {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Creates a new {@link PropertiesBeanDefinitionReader}.
	 * </p>
	 *
	 * @see org.springframework.test.context.support.AbstractGenericContextLoader#createBeanDefinitionReader(org.springframework.context.support.GenericApplicationContext)
	 * @see PropertiesBeanDefinitionReader
	 * @return a new PropertiesBeanDefinitionReader.
	 */
	@Override
	protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {

		return new PropertiesBeanDefinitionReader(context);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Returns &quot;<code>-context.properties</code>&quot;.
	 *
	 * @see org.springframework.test.context.support.AbstractContextLoader#getResourceSuffix()
	 */
	@Override
	public String getResourceSuffix() {

		return "-context.properties";
	}

	// ------------------------------------------------------------------------|

}
