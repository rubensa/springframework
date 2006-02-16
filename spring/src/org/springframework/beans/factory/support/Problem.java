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

package org.springframework.beans.factory.support;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class Problem {

	private String message;

	private String bean;

	private Throwable rootCause;

	private Location location;

	public Problem(String message, String bean, Throwable rootCause, Location location) {
		this.message = message;
		this.bean = bean;
		this.rootCause = rootCause;
		this.location = location;
	}

	public String getBean() {
		return bean;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	public void setRootCause(Throwable rootCause) {
		this.rootCause = rootCause;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getResourceDescription() {
		return getLocation().getResource().toString();
	}

	public String toString() {
		return new StringBuilder()
						.append(this.message)
						.append(" @ <")
						.append(getResourceDescription())
						.append(">.").toString();
	}
}
