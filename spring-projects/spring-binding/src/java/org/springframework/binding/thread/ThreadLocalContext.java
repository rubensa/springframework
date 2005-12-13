/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.thread;

/**
 * Service interface for access to thread local storage.
 * @author Keith Donald
 */
public interface ThreadLocalContext {

	/**
	 * Get an object from thread local storage
	 * @param key the object's key
	 * @return the thread local
	 */
	public Object get(Object key);

	/**
	 * Put an object into thread local storage
	 * @param key the object's key
	 * @param value the object
	 * @return the object
	 */
	public Object put(Object key, Object value);

	/**
	 * Remove all objects from storage.
	 */
	public void clear();
}
