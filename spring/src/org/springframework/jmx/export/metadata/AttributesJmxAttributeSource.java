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

package org.springframework.jmx.export.metadata;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.metadata.Attributes;

/**
 * Implementation of the <code>JmxAttributeSource</code> interface that
 * reads metadata via Spring's <code>Attributes</code> abstraction.
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.metadata.Attributes
 * @see org.springframework.metadata.commons.CommonsAttributes
 */
public class AttributesJmxAttributeSource implements JmxAttributeSource, InitializingBean {

	/**
	 * Underlying Attributes implementation that we're using.
	 */
	private Attributes attributes;


	/**
	 * Create a new AttributesJmxAttributeSource.
	 * @see #setAttributes
	 */
	public AttributesJmxAttributeSource() {
	}

	/**
	 * Create a new AttributesJmxAttributeSource.
	 * @param attributes the Attributes implementation to use
	 */
	public AttributesJmxAttributeSource(Attributes attributes) {
		if (attributes == null) {
			throw new IllegalArgumentException("Attributes is required");
		}
		this.attributes = attributes;
	}

	/**
	 * Set the Attributes implementation to use.
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public void afterPropertiesSet() {
		if (this.attributes == null) {
			throw new IllegalArgumentException("'attributes' is required");
		}
	}


	/**
	 * If the specified class has a <code>ManagedResource</code> attribute,
	 * then it is returned. Otherwise returns null.
	 * @param clazz the class to read the attribute data from
	 * @return the attribute, or null if not found
	 * @throws InvalidMetadataException if more than one attribute exists
	 */
	public ManagedResource getManagedResource(Class clazz) {
		Collection attrs = this.attributes.getAttributes(clazz, ManagedResource.class);
		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedResource) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Class can have only one ManagedResource attribute");
		}
	}

	/**
	 * If the specified method has a <code>ManagedAttribute</code> attribute,
	 * then it is returned. Otherwise returns null.
	 * @param method the method to read the attribute data from
	 * @return the attribute, or null if not found
	 * @throws InvalidMetadataException if more than one attribute exists,
	 * or if the supplied method does not represent a JavaBean property
	 */
	public ManagedAttribute getManagedAttribute(Method method) throws InvalidMetadataException {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd == null) {
			throw new InvalidMetadataException(
					"The ManagedAttribute attribute is only valid for JavaBean properties: " +
					"use ManagedOperation for methods");
		}
		Collection attrs = this.attributes.getAttributes(method, ManagedAttribute.class);
		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedAttribute) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
		}
	}

	/**
	 * If the specified method has a <code>ManagedOperation</code> attribute,
	 * then it is returned. Otherwise return null.
	 * @param method the method to read the attribute data from
	 * @return the attribute, or null if not found
	 * @throws InvalidMetadataException if more than one attribute exists,
	 * or if the supplied method represents a JavaBean property
	 */
	public ManagedOperation getManagedOperation(Method method) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			throw new InvalidMetadataException(
					"The ManagedOperation attribute is not valid for JavaBean properties: "+
					"use ManagedAttribute instead");
		}
		Collection attrs = this.attributes.getAttributes(method, ManagedOperation.class);
		if (attrs.isEmpty()) {
			return null;
		}
		else if (attrs.size() == 1) {
			return (ManagedOperation) attrs.iterator().next();
		}
		else {
			throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
		}
	}

}
