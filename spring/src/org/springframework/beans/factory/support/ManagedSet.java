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

package org.springframework.beans.factory.support;

import java.util.HashSet;

/**
 * Tag subclass used to hold managed Set elements, which may
 * include runtime bean references.
 * @author Juergen Hoeller
 * @since 21.01.2004
 */
public class ManagedSet extends HashSet {

	public ManagedSet() {
	}

	public ManagedSet(int initialCapacity) {
		super(initialCapacity);
	}

}
