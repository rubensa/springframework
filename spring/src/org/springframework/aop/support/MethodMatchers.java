/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.util.Assert;

/**
 * Static utility methods for composing
 * {@link org.springframework.aop.MethodMatcher MethodMatchers}.
 *
 * <p>A MethodMatcher may be evaluated statically (based on method
 * and target class) or need further evaluation dynamically
 * (based on arguments at the time of method invocation).
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 11.11.2003
 * @see ClassFilters
 * @see Pointcuts
 */
public abstract class MethodMatchers {

	/**
	 * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.
	 * @param a the first MethodMatcher
	 * @param b the second MethodMatcher
	 * @return a distinct MethodMatcher that matches all methods that either
	 * of the given MethodMatchers matches
	 */
	public static MethodMatcher union(MethodMatcher a, MethodMatcher b) {
		return new UnionMethodMatcher(a, b);
	}

	/**
	 * Match all methods that <i>both</i> of the given MethodMatchers match.
	 * @param a the first MethodMatcher
	 * @param b the second MethodMatcher
	 * @return a distinct MethodMatcher that matches all methods that both
	 * of the given MethodMatchers matches
	 */
	public static MethodMatcher intersection(MethodMatcher a, MethodMatcher b) {
		return new IntersectionMethodMatcher(a, b);
	}

	/**
	 * Apply the given MethodMatcher to the given Method, supporting an
	 * {@link org.springframework.aop.IntroductionAwareMethodMatcher}
	 * (if applicable).
	 * @param mm the MethodMatcher to apply
	 * @param method the candidate method
	 * @param targetClass the target class (may be <code>null</code>, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @param hasIntroductions <code>true</code> if the object on whose behalf we are
	 * asking is the subject on one or more introductions; <code>false</code> otherwise
	 * @return whether or not this method matches statically
	 */
	public static boolean matches(MethodMatcher mm, Method method, Class targetClass, boolean hasIntroductions) {
		return ((mm instanceof IntroductionAwareMethodMatcher &&
				((IntroductionAwareMethodMatcher) mm).matches(method, targetClass, hasIntroductions)) ||
				mm.matches(method, targetClass));
	}


	/**
	 * MethodMatcher implementation for a union of two given MethodMatchers.
	 */
	private static class UnionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private MethodMatcher a;
		private MethodMatcher b;

		private UnionMethodMatcher(MethodMatcher a, MethodMatcher b) {
			Assert.notNull(a, "First MethodMatcher must not be null");
			Assert.notNull(b, "Second MethodMatcher must not be null");
			this.a = a;
			this.b = b;
		}

		public boolean matches(Method method, Class targetClass, boolean hasIntroductions) {
			return MethodMatchers.matches(this.a, method, targetClass, hasIntroductions) ||
					MethodMatchers.matches(this.b, method, targetClass, hasIntroductions);
		}

		public boolean matches(Method method, Class targetClass) {
			return this.a.matches(method, targetClass) || this.b.matches(method, targetClass);
		}

		public boolean isRuntime() {
			return this.a.isRuntime() || this.b.isRuntime();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			return this.a.matches(method, targetClass, args) || this.b.matches(method, targetClass, args);
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof UnionMethodMatcher)) {
				return false;
			}
			UnionMethodMatcher that = (UnionMethodMatcher) obj;
			return (this.a.equals(that.a) && this.b.equals(that.b));
		}

		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.a.hashCode();
			hashCode = 37 * hashCode + this.b.hashCode();
			return hashCode;
		}
	}


	/**
	 * MethodMatcher implementation for an intersection of two given MethodMatchers.
	 */
	private static class IntersectionMethodMatcher implements IntroductionAwareMethodMatcher, Serializable {

		private MethodMatcher a;
		private MethodMatcher b;

		private IntersectionMethodMatcher(MethodMatcher a, MethodMatcher b) {
			Assert.notNull(a, "First MethodMatcher must not be null");
			Assert.notNull(b, "Second MethodMatcher must not be null");
			this.a = a;
			this.b = b;
		}

		public boolean matches(Method method, Class targetClass, boolean hasIntroductions) {
			return MethodMatchers.matches(this.a, method, targetClass, hasIntroductions) &&
					MethodMatchers.matches(this.b, method, targetClass, hasIntroductions);
		}

		public boolean matches(Method method, Class targetClass) {
			return this.a.matches(method, targetClass) && this.b.matches(method, targetClass);
		}

		public boolean isRuntime() {
			return this.a.isRuntime() || this.b.isRuntime();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			// Because a dynamic intersection may be composed of a static and dynamic part,
			// we must avoid calling the 3-arg matches method on a dynamic matcher, as
			// it will probably be an unsupported operation.
			boolean aMatches = this.a.isRuntime() ?
					this.a.matches(method, targetClass, args) : this.a.matches(method, targetClass);
			boolean bMatches = this.b.isRuntime() ?
					this.b.matches(method, targetClass, args) : this.b.matches(method, targetClass);
			return aMatches && bMatches;
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof IntersectionMethodMatcher)) {
				return false;
			}
			IntersectionMethodMatcher that = (IntersectionMethodMatcher) other;
			return (this.a.equals(that.a) && this.b.equals(that.b));
		}

		public int hashCode() {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.a.hashCode();
			hashCode = 37 * hashCode + this.b.hashCode();
			return hashCode;
		}
	}

}
