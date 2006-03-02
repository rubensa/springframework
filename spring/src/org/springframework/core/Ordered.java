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

package org.springframework.core;

/**
 * Interface that can be implemented by objects that should be
 * orderable, e.g. in a Collection.
 * <p>
 * The actual order can be interpreted as prioritization, with
 * the first object (with the lowest order value) having the highest
 * priority.
 *
 * @author Juergen Hoeller
 * @since 07.04.2003
 */
public interface Ordered {

	/**
	 * Useful constant for the lowest precedence value.
	 *
	 * @see java.lang.Integer#MAX_VALUE
	 */
	public static final int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


	/**
	 * Return the order value of this object, with a
	 * higher value meaning greater in terms of sorting.
	 * <p>
	 * Normally starting with 0 or 1, with {@link #LOWEST_PRECEDENCE}
	 * indicating greatest.
	 * Same order values will result in arbitrary positions
	 * for the affected objects.
	 * <p>
	 * Higher value can be interpreted as lower priority,
	 * consequently the first object has highest priority
	 * (somewhat analogous to Servlet "load-on-startup" values).
	 *
	 * @return the order value
	 * @see #LOWEST_PRECEDENCE
	 */
	public int getOrder();

}
