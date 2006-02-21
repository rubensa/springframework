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
package org.springframework.binding.util;

import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A simple decorator for getting attributes out of a map. May be instantiated
 * directly or used as a base class as a convenience.
 * 
 * @author Keith Donald
 */
public class MapAccessor {

	/**
	 * The target map.
	 */
	private Map map;

	/**
	 * Creates a new attribute map accessor.
	 * @param map the map
	 */
	public MapAccessor(Map map) {
		Assert.notNull(map, "The map to decorate is required");
		this.map = map;
	}

	/**
	 * Returns the wrapped target map.
	 * @return the map
	 */
	public Map getMap() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Returns a value in the map, returning the defaultValue if no value was
	 * found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the attribute value
	 */
	public Object get(Object key, Object defaultValue) {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return map.get(key);
	}

	/**
	 * Returns a value in the map, asserting it is of the required type if
	 * present and returning <code>null</code> if not found.
	 * @param key the key
	 * @param requiredType the required type
	 * @return the value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not of the required type
	 */
	public Object get(Object key, Class requiredType) throws IllegalArgumentException {
		return get(key, requiredType, null);
	}

	/**
	 * Returns a value in the map of the specified type, returning the
	 * defaultValue if no value is found.
	 * @param key the key
	 * @param requiredType the required type
	 * @param defaultValue the default
	 * @return the attribute value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not of the required type
	 */
	public Object get(Object key, Class requiredType, Object defaultValue) {
		if (!map.containsKey(key)) {
			return map.get(key);
		}
		return assertValueOfType(key, requiredType);
	}

	/**
	 * Returns a value in the map, throwing an exception if the attribute is not
	 * present and of the correct type.
	 * @param key the key
	 * @return the value
	 */
	public Object getRequired(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return map.get(key);
	}

	/**
	 * Returns an value in the map, asserting it is present and of the required
	 * type.
	 * @param key the key
	 * @param requiredType the required type
	 * @return the value
	 */
	public Object getRequired(Object key, Class requiredType) throws IllegalArgumentException {
		assertContainsKey(key);
		return assertValueOfType(key, requiredType);
	}

	/**
	 * Returns a string value in the map, returning <code>null</code> if no
	 * value was found.
	 * @param key the key
	 * @return the string value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a string
	 */
	public String getString(Object key) throws IllegalArgumentException {
		return getString(key, null);
	}

	/**
	 * Returns a string value in the map, returning the defaultValue if no value
	 * was found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the string value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a string
	 */
	public String getString(Object key, String defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return (String)assertValueOfType(key, String.class);
	}

	/**
	 * Returns a string value in the map, throwing an exception if the attribute
	 * is not present and of the correct type.
	 * @param key the key
	 * @return the string value
	 * @throws IllegalArgumentException if the key is not present or present but
	 * the value is not a string
	 */
	public String getRequiredString(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return (String)assertValueOfType(key, String.class);
	}

	/**
	 * Returns a number value in the map that is of the specified type,
	 * returning <code>null</code> if no value was found.
	 * @param key the key
	 * @param requiredType the required number type
	 * @return the numbervalue
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a number of the required type
	 */
	public Number getNumber(Object key, Class requiredType) throws IllegalArgumentException {
		return getNumber(key, requiredType, null);
	}

	/**
	 * Returns a number attribute value in the map of the specified type,
	 * returning the defaultValue if no value was found.
	 * @param key the attribute name
	 * @return the number value
	 * @param defaultValue the default
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a number of the required type
	 */
	public Number getNumber(Object key, Class requiredType, Number defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return (Number)assertValueOfType(key, requiredType);
	}

	/**
	 * Returns a number value in the map, throwing an exception if the attribute
	 * is not present and of the correct type.
	 * @param key the key
	 * @return the number value
	 * @throws IllegalArgumentException if the key is not present or present but
	 * the value is not a number of the required type
	 */
	public Number getRequiredNumber(Object key, Class requiredType) throws IllegalArgumentException {
		assertContainsKey(key);
		return (Number)assertValueOfType(key, requiredType);
	}

	/**
	 * Returns an integer value in the map, returning <code>null</code> if no
	 * value was found.
	 * @param key the key
	 * @return the integer value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not an integer
	 */
	public Integer getInteger(Object key) throws IllegalArgumentException {
		return getInteger(key, null);
	}

	/**
	 * Returns an integer value in the map, returning the defaultValue if no
	 * value was found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the integer value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not an integer
	 */
	public Integer getInteger(Object key, Integer defaultValue) throws IllegalArgumentException {
		return (Integer)getNumber(key, Integer.class, defaultValue);
	}

	/**
	 * Returns an integer value in the map, throwing an exception if the value
	 * is not present and of the correct type.
	 * @param key the attribute name
	 * @return the integer attribute value
	 * @throws IllegalArgumentException if the key is not present or present but
	 * the value is not an integer
	 */
	public Integer getRequiredInteger(Object key) throws IllegalArgumentException {
		return (Integer)getRequiredNumber(key, Integer.class);
	}

	/**
	 * Returns a long value in the map, returning <code>null</code> if no
	 * value was found.
	 * @param key the key
	 * @return the long value
	 * @throws IllegalArgumentException if the key is present but not a long
	 */
	public Long getLong(Object key) throws IllegalArgumentException {
		return getLong(key, null);
	}

	/**
	 * Returns a long value in the map, returning the defaultValue if no value
	 * was found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a long
	 */
	public Long getLong(Object key, Long defaultValue) throws IllegalArgumentException {
		return (Long)getNumber(key, Long.class, defaultValue);
	}

	/**
	 * Returns a long value in the map, throwing an exception if the value is
	 * not present and of the correct type.
	 * @param key the key
	 * @return the long attribute value
	 * @throws IllegalArgumentException if the key is not present or present but
	 * the value is not a long
	 */
	public Long getRequiredLong(Object key) throws IllegalArgumentException {
		return (Long)getRequiredNumber(key, Long.class);
	}

	/**
	 * Returns a boolean value in the map, returning <code>null</code> if no
	 * value was found.
	 * @param key the key
	 * @return the boolean value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a boolean
	 */
	public Boolean getBoolean(Object key) throws IllegalArgumentException {
		return getBoolean(key, null);
	}

	/**
	 * Returns a boolean value in the map, returning the defaultValue if no
	 * value was found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the boolean value
	 * @throws IllegalArgumentException if the key is present but the value is
	 * not a boolean
	 */
	public Boolean getBoolean(Object key, Boolean defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return (Boolean)assertValueOfType(key, Boolean.class);
	}

	/**
	 * Returns a boolean value in the map, throwing an exception if the value is
	 * not present and of the correct type.
	 * @param key the attribute
	 * @return the boolean value
	 * @throws IllegalArgumentException if the key is not present or present but
	 * the value is not a boolean
	 */
	public Boolean getRequiredBoolean(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return (Boolean)assertValueOfType(key, Boolean.class);
	}

	/**
	 * Asserts that the attribute is present in the attribute map.
	 * @param key the key
	 * @throws IllegalArgumentException if the key is not present
	 */
	public void assertContainsKey(Object key) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("Required attribute '" + key
					+ "' is not present in map; attributes present are [" + getMap() + "]");
		}
	}

	/**
	 * Indicates if the attribute is present in the attribute map and of the
	 * required type.
	 * @param key the attribute name
	 * @return true if present and of the required type, false if not present.
	 */
	public boolean containsKey(Object key, Class requiredType) throws IllegalArgumentException {
		if (map.containsKey(key)) {
			assertValueOfType(key, requiredType);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Assert that the type of the given attribute value is of the required
	 * type.
	 * @param key the attribute name
	 * @param type the required type
	 * @return the attribute value
	 */
	public Object assertValueOfType(Object key, Class requiredType) {
		Object value = map.get(key);
		Assert.notNull(requiredType, "The required type to assert is required");
		if (!requiredType.isInstance(value)) {
			throw new IllegalArgumentException("Map key '" + key + "' has value [" + value
					+ "] that is not of expected type [" + requiredType + "], instead it is of type ["
					+ value.getClass() + "]");
		}
		return value;
	}
}