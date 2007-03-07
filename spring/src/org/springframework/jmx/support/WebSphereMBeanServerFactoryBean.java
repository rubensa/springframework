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

package org.springframework.jmx.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.MBeanServer;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.MBeanServerNotFoundException;

/**
 * FactoryBean that obtains a WebSphere {@link javax.management.MBeanServer}
 * reference through WebSphere's proprietary <code>AdminServiceFactory</code> API,
 * available on WebSphere 5.1 and higher.
 *
 * <p>Exposes the <code>MBeanServer</code> for bean references.
 * This FactoryBean is a direct alternative to {@link MBeanServerFactoryBean},
 * which uses standard JMX 1.2 API to access the platform's MBeanServer.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0.3
 * @see com.ibm.websphere.management.AdminServiceFactory#getMBeanFactory()
 * @see com.ibm.websphere.management.MBeanFactory#getMBeanServer()
 * @see javax.management.MBeanServer
 * @see MBeanServerFactoryBean
 */
public class WebSphereMBeanServerFactoryBean implements FactoryBean, InitializingBean {

	private static final String ADMIN_SERVICE_FACTORY_CLASS = "com.ibm.websphere.management.AdminServiceFactory";

	private static final String GET_MBEAN_FACTORY_METHOD = "getMBeanFactory";

	private static final String GET_MBEAN_SERVER_METHOD = "getMBeanServer";


	private MBeanServer mbeanServer;


	public void afterPropertiesSet() throws MBeanServerNotFoundException {
		try {
			/*
			 * this.mbeanServer = AdminServiceFactory.getMBeanFactory().getMBeanServer();
			 */
			Class adminServiceClass = getClass().getClassLoader().loadClass(ADMIN_SERVICE_FACTORY_CLASS);
			Method getMBeanFactoryMethod = adminServiceClass.getMethod(GET_MBEAN_FACTORY_METHOD, new Class[0]);
			Object mbeanFactory = getMBeanFactoryMethod.invoke(null, new Object[0]);
			Method getMBeanServerMethod = mbeanFactory.getClass().getMethod(GET_MBEAN_SERVER_METHOD, new Class[0]);
			this.mbeanServer = (MBeanServer) getMBeanServerMethod.invoke(mbeanFactory, new Object[0]);
		}
		catch (ClassNotFoundException ex) {
			throw new MBeanServerNotFoundException("Could not find WebSphere's AdminServiceFactory class", ex);
		}
		catch (InvocationTargetException ex) {
			throw new MBeanServerNotFoundException(
					"WebSphere's AdminServiceFactory.getMBeanFactory/getMBeanServer method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new MBeanServerNotFoundException(
					"Could not access WebSphere's AdminServiceFactory.getMBeanFactory/getMBeanServer method", ex);
		}
	}


	public Object getObject() {
		return this.mbeanServer;
	}

	public Class getObjectType() {
		return (this.mbeanServer != null ? this.mbeanServer.getClass() : MBeanServer.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
