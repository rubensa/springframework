/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.remoting.caucho;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author Juergen Hoeller
 * @since 16.05.2003
 */
public class CauchoTestSuite extends TestCase {

	public void testHessianProxyFactoryBeanWithAccessError() throws Exception {
		HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
		try {
			factory.setServiceInterface(TestBean.class);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();
		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testBurlapProxyFactoryBeanWithAccessError() throws Exception {
		BurlapProxyFactoryBean factory = new BurlapProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();
		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}


}
