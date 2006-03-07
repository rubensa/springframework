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

package org.springframework.beans;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of the ConfigurablePropertyAccessor interface.
 * Provides base implementations of all convenience methods, with the
 * implementation of actual property access left to subclasses
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getPropertyValue
 * @see #setPropertyValue
 */
public abstract class AbstractPropertyAccessor extends PropertyEditorRegistrySupport
		implements ConfigurablePropertyAccessor {

	private boolean extractOldValueForEditor = false;


	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}

	public boolean isExtractOldValueForEditor() {
		return extractOldValueForEditor;
	}


	public void setPropertyValue(PropertyValue pv) throws BeansException {
		setPropertyValue(pv.getName(), pv.getValue());
	}

	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false);
	}

	public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown) throws BeansException {
		List propertyAccessExceptions = new LinkedList();
		PropertyValue[] pvs = propertyValues.getPropertyValues();
		for (int i = 0; i < pvs.length; i++) {
			try {
				// This method may throw any BeansException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions.
				setPropertyValue(pvs[i]);
			}
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			}
			catch (PropertyAccessException ex) {
				propertyAccessExceptions.add(ex);
			}
		}

		// If we encountered individual exceptions, throw the composite exception.
		if (!propertyAccessExceptions.isEmpty()) {
			Object[] paeArray =
					propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
			throw new PropertyAccessExceptionsException((PropertyAccessException[]) paeArray);
		}
	}


	/**
	 * PropertyEditor
	 * @param propertyPath
	 * @return
	 */
	public Class getPropertyType(String propertyPath) {
		return null;
	}

	/**
	 * Actually get the value of a property.
	 * @param propertyName name of the property to get the value of
	 * @return the value of the property
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't readable, or if the property getter throws an exception.
	 */
	public abstract Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * Actually set a property value.
	 * @param propertyName name of the property to set value of
	 * @param value the new value
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 */
	public abstract void setPropertyValue(String propertyName, Object value) throws BeansException;

}
