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

package org.springframework.web.bind.support;

/**
 * Simple implementation of the {@link FormStatus} interface,
 * keeping the complete flag as an instance variable.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class SimpleFormStatus implements FormStatus {

	private boolean complete = false;


	public void setComplete() {
		this.complete = true;
	}

	/**
	 * Return whether the current form processing is complete.
	 */
	public boolean isComplete() {
		return this.complete;
	}

}
