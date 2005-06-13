/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.core.comparator;

import java.util.Comparator;

import org.springframework.util.Assert;

/**
 * Adapter adapting a comparable to the comparator interface.
 * 
 * @author Keith Donald
 */
public class ComparableComparator implements Comparator {

	private static final ComparableComparator INSTANCE = new ComparableComparator();

	/**
	 * Factory method that returns a shared instance of a ComparableComparator.
	 */
	public static Comparator instance() {
		return INSTANCE;
	}

	/**
	 * Factory method that returns a shared null safe instance of a ComparableComparator.
	 */
	public static Comparator nullSafeInstance() {
		return NullSafeComparator.instance();
	}

	private ComparableComparator() {
	}

	public int compare(Object o1, Object o2) {
		Assert.isTrue(o1 instanceof Comparable, "The first object provided is not Comparable");
		Assert.isTrue(o2 instanceof Comparable, "The second object provided is not Comparable");
		return ((Comparable)o1).compareTo(o2);
	}

}