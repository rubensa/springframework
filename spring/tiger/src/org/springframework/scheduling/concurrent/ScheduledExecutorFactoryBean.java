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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that sets up
 * a JDK 1.5 {@link java.util.concurrent.ScheduledExecutorService}
 * (by default: {@link java.util.concurrent.ScheduledThreadPoolExecutor}
 * as implementation) and exposes it for bean references.
 *
 * <p>Allows for registration of {@link ScheduledExecutorTask ScheduledExecutorTasks},
 * automatically starting the {@link ScheduledExecutorService} on initialization and
 * cancelling it on destruction of the context. In scenarios that just require static
 * registration of tasks at startup, there is no need to access the
 * {@link ScheduledExecutorService} instance itself in application code.
 *
 * <p>Note that {@link java.util.concurrent.ScheduledExecutorService}
 * uses a {@link Runnable} instance that is shared between repeated executions,
 * in contrast to Quartz which instantiates a new Job for each execution.
 *
 * <p>This class is analogous to the
 * {@link org.springframework.scheduling.timer.TimerFactoryBean}
 * class for the JDK 1.3 {@link java.util.Timer} facility.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScheduledExecutorTask
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see org.springframework.scheduling.timer.TimerFactoryBean
 */
public class ScheduledExecutorFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private ScheduledExecutorTask[] scheduledExecutorTasks;

	private int poolSize = 1;

	private ThreadFactory threadFactory = Executors.defaultThreadFactory();

	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

	private ScheduledExecutorService executor;


	/**
	 * Register a list of ScheduledExecutorTask objects with the ScheduledExecutorService
	 * that this FactoryBean creates. Depending on each ScheduledExecutorTask's settings,
	 * it will be registered via one of ScheduledExecutorService's schedule methods.
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public void setScheduledExecutorTasks(ScheduledExecutorTask[] scheduledExecutorTasks) {
		this.scheduledExecutorTasks = scheduledExecutorTasks;
	}

	/**
	 * Set the ScheduledExecutorService's pool size.
	 * Default is 1.
	 */
	public void setPoolSize(int poolSize) {
		Assert.isTrue(poolSize > 0, "'poolSize' must be 1 or higher");
		this.poolSize = poolSize;
	}

	/**
	 * Set the ThreadFactory to use for the ThreadPoolExecutor's thread pool.
	 * Default is the ThreadPoolExecutor's default thread factory.
	 * @see java.util.concurrent.Executors#defaultThreadFactory()
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = (threadFactory != null ? threadFactory : Executors.defaultThreadFactory());
	}

	/**
	 * Set the RejectedExecutionHandler to use for the ThreadPoolExecutor.
	 * Default is the ThreadPoolExecutor's default abort policy.
	 * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
	 */
	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler =
				(rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy());
	}


	public void afterPropertiesSet() {
		logger.info("Initializing SchedulerExecutorService");
		this.executor = createExecutor(this.poolSize, this.threadFactory, this.rejectedExecutionHandler);

		// Register specified ScheduledExecutorTasks, if necessary.
		if (!ObjectUtils.isEmpty(this.scheduledExecutorTasks)) {
			registerTasks(this.scheduledExecutorTasks, this.executor);
		}
	}

	/**
	 * Create a new {@link ScheduledExecutorService} instance.
	 * Called by <code>afterPropertiesSet</code>.
	 * <p>Default implementation creates a {@link ScheduledThreadPoolExecutor}.
	 * Can be overridden in subclasses to provide custom
	 * {@link ScheduledExecutorService} instances.
	 * @param poolSize the specified pool size
	 * @param threadFactory the ThreadFactory to use
	 * @param rejectedExecutionHandler the RejectedExecutionHandler to use
	 * @return a new ScheduledExecutorService instance
	 * @see #afterPropertiesSet()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor
	 */
	protected ScheduledExecutorService createExecutor(
			int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		return new ScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler);
	}

	/**
	 * Register the specified {@link ScheduledExecutorTask ScheduledExecutorTasks}
	 * on the given {@link ScheduledExecutorService}.
	 * @param tasks the specified ScheduledExecutorTasks (never empty)
	 * @param executor the ScheduledExecutorService to register the tasks on.
	 */
	protected void registerTasks(ScheduledExecutorTask[] tasks, ScheduledExecutorService executor) {
		for (int i = 0; i < tasks.length; i++) {
			ScheduledExecutorTask task = tasks[i];
			if (task.isOneTimeTask()) {
				executor.schedule(task.getRunnable(), task.getDelay(), task.getTimeUnit());
			}
			else {
				if (task.isFixedRate()) {
					executor.scheduleAtFixedRate(
							task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
				}
				else {
					executor.scheduleWithFixedDelay(
							task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
				}
			}
		}
	}


	public Object getObject() {
		return this.executor;
	}

	public Class getObjectType() {
		return (this.executor != null ? this.executor.getClass() : ScheduledExecutorService.class);
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Cancel the ScheduledExecutorService on bean factory shutdown,
	 * stopping all scheduled tasks.
	 * @see java.util.concurrent.ScheduledExecutorService#shutdown()
	 */
	public void destroy() {
		logger.info("Shutting down ScheduledExecutorService");
		this.executor.shutdown();
	}

}
