package org.springframework.scheduling.quartz;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.MethodInvoker;

/**
 * FactoryBean that exposes a JobDetail object that delegates
 * job execution to a specified (static or non-static) method.
 * Avoids the need to implement a one-line Quartz Job that just
 * invokes an existing business method.
 *
 * <p>Derived from MethodInvoker to share common properties and
 * behavior with MethodInvokingFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see org.springframework.beans.factory.config.MethodInvokingFactoryBean
 */
public class MethodInvokingJobDetailFactoryBean extends MethodInvoker
    implements FactoryBean, BeanNameAware, InitializingBean {

	private String name;

	private String group = Scheduler.DEFAULT_GROUP;

	private String beanName;

	private JobDetail jobDetail;

	/**
	 * Set the name of the job.
	 * Default is the bean name of this FactoryBean.
	 * @see org.quartz.JobDetail#setName
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the group of the job.
	 * Default is the default group of the Scheduler.
	 * @see org.quartz.JobDetail#setGroup
	 * @see org.quartz.Scheduler#DEFAULT_GROUP
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		prepare();
		this.jobDetail = new JobDetail(this.name != null ? this.name : this.beanName,
		                               this.group, MethodInvokingJob.class);
		this.jobDetail.getJobDataMap().put("methodInvoker", this);
	}

	public Object getObject() {
		return this.jobDetail;
	}

	public Class getObjectType() {
		return (this.jobDetail != null) ? this.jobDetail.getClass() : JobDetail.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Quartz Job implementation that invokes a specified method.
	 * Automatically applied by MethodInvokingJobDetailFactoryBean.
	 */
	public static class MethodInvokingJob extends QuartzJobBean {

		private MethodInvoker methodInvoker;

		/**
		 * Set the MethodInvoker to use.
		 */
		public void setMethodInvoker(MethodInvoker methodInvoker) {
			this.methodInvoker = methodInvoker;
		}

		/**
		 * Invoke the method via the MethodInvoker.
		 */
		protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
			try {
				this.methodInvoker.invoke();
			}
			catch (Exception ex) {
				throw new JobExecutionException("Could not invoke method '" + this.methodInvoker.getTargetMethod() +
				                                "' of target object [" + this.methodInvoker.getTargetObject() + "]", ex, false);
			}
		}
	}

}
