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

package org.springframework.context.event;

import junit.framework.TestCase;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.TestListener;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.AssertThrows;

/**
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Rick Evans
 */
public final class EventPublicationInterceptorTests extends TestCase {

	public void testWithNoApplicationEventClassSupplied() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
				ApplicationContext ctx = new StaticApplicationContext();
				interceptor.setApplicationEventPublisher(ctx);
				interceptor.afterPropertiesSet();
			}
		}.runTest();
	}

	public void testWithNonApplicationEventClassSupplied() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
				ApplicationContext ctx = new StaticApplicationContext();
				interceptor.setApplicationEventPublisher(ctx);
				interceptor.setApplicationEventClass(getClass());
				interceptor.afterPropertiesSet();
			}
		}.runTest();
	}

	public void testWithAbstractStraightApplicationEventClassSupplied() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
				ApplicationContext ctx = new StaticApplicationContext();
				interceptor.setApplicationEventPublisher(ctx);
				interceptor.setApplicationEventClass(ApplicationEvent.class);
				interceptor.afterPropertiesSet();
			}
		}.runTest();
	}

	public void testWithApplicationEventClassThatDoesntExposeAValidCtor() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
				ApplicationContext ctx = new StaticApplicationContext();
				interceptor.setApplicationEventPublisher(ctx);
				interceptor.setApplicationEventClass(TestEventWithNoValidOneArgObjectCtor.class);
				interceptor.afterPropertiesSet();
			}
		}.runTest();
	}

	public void testExpectedBehavior() throws Exception {
		TestBean target = new TestBean();
		final TestListener listener = new TestListener();

		class TestContext extends StaticApplicationContext {
			protected void onRefresh() throws BeansException {
				addListener(listener);
			}
		}

		StaticApplicationContext ctx = new TestContext();

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("applicationEventClass", TestEvent.class.getName());
		// should automatically receive applicationEventPublisher reference
		ctx.registerSingleton("publisher", EventPublicationInterceptor.class, pvs);

		ctx.registerSingleton("otherListener", FactoryBeanTestListener.class);

		ctx.refresh();

		EventPublicationInterceptor interceptor =
				(EventPublicationInterceptor) ctx.getBean("publisher");
		ProxyFactory factory = new ProxyFactory(target);
		factory.addAdvice(0, interceptor);

		ITestBean testBean = (ITestBean) factory.getProxy();

		// invoke any method on the advised proxy to see if the interceptor has been invoked
		testBean.getAge();

		// two events: ContextRefreshedEvent and TestEvent
		assertTrue("Interceptor must have published 2 events", listener.getEventCount() == 2);
		TestListener otherListener = (TestListener) ctx.getBean("&otherListener");
		assertTrue("Interceptor must have published 2 events", otherListener.getEventCount() == 2);
	}


	public static class TestEvent extends ApplicationEvent {

		public TestEvent(Object source) {
			super(source);
		}
	}


	public static final class TestEventWithNoValidOneArgObjectCtor extends ApplicationEvent {

		public TestEventWithNoValidOneArgObjectCtor() {
			super("");
		}
	}


	public static class FactoryBeanTestListener extends TestListener implements FactoryBean {

		public Object getObject() throws Exception {
			return "test";
		}

		public Class getObjectType() {
			return String.class;
		}

		public boolean isSingleton() {
			return true;
		}
	}

}
