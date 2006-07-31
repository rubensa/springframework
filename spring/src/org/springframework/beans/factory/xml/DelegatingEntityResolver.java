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

package org.springframework.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.util.Assert;

/**
 * {@link EntityResolver} implementation that delegates to a {@link BeansDtdResolver}
 * and a {@link PluggableSchemaResolver} for DTDs and XML schemas, respectively.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see BeansDtdResolver
 * @see PluggableSchemaResolver
 */
public class DelegatingEntityResolver implements EntityResolver {

	/** Suffix for DTD files */
	public static final String DTD_SUFFIX = ".dtd";

	/** Suffix for schema definition files */
	public static final String XSD_SUFFIX = ".xsd";


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private final EntityResolver dtdResolver;

	private final EntityResolver schemaResolver;


	/**
	 * Create a new DelegatingEntityResolver that delegates to
	 * a default {@link BeansDtdResolver} and a default {@link PluggableSchemaResolver}.
	 * <p>Configures the {@link PluggableSchemaResolver} with the supplied
	 * {@link ClassLoader}.
	 * @param classLoader the ClassLoader to use for loading
	 * @throws IllegalArgumentException if the supplied class loader is <code>null</code> 
	 */
	public DelegatingEntityResolver(ClassLoader classLoader) {
		this.dtdResolver = new BeansDtdResolver();
		this.schemaResolver = new PluggableSchemaResolver(classLoader);
	}

	/**
	 * Create a new DelegatingEntityResolver that delegates to
	 * the given {@link EntityResolver EntityResolvers}.
	 * @param dtdResolver the EntityResolver to resolve DTDs with
	 * @param schemaResolver the EntityResolver to resolve XML schemas with
	 * @throws IllegalArgumentException if either of the supplied resolvers is <code>null</code>
	 */
	public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
		Assert.notNull(dtdResolver);
		Assert.notNull(schemaResolver);
		this.dtdResolver = dtdResolver;
		this.schemaResolver = schemaResolver;
	}


	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId != null) {
			if (systemId.endsWith(DTD_SUFFIX)) {
				return resolveEntity(publicId, systemId, this.dtdResolver, "DTD");
			}
			else if (systemId.endsWith(XSD_SUFFIX)) {
				return resolveEntity(publicId, systemId, this.schemaResolver, "XML Schema");
			}
		}
		return null;
	}


	private InputSource resolveEntity(String publicId, String systemId, EntityResolver resolver, String type) throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve " + type + " [" + systemId
					+ "] using [" + resolver.getClass().getName() + "]");
		}
		return resolver.resolveEntity(publicId, systemId);
	}

}
