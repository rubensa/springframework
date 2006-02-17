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

package org.springframework.webflow.execution.repository.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.repository.ConversationLock;
import org.springframework.core.JdkVersion;

/**
 * Simple utility class for creating instances of {@link org.springframework.webflow.execution.repository.ConversationLock} based
 * on the current execution environment.
 * 
 * @author Keith Donald
 * @author Rob Harrop
 */
class ConversationLockFactory {

	private static final Log logger = LogFactory.getLog(ConversationLockFactory.class);

	private static boolean utilConcurrentPresent;

	static {
		try {
			Class.forName("EDU.oswego.cs.dl.util.concurrent.ReentrantLock");
			utilConcurrentPresent = true;
		}
		catch (ClassNotFoundException ex) {
			utilConcurrentPresent = false;
		}
	}

	/**
	 * When running on Java 1.5+, returns an instance of {@link org.springframework.webflow.execution.repository.support.JdkConcurrentConversationLock}. When
	 * running on older JDKs with the 'util.concurrent' package available, an instance of
	 * {@link org.springframework.webflow.execution.repository.support.UtilConcurrentConversationLock} is returned. In all other cases an instance of
	 * {@link org.springframework.webflow.execution.repository.support.NoOpConversationLock} is returned.
	 */
	public static ConversationLock createLock() {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_15) {
			return new JdkConcurrentConversationLock();
		}
		else if (utilConcurrentPresent) {
			return new UtilConcurrentConversationLock();
		}
		else {
			logger.warn("Unable to enable conversation locking. Switch to Java 5 or above, " +
							"or put the 'util.concurrent' package on the classpath " +
							"to enable locking in your environment.");
			return NoOpConversationLock.INSTANCE;
		}
	}
}
