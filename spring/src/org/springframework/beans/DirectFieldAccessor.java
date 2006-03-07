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

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * PropertyAccessor implementation that directly accesses instance fields.
 * Allows for direct binding to fields instead of going through JavaBean setters.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapper
 * @see org.springframework.validation.DirectFieldBindingResult
 * @see org.springframework.validation.DataBinder#initDirectFieldAccess()
 */
public class DirectFieldAccessor extends AbstractPropertyAccessor {

	private final Object target;

	private final Map fieldMap = new HashMap();

	private final PropertyTypeConverter propertyTypeConverter;


	/**
	 * Create a new DirectFieldAccessor for the given target object.
	 * @param target the target object to access
	 */
	public DirectFieldAccessor(Object target) {
		Assert.notNull(target, "Target object must not be null");
		this.target = target;
		ReflectionUtils.doWithFields(this.target.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) {
				fieldMap.put(field.getName(), field);
			}
		});
		this.propertyTypeConverter = new PropertyTypeConverter(this, target);
		setExtractOldValueForEditor(true);
	}


	public boolean isReadableProperty(String propertyName) throws BeansException {
		return this.fieldMap.containsKey(propertyName);
	}

	public boolean isWritableProperty(String propertyName) throws BeansException {
		return this.fieldMap.containsKey(propertyName);
	}

	public Class getPropertyType(String propertyName) throws BeansException {
		Field field = (Field) this.fieldMap.get(propertyName);
		if (field != null) {
			return field.getType();
		}
		return null;
	}

	public Object getPropertyValue(String propertyName) throws BeansException {
		Field field = (Field) this.fieldMap.get(propertyName);
		if (field == null) {
			throw new NotReadablePropertyException(
					this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
		}
		try {
			ReflectionUtils.makeAccessible(field);
			return field.get(this.target);
		}
		catch (IllegalAccessException ex) {
			throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", ex);
		}
	}

	public void setPropertyValue(String propertyName, Object newValue) throws BeansException {
		Field field = (Field) this.fieldMap.get(propertyName);
		if (field == null) {
			throw new NotWritablePropertyException(
					this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
		}
		Object oldValue = null;
		try {
			ReflectionUtils.makeAccessible(field);
			oldValue = field.get(this.target);
			Object convertedValue =
					this.propertyTypeConverter.convertIfNecessary(propertyName, oldValue, newValue, field.getType());
			field.set(this.target, convertedValue);
		}
		catch (IllegalAccessException ex) {
			throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", ex);
		}
		catch (IllegalArgumentException ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, field.getType(), ex);
		}
	}

}
