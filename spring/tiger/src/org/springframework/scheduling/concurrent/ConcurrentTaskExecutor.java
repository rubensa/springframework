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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;

/**
 * Adapter that takes a JDK 1.5 <code>java.util.concurrent.Executor</code> and
 * exposes a Spring {@link org.springframework.core.task.TaskExecutor} for it.
 *
 * <p><b>NOTE:</b> This class implements Spring's
 * {@link org.springframework.core.task.TaskExecutor} interface as well as the JDK 1.5
 * {@link java.util.concurrent.Executor} interface, with the former being the primary
 * interface, the other just serving as secondary convenience. For this reason, the
 * exception handling follows the TaskExecutor contract rather than the Executor contract,
 * in particular regarding the {@link org.springframework.core.task.TaskRejectedException}.
 *
 * <p>Note that there is a pre-built {@link ThreadPoolTaskExecutor} that allows for
 * defining a JDK 1.5 {@link java.util.concurrent.ThreadPoolExecutor in bean style,
 * exposing it as a Spring {@link org.springframework.core.task.TaskExecutor} directly.
 * This is a convenient alternative to a raw ThreadPoolExecutor definition with
 * a separate definition of the present adapter class.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see ThreadPoolTaskExecutor
 */
public class ConcurrentTaskExecutor implements SchedulingTaskExecutor, Executor {

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
		try {
			this.concurrentExecutor.execute(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
		}
	}

	/**
	 * This task executor prefers short-lived work units.
	 */
	public boolean prefersShortLivedTasks() {
		return true;
	}

}
