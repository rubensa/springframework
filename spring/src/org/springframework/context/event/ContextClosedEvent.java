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

package org.springframework.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an <code>ApplicationContext</code> gets closed.
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 */
public class ContextClosedEvent extends ApplicationEvent {

	/**
	 * Create a new <code>ContextRefreshedEvent</code>.
	 * @param source the <code>ApplicationContext</code> that has been closed
	 * (must not be <code>null</code>)
	 */
	public ContextClosedEvent(ApplicationContext source) {
		super(source);
	}

	/**
	 * Get the <code>ApplicationContext</code> that has been closed.
	 */
	public ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}

}
