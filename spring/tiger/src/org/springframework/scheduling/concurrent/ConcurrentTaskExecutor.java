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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.core.task.TaskExecutor;

/**
 * Adapter that takes a JDK 1.5 <code>java.util.concurrent.Executor</code>
 * and exposes a Spring TaskExecutor for it.
 *
 * <p>Note that there is a pre-built ThreadPoolTaskExecutor that allows
 * for defining a JDK 1.5 ThreadPoolExecutor in bean style, exposing
 * it as both Spring TaskExecutor and JDK 1.5 Executor. This is a
 * convenient alternative to a direct ThreadPoolExecutor definition
 * with separate ConcurrentTaskExecutor adapter definition.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see ThreadPoolTaskExecutor
 */
public class ConcurrentTaskExecutor implements TaskExecutor, Executor {

	private Executor concurrentExecutor;


	/**
	 * Create a new ConcurrentTaskExecutor,
	 * using a single thread executor as default.
	 * @see java.util.concurrent.Executors#newSingleThreadExecutor()
	 */
	public ConcurrentTaskExecutor() {
		setConcurrentExecutor(null);
	}

	/**
	 * Create a new ConcurrentTaskExecutor,
	 * using the given JDK 1.5 concurrent executor as default.
	 * @param concurrentExecutor the JDK 1.5 concurrent executor to delegate to
	 */
	public ConcurrentTaskExecutor(Executor concurrentExecutor) {
		setConcurrentExecutor(concurrentExecutor);
	}

	/**
	 * Specify the JDK 1.5 concurrent executor to delegate to.
	 */
	public void setConcurrentExecutor(Executor concurrentExecutor) {
		this.concurrentExecutor =
				(concurrentExecutor != null ? concurrentExecutor : Executors.newSingleThreadExecutor());
	}


	/**
	 * Delegates to the specified JDK 1.5 concurrent executor.
	 * @see java.util.concurrent.Executor#execute(Runnable)
	 */
	public void execute(Runnable task) {
		this.concurrentExecutor.execute(task);
	}

}
