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

package org.springframework.core.task;

/**
 * Simple task executor interface that abstracts the execution
 * of a {@link Runnable}.
 * 
 * <p>Implementations can use all sorts of different execution strategies,
 * such as: synchronous, asynchronous, using a thread pool, and more.
 *
 * <p>Identical to JDK 1.5's {@link java.util.concurrent.Executor}
 * interface. Separate mainly for compatibility with JDK 1.3+.
 * Implementations can simply implement the JDK 1.5 <code>Executor</code>
 * interface as well, as it defines the exact same method signature.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface TaskExecutor {

	/**
	 * Execute the given <code>task</code>.
	 * <p>The call might return immediately if the implementation uses
	 * an asynchronous execution strategy, or might block in the case
	 * of synchronous execution.
	 * @param task the <code>Runnable</code> to execute
	 */
	void execute(Runnable task);

}
