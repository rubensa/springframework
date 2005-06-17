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
package org.springframework.webflow;

import org.springframework.core.enums.support.ShortCodedLabeledEnum;

/**
 * Type-safe enumeration of possible flow session statuses.
 * 
 * @see org.springframework.webflow.FlowSession
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowSessionStatus extends ShortCodedLabeledEnum {

	/**
	 * Initial status of a flow session; the session has been created but not
	 * yet activated.
	 */
	public static FlowSessionStatus CREATED = new FlowSessionStatus(0, "Created");

	/**
	 * A flow session with ACTIVE status is currently executing.
	 */
	public static FlowSessionStatus ACTIVE = new FlowSessionStatus(1, "Active");

	/**
	 * A flow session with PAUSED status is currently waiting on the user to
	 * signal an event.
	 */
	public static FlowSessionStatus PAUSED = new FlowSessionStatus(2, "Paused");

	/**
	 * A flow session that is SUSPENDED is not actively executing a flow. It is
	 * waiting for subflow execution to complete before continuing.
	 */
	public static FlowSessionStatus SUSPENDED = new FlowSessionStatus(3, "Suspended");

	/**
	 * A flow session that has ENDED is no longer actively executing a flow.
	 * This is the final status of a flow session.
	 */
	public static FlowSessionStatus ENDED = new FlowSessionStatus(4, "Ended");

	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private FlowSessionStatus(int code, String label) {
		super(code, label);
	}
}