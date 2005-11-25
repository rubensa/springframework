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
package org.springframework.webflow.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Base class for attribute map adapters whose keys are String values. Concrete`
 * classses need only implement the abstract hook method defined by this class.
 * 
 * @author Keith Donald
 */
public abstract class StringKeyedAttributeMapAdapter implements Map {
	
	private Set keySet;

	private Collection values;

	private Set entrySet;

	public void clear() {
		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			removeAttribute((String)e.nextElement());
		}
	}

	public boolean containsKey(Object key) {
		return getAttribute(key.toString()) != null;
	}

	public boolean containsValue(Object value) {
		if (value == null) {
			return false;
		}
		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			Object aValue = getAttribute((String)e.nextElement());
			if (value.equals(aValue)) {
				return true;
			}
		}
		return false;
	}

	public Set entrySet() {
		return (entrySet != null) ? entrySet : (entrySet = new EntrySet());
	}

	public Object get(Object key) {
		return getAttribute(key.toString());
	}

	public boolean isEmpty() {
		return !getAttributeNames().hasMoreElements();
	}

	public Set keySet() {
		return (keySet != null) ? keySet : (keySet = new KeySet());
	}

	public Object put(Object key, Object value) {
		String stringKey = String.valueOf(key);
		Object previousValue = getAttribute(stringKey);
		setAttribute(stringKey, value);
		return previousValue;
	}

	public void putAll(Map map) {
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry)it.next();
			setAttribute(entry.getKey().toString(), entry.getValue());
		}
	}

	public Object remove(Object key) {
		String stringKey = key.toString();
		Object retval = getAttribute(stringKey);
		removeAttribute(stringKey);
		return retval;
	}

	public int size() {
		int size = 0;
		for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
			size++;
			e.nextElement();
		}
		return size;
	}

	public Collection values() {
		return (values != null) ? values : (values = new Values());
	}

	/**
	 * Hook method that needs to be implemented by concrete subclasses.
	 * Gets a value associated with a key.
	 * @param key the key to lookup
	 * @return the associated value, or null if none
	 */
	abstract protected Object getAttribute(String key);

	/**
	 * Hook method that needs to be implemented by concrete subclasses.
	 * Puts a key-value pair in the map, overwriting any possible earlier
	 * value associated with the same key.
	 * @param key the key to associate the value with
	 * @param value the value to associate with the key
	 */
	abstract protected void setAttribute(String key, Object value);

	/**
	 * Hook method that needs to be implemented by concrete subclasses.
	 * Removes a key and its associated value from the map.
	 * @param key the key to remove
	 */
	abstract protected void removeAttribute(String key);

	/**
	 * Hook method that needs to be implemented by concrete subclasses.
	 * Returns an enumeration listing all keys known to the map.
	 * @return the key enumeration
	 */
	abstract protected Enumeration getAttributeNames();
	
	//internal helper classes -- is there no easier way to do this?

	private abstract class AbstractSet extends java.util.AbstractSet {
		public boolean isEmpty() {
			return StringKeyedAttributeMapAdapter.this.isEmpty();
		}

		public int size() {
			return StringKeyedAttributeMapAdapter.this.size();
		}

		public void clear() {
			StringKeyedAttributeMapAdapter.this.clear();
		}
	}

	private class KeySet extends AbstractSet {
		public Iterator iterator() {
			return new KeyIterator();
		}

		public boolean contains(Object o) {
			return StringKeyedAttributeMapAdapter.this.containsKey(o);
		}

		public boolean remove(Object o) {
			return StringKeyedAttributeMapAdapter.this.remove(o) != null;
		}

	}

	private class KeyIterator implements Iterator {
		protected final Enumeration enumeration = getAttributeNames();

		protected Object currentKey;

		public void remove() {
			if (currentKey == null) {
				throw new NoSuchElementException("You must call next() at least once");
			}
			StringKeyedAttributeMapAdapter.this.remove(currentKey);
		}

		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		public Object next() {
			return currentKey = enumeration.nextElement();
		}
	}

	private class Values extends AbstractSet {
		public Iterator iterator() {
			return new ValuesIterator();
		}

		public boolean contains(Object o) {
			return StringKeyedAttributeMapAdapter.this.containsValue(o);
		}

		public boolean remove(Object o) {
			if (o == null) {
				return false;
			}
			for (Iterator it = iterator(); it.hasNext();) {
				if (o.equals(it.next())) {
					it.remove();
					return true;
				}
			}
			return false;
		}
	}

	private class ValuesIterator extends KeyIterator {
		public Object next() {
			super.next();
			return StringKeyedAttributeMapAdapter.this.get(currentKey);
		}
	}

	private class EntrySet extends AbstractSet {
		public Iterator iterator() {
			return new EntryIterator();
		}

		public boolean contains(Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			Entry entry = (Entry)o;
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key == null || value == null) {
				return false;
			}
			return value.equals(StringKeyedAttributeMapAdapter.this.get(key));
		}

		public boolean remove(Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			Entry entry = (Entry)o;
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key == null || value == null || !value.equals(StringKeyedAttributeMapAdapter.this.get(key))) {
				return false;
			}
			return StringKeyedAttributeMapAdapter.this.remove(((Entry)o).getKey()) != null;
		}
	}

	private class EntryIterator extends KeyIterator {
		public Object next() {
			super.next();
			return new EntrySetEntry(currentKey);
		}
	}

	private class EntrySetEntry implements Entry {
		private final Object currentKey;

		public EntrySetEntry(Object currentKey) {
			this.currentKey = currentKey;
		}

		public Object getKey() {
			return currentKey;
		}

		public Object getValue() {
			return StringKeyedAttributeMapAdapter.this.get(currentKey);
		}

		public Object setValue(Object value) {
			return StringKeyedAttributeMapAdapter.this.put(currentKey, value);
		}
	}
}