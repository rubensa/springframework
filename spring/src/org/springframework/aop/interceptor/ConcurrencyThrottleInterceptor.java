package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor that throttles concurrent access, blocking invocations
 * if a specified concurrency limit is reached.
 *
 * <p>Can be applied to methods of local services that involve heavy use
 * of system resources, in a scenario where it is more efficient to
 * throttle concurrency for a specific service rather than restricting
 * the entire thread pool (e.g. the web container's thread pool).
 *
 * @author Juergen Hoeller
 * @since 11.02.2004
 */
public class ConcurrencyThrottleInterceptor implements MethodInterceptor {

	protected final Log logger = LogFactory.getLog(getClass());

	private int concurrencyLimit = 1;

	private int concurrencyCount = 0;

	/**
	 * Set the maximum number of parallel invocations that this interceptor
	 * allows. Default is 1 (having the same effect as a synchronized block).
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean debug = logger.isDebugEnabled();
		synchronized (this) {
			while (this.concurrencyCount >= this.concurrencyLimit) {
				if (debug) {
					logger.debug("Concurrency count " + this.concurrencyCount +
											 " has reached limit " + this.concurrencyLimit + " - blocking");
				}
				try {
					wait();
				}
				catch (InterruptedException ex) {
				}
			}
			if (debug) {
				logger.debug("Entering method at concurrency count " + this.concurrencyCount);
			}
			this.concurrencyCount++;
		}
		try {
			return methodInvocation.proceed();
		}
		finally {
			synchronized (this) {
				this.concurrencyCount--;
				if (debug) {
					logger.debug("Returning from method at concurrency count " + this.concurrencyCount);
				}
				notify();
			}
		}
	}

}
