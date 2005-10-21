/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.MutableAttributeSource;

/**
 * Superclass of all objects in the web flow system that support annotation
 * using arbitrary properties. Mainly used to ensure consistent configuration of
 * properties for all annotated objects.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public abstract class AnnotatedObject implements MutableAttributeSource {

	/**
	 * Additional properties further describing this object.
	 */
	private Map properties = new HashMap();

	/**
	 * Returns the additional properties describing this object in an
	 * unmodifiable map.
	 */
	public Map getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Set the additional properties describing this object.
	 */
	public void setProperties(Map properties) {
		if (properties != null) {
			this.properties = new HashMap(properties);
		}
	}

	/**
	 * Returns the value of given property, or <code>null</code> if not found.
	 */
	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}

	/**
	 * Set the value of named property.
	 * @param propertyName the name of the property
	 * @param value the value to set
	 * @return previous value associated with specified name
	 */
	public Object setProperty(String propertyName, Object value) {
		return this.properties.put(propertyName, value);
	}

	/**
	 * Returns whether or not this annotated object contains a property with
	 * specified name
	 * @param propertyName the name of the property
	 * @return true if the property is set, false otherwise
	 */
	public boolean containsProperty(String propertyName) {
		return this.properties.containsKey(propertyName);
	}

	/**
	 * Returns a string property value.
	 * @param propertyName the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public String getStringProperty(String propertyName, String defaultValue) {
		if (containsProperty(propertyName)) {
			return (String)getProperty(propertyName);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Returns a integer property value.
	 * @param propertyName the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public int getIntProperty(String propertyName, int defaultValue) {
		if (containsProperty(propertyName)) {
			return ((Integer)getProperty(propertyName)).intValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Returns a boolean property value.
	 * @param propertyName the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
		if (containsProperty(propertyName)) {
			return ((Boolean)getProperty(propertyName)).booleanValue();
		}
		else {
			return defaultValue;
		}
	}

	// implementing MutableAttributeSource

	public boolean containsAttribute(String attributeName) {
		return containsProperty(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return getProperty(attributeName);
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		return setProperty(attributeName, attributeValue);
	}

	public Object removeAttribute(String attributeName) {
		return this.properties.remove(attributeName);
	}
}