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
package org.springframework.rules.constraint;

import java.io.Serializable;
import java.util.Comparator;

import org.springframework.rules.Constraint;
import org.springframework.rules.factory.Constraints;
import org.springframework.util.Assert;
import org.springframework.util.ToStringBuilder;

/**
 * A range whose edges are defined by a minimum Comparable and a maximum
 * Comparable.
 *
 * @author Keith Donald
 */
public final class Range implements Serializable, Constraint {

	private Object min;

	private Object max;

	private boolean inclusive = true;

	private Constraint rangeConstraint;

	/**
	 * Creates a range with the specified <code>Comparable</code> min and max
	 * edges.
	 *
	 * @param min
	 *            the low edge of the range
	 * @param max
	 *            the high edge of the range
	 */
	public Range(Comparable min, Comparable max) {
		this(min, max, true);
	}

	/**
	 * Creates a range with the specified <code>Comparable</code> min and max
	 * edges.
	 *
	 * @param min
	 *            the low edge of the range
	 * @param max
	 *            the high edge of the range
	 * @param inclusive
	 *            the range is inclusive?
	 */
	public Range(Comparable min, Comparable max, boolean inclusive) {
		commonAssert(min, max);
		Constraints c = Constraints.instance();
		Constraint minimum;
		Constraint maximum;
		this.inclusive = inclusive;
		if (this.inclusive) {
			Assert.isTrue(LessThanEqualTo.instance().test(min, max), "Minimum "
					+ min + " must be less than or equal to maximum " + max);
			minimum = c.bind(GreaterThanEqualTo.instance(), min);
			maximum = c.bind(LessThanEqualTo.instance(), max);
		}
		else {
			Assert.isTrue(LessThan.instance().test(min, max), "Minimum " + min
					+ " must be less than maximum " + max);
			minimum = c.bind(GreaterThan.instance(), min);
			maximum = c.bind(LessThan.instance(), max);
		}
		this.rangeConstraint = c.and(minimum, maximum);
		this.min = min;
		this.max = max;
	}

	/**
	 * Creates a range with the specified min and max edges.
	 *
	 * @param min
	 *            the low edge of the range
	 * @param max
	 *            the high edge of the range
	 * @param comparator
	 *            the comparator to use to perform value comparisons
	 */
	public Range(Object min, Object max, Comparator comparator) {
		this(min, max, comparator, true);
	}

	/**
	 * Creates a range with the specified min and max edges.
	 *
	 * @param min
	 *            the low edge of the range
	 * @param max
	 *            the high edge of the range
	 * @param comparator
	 *            the comparator to use to perform value comparisons
	 */
	public Range(Object min, Object max, Comparator comparator,
			boolean inclusive) {
		commonAssert(min, max);
		Constraints c = Constraints.instance();
		Constraint minimum;
		Constraint maximum;
		this.inclusive = inclusive;
		if (this.inclusive) {
			Assert.isTrue(LessThanEqualTo.instance(comparator).test(min, max),
					"Minimum " + min
					+ " must be less than or equal to maximum " + max);
			minimum = c.bind(GreaterThanEqualTo.instance(comparator), min);
			maximum = c.bind(LessThanEqualTo.instance(comparator), max);
		}
		else {
			Assert.isTrue(LessThan.instance(comparator).test(min, max),
					"Minimum " + min + " must be less than maximum " + max);
			minimum = c.bind(GreaterThan.instance(comparator), min);
			maximum = c.bind(LessThan.instance(comparator), max);
		}
		this.rangeConstraint = c.and(minimum, maximum);
		this.min = min;
		this.max = max;
	}

	private void commonAssert(Object min, Object max) {
		Assert.isTrue(min != null && max != null, "Both min and max are required");
		Assert.isTrue(min.getClass() == max.getClass(), "Min needs to be of same type as max");
	}

	/**
	 * Convenience constructor for <code>int</code> ranges.
	 */
	public Range(int min, int max) {
		this(new Integer(min), new Integer(max));
	}

	/**
	 * Convenience constructor for <code>float</code> ranges.
	 */
	public Range(float min, float max) {
		this(new Float(min), new Float(max));
	}

	/**
	 * Convenience constructor for <code>double</code> ranges.
	 */
	public Range(double min, double max) {
		this(new Double(min), new Double(max));
	}

	public Object getMin() {
		return min;
	}

	public Object getMax() {
		return max;
	}

	public boolean isInclusive() {
		return inclusive;
	}

	/**
	 * Test if the specified argument falls within the established range.
	 *
	 * @see org.springframework.rules.Constraint#test(java.lang.Object)
	 */
	public boolean test(Object argument) {
		return this.rangeConstraint.test(argument);
	}

	public String toString() {
		return new ToStringBuilder(this).append("rangeConstraint",
				rangeConstraint).toString();
	}

}