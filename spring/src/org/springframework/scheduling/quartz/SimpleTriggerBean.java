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

package org.springframework.scheduling.quartz;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;

/**
 * Convenience subclass of Quartz's {@link org.quartz.SimpleTrigger}
 * class, making bean-style usage easier.
 *
 * <p>SimpleTrigger itself is already a JavaBean but lacks sensible defaults.
 * This class uses the Spring bean name as job name, the Quartz default group
 * ("DEFAULT") as job group, the current time as start time, and indefinite
 * repetition, if not specified.
 *
 * <p>This class will also register the trigger with the job name and group of
 * a given {@link org.quartz.JobDetail}. This allows {@link SchedulerFactoryBean}
 * to automatically register a trigger for the corresponding JobDetail,
 * instead of registering the JobDetail separately.
 *
 * <p><b>NOTE:</b> This convenience subclass does not work with trigger
 * persistence in Quartz 1.6, due to a change in Quartz's trigger handling.
 * Use Quartz 1.5 if you rely on trigger persistence based on this class,
 * or the standard Quartz {@link org.quartz.SimpleTrigger} class instead.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see #setName
 * @see #setGroup
 * @see #setStartTime
 * @see #setJobName
 * @see #setJobGroup
 * @see #setJobDetail
 * @see SchedulerFactoryBean#setTriggers
 * @see SchedulerFactoryBean#setJobDetails
 * @see CronTriggerBean
 */
public class SimpleTriggerBean extends SimpleTrigger
    implements JobDetailAwareTrigger, BeanNameAware, InitializingBean {

	/** Constants for the SimpleTrigger class */
	private static final Constants constants = new Constants(SimpleTrigger.class);


	private long startDelay = 0;

	private JobDetail jobDetail;

	private String beanName;


	public SimpleTriggerBean() {
		setRepeatCount(REPEAT_INDEFINITELY);
	}

	/**
	 * Register objects in the JobDataMap via a given Map.
	 * <p>These objects will be available to this Trigger only,
	 * in contrast to objects in the JobDetail's data map.
	 * @param jobDataAsMap Map with String keys and any objects as values
	 * (for example Spring-managed beans)
	 * @see JobDetailBean#setJobDataAsMap
	 */
	public void setJobDataAsMap(Map jobDataAsMap) {
		getJobDataMap().putAll(jobDataAsMap);
	}

	/**
	 * Set the misfire instruction via the name of the corresponding
	 * constant in the {@link org.quartz.SimpleTrigger} class.
	 * Default is <code>MISFIRE_INSTRUCTION_SMART_POLICY</code>.
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_FIRE_NOW
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
	 * @see org.quartz.SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT
	 * @see org.quartz.Trigger#MISFIRE_INSTRUCTION_SMART_POLICY
	 */
	public void setMisfireInstructionName(String constantName) {
		setMisfireInstruction(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set a list of TriggerListener names for this job, referring to
	 * non-global TriggerListeners registered with the Scheduler.
	 * <p>A TriggerListener name always refers to the name returned
	 * by the TriggerListener implementation.
	 * @see SchedulerFactoryBean#setTriggerListeners
	 * @see org.quartz.TriggerListener#getName
	 */
	public void setTriggerListenerNames(String[] names) {
		for (int i = 0; i < names.length; i++) {
			addTriggerListener(names[i]);
		}
	}

	/**
	 * Set the delay before starting the job for the first time.
	 * The given number of milliseconds will be added to the current
	 * time to calculate the start time. Default is 0.
	 * <p>This delay will just be applied if no custom start time was
	 * specified. However, in typical usage within a Spring context,
	 * the start time will be the container startup time anyway.
	 * Specifying a relative delay is appropriate in that case.
	 * @see #setStartTime
	 */
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}

	/**
	 * Set the JobDetail that this trigger should be associated with.
	 * <p>This is typically used with a bean reference if the JobDetail
	 * is a Spring-managed bean. Alternatively, the trigger can also
	 * be associated with a job by name and group.
	 * @see #setJobName
	 * @see #setJobGroup
	 */
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public JobDetail getJobDetail() {
		return this.jobDetail;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	public void afterPropertiesSet() throws ParseException {
		if (getName() == null) {
			setName(this.beanName);
		}
		if (getGroup() == null) {
			setGroup(Scheduler.DEFAULT_GROUP);
		}
		if (getStartTime() == null) {
			setStartTime(new Date(System.currentTimeMillis() + this.startDelay));
		}
		if (this.jobDetail != null) {
			setJobName(this.jobDetail.getName());
			setJobGroup(this.jobDetail.getGroup());
		}
	}

}
