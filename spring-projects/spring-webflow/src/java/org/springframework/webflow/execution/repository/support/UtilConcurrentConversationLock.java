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

package org.springframework.webflow.execution.repository.support;

import org.springframework.core.NestedRuntimeException;
import org.springframework.webflow.execution.repository.ConversationLock;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

/**
 * A conversation lock that relies on a {@link ReentrantLock} within Doug Lea's
 * <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>
 * package. For use on JDK 1.3 and 1.4.
 *
 * @author Keith Donald
 * @author Rob Harrop
 */
public class UtilConcurrentConversationLock implements ConversationLock {

	/**
	 * The {@link ReentrantLock} instance.
	 */
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * Acquires the lock.
	 * @throws SystemInterruptedException if the lock cannot be acquired due to interruption.
	 */
	public void lock() {
		try {
			this.lock.acquire();
		}
		catch (InterruptedException e) {
			throw new SystemInterruptedException("Unable to acquire lock.", e);
		}
	}

	/**
	 * Releases the lock.
	 */
	public void unlock() {
		this.lock.release();
	}

	/**
	 * <code>Exception</code> indicating that some {@link Thread} was {@link Thread#interrupt() interrupted}
	 * during processing and as such processing was halted.
	 */
	public static class SystemInterruptedException extends NestedRuntimeException {

		/**
		 * Creates a new <code>SystemInterruptedException</code>.
		 * @param msg the <code>Exception</code> message.
		 */
		public SystemInterruptedException(String msg) {
			super(msg);
		}

		/**
		 * Creates a new <code>SystemInterruptedException</code>.
		 * @param msg the <code>Exception</code> message.
		 * @param cause the root cause of this <code>Exception</code>.
		 */
		public SystemInterruptedException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
}