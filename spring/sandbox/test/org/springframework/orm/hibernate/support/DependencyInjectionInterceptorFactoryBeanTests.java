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

package org.springframework.orm.hibernate.support;

import java.io.Serializable;

import junit.framework.TestCase;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.metadata.ClassMetadata;

import org.easymock.MockControl;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.DependencyInjectionAspectSupportTests;

import bsh.Interpreter;

/**
 * 
 * @author Rod Johnson
 */
public class DependencyInjectionInterceptorFactoryBeanTests extends TestCase {
	
	private static final Integer id = new Integer(25);
	
	public void testNoChainNoRegistration() throws Exception {
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		// We'll ask for a different class
		dias.addAutowireByNameClass(NestedTestBean.class);
		dias.setSessionFactory(mockSessionFactory(TestBean.class));
		dias.setBeanFactory(new DefaultListableBeanFactory());
		dias.afterPropertiesSet();
		
		Interceptor interceptor = (Interceptor) dias.getObject();
		assertNull("Doesn't know about this class", interceptor.instantiate(TestBean.class, id));
	}
	
	public void testAutowireByType() throws Exception {
		DependencyInjectionInterceptorFactoryBean dias = new DependencyInjectionInterceptorFactoryBean();
		DefaultListableBeanFactory bf = DependencyInjectionAspectSupportTests.beanFactoryWithTestBeanSingleton();
		
		dias.addAutowireByTypeClass(TestBean.class);
		dias.setSessionFactory(mockSessionFactory(TestBean.class));
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();
		
		Interceptor interceptor = (Interceptor) dias.getObject();
		TestBean tb = (TestBean) interceptor.instantiate(TestBean.class, id);
		assertNull("Doesn't know about this class", interceptor.instantiate(NestedTestBean.class, id));
		assertNotNull("Knows about this class", tb);
		assertSame("Spouse dependency was populated", bf.getBean("kerry"), tb.getSpouse() );
		assertEquals("Id was set", 25, tb.getAge());
	}

	protected SessionFactory mockSessionFactory(Class clazz) throws Exception {
		MockControl mc = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) mc.getMock();
		sf.getClassMetadata(clazz);
		
		// Beanshell's an easy way to mock just one method of many...
		Interpreter bsh = new Interpreter();
		bsh.eval("public String getIdentifierPropertyName() { return \"age\"; }");
		ClassMetadata cm = (ClassMetadata) bsh.eval("return (" + ClassMetadata.class.getName() + ") this");
		mc.setReturnValue(cm);
		mc.replay();
		return sf;
	}
}
