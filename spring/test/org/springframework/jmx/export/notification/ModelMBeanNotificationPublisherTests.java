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

package org.springframework.jmx.export.notification;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import junit.framework.TestCase;

import org.springframework.jmx.export.SpringModelMBean;
import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 */
public final class ModelMBeanNotificationPublisherTests extends TestCase {

	public void testCtorWithNullMBean() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new ModelMBeanNotificationPublisher(null, createObjectName(), this);
			}
		}.runTest();
	}

	public void testCtorWithNullObjectName() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new ModelMBeanNotificationPublisher(new SpringModelMBean(), null, this);
			}
		}.runTest();
	}

	public void testCtorWithNullManagedResource() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new ModelMBeanNotificationPublisher(new SpringModelMBean(), createObjectName(), null);
			}
		}.runTest();
	}

	public void testSendNullNotification() throws Exception {
		final NotificationPublisher publisher
				= new ModelMBeanNotificationPublisher(new SpringModelMBean(), createObjectName(), this);
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				publisher.sendNotification(null);
			}
		}.runTest();
	}

	public void testSendVanillaNotification() throws Exception {
		StubSpringModelMBean mbean = new StubSpringModelMBean();
		Notification notification = new Notification("network.alarm.router", mbean, 1872);
		ObjectName objectName = createObjectName();

		NotificationPublisher publisher = new ModelMBeanNotificationPublisher(mbean, objectName, mbean);
		publisher.sendNotification(notification);

		assertNotNull(mbean.getActualNotification());
		assertSame("The exact same Notification is not being passed through from the publisher to the mbean.", notification, mbean.getActualNotification());
		assertSame("The 'source' property of the Notification is not being set to the ObjectName of the associated MBean.", objectName, mbean.getActualNotification().getSource());
	}

	public void testSendAttributeChangeNotification() throws Exception {
		StubSpringModelMBean mbean = new StubSpringModelMBean();
		Notification notification = new AttributeChangeNotification(mbean, 1872, System.currentTimeMillis(), "Shall we break for some tea?", "agree", "java.lang.Boolean", Boolean.FALSE, Boolean.TRUE);
		ObjectName objectName = createObjectName();

		NotificationPublisher publisher = new ModelMBeanNotificationPublisher(mbean, objectName, mbean);
		publisher.sendNotification(notification);

		assertNotNull(mbean.getActualNotification());
		assertTrue(mbean.getActualNotification() instanceof AttributeChangeNotification);
		assertSame("The exact same Notification is not being passed through from the publisher to the mbean.", notification, mbean.getActualNotification());
		assertSame("The 'source' property of the Notification is not being set to the ObjectName of the associated MBean.", objectName, mbean.getActualNotification().getSource());
	}

	public void testSendAttributeChangeNotificationWhereSourceIsNotTheManagedResource() throws Exception {
		StubSpringModelMBean mbean = new StubSpringModelMBean();
		Notification notification = new AttributeChangeNotification(this, 1872, System.currentTimeMillis(), "Shall we break for some tea?", "agree", "java.lang.Boolean", Boolean.FALSE, Boolean.TRUE);
		ObjectName objectName = createObjectName();

		NotificationPublisher publisher = new ModelMBeanNotificationPublisher(mbean, objectName, mbean);
		publisher.sendNotification(notification);

		assertNotNull(mbean.getActualNotification());
		assertTrue(mbean.getActualNotification() instanceof AttributeChangeNotification);
		assertSame("The exact same Notification is not being passed through from the publisher to the mbean.", notification, mbean.getActualNotification());
		assertSame("The 'source' property of the Notification is *wrongly* being set to the ObjectName of the associated MBean.", this, mbean.getActualNotification().getSource());
	}

	private static ObjectName createObjectName() throws MalformedObjectNameException {
		return ObjectName.getInstance("foo:type=bar");
	}


	private static class StubSpringModelMBean extends SpringModelMBean {

		private Notification actualNotification;

		public StubSpringModelMBean() throws MBeanException, RuntimeOperationsException {
		}

		public Notification getActualNotification() {
			return this.actualNotification;
		}

		public void sendNotification(Notification notification) throws RuntimeOperationsException {
			this.actualNotification = notification;
		}

		public void sendAttributeChangeNotification(AttributeChangeNotification notification) throws RuntimeOperationsException {
			this.actualNotification = notification;
		}
	}

}
