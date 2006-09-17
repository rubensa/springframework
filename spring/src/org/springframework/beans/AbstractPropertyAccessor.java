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
		setPropertyValues(pvs, false, false);
	}

	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException {
		setPropertyValues(pvs, ignoreUnknown, false);
	}

	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException {

		List propertyAccessExceptions = new LinkedList();
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			try {
				// This method may throw any BeansException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions.
				setPropertyValue(pvArray[i]);
			}
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			}
			catch (NullValueInNestedPathException ex) {
				if (!ignoreInvalid) {
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
			PropertyAccessException[] paeArray = (PropertyAccessException[])
					propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
			throw new PropertyBatchUpdateException(paeArray);
		}
	}


	public Class getPropertyType(String propertyPath) {
		return null;
	}

	/**
	 * Actually get the value of a property.
	 * @param propertyName name of the property to get the value of
	 * @return the value of the property
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed
	 */
	public abstract Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * Actually set a property value.
	 * @param propertyName name of the property to set value of
	 * @param value the new value
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't writable
	 * @throws PropertyAccessException if the property was valid but the
	 * accessor method failed or a type mismatch occured
	 */
	public abstract void setPropertyValue(String propertyName, Object value) throws BeansException;

}
