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
package org.springframework.webflow.execution;

import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * A aimple flow execution listener loader that simply returns a static listener
 * array on each invocation. For more elaborate needs see the
 * {@link ConditionalFlowExecutionListenerLoader}.
 * 
 * @see ConditionalFlowExecutionListenerLoader
 * 
 * @author Keith Donald
 */
public final class StaticFlowExecutionListenerLoader implements FlowExecutionListenerLoader {

	/**
	 * The listener array to return when {@link #getListeners(Flow)} is invoked. 
	 */
	private final FlowExecutionListener[] listeners;

	/**
	 * Creates a new listener loader that returns an empty listener array on
	 * each invocation.
	 */
	public StaticFlowExecutionListenerLoader() {
		this(new FlowExecutionListener[0]);
	}

	/**
	 * Creates a new listener loader that returns the provided listener array on
	 * each invocation. Clients should not attempt to modify the passed in array
	 * as no deep copy is made.
	 * @param listeners the listener array.
	 */
	public StaticFlowExecutionListenerLoader(FlowExecutionListener[] listeners) {
		Assert.notNull(listeners, "The listener array is required");
		this.listeners = listeners;
	}

	public FlowExecutionListener[] getListeners(Flow flow) {
		return listeners;
	}
}