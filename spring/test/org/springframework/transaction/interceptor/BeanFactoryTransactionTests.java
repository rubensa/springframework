/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.io.InputStream;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.MockControl;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Test cases for AOP transaction management.
 * @author Rod Johnson
 * @since 23-Apr-2003
 * @version $Id$
 */
public class BeanFactoryTransactionTests extends TestCase {
	
	private BeanFactory factory;

	public void setUp() {
		InputStream is = getClass().getResourceAsStream("transactionalBeanFactory.xml");
		this.factory = new XmlBeanFactory(is, null);
		ITestBean testBean = (ITestBean) factory.getBean("target");
		testBean.setAge(666);
	}

	public void testGetsAreNotTransactionalWithProxyFactory1() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory1");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory2() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory2");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory3() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory3");
		assertTrue("testBean is a full proxy", testBean instanceof TestBean);
		executeGetsAreNotTransactional(testBean);
	}

	public void executeGetsAreNotTransactional(ITestBean testBean) throws NoSuchMethodException {
		// Install facade
		MockControl ptmControl = EasyMock.controlFor(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptmControl.getMock();
		// Expect no methods
		ptmControl.activate();
		PlatformTransactionManagerFacade.delegate = ptm;
		
		assertTrue("Age should not be " + testBean.getAge(), testBean.getAge() == 666);
		// Check no calls
		ptmControl.verify();
		
		// Install facade expecting a call
		ptmControl = EasyMock.controlFor(PlatformTransactionManager.class);
		ptm = (PlatformTransactionManager) ptmControl.getMock();
		TransactionStatus txStatus = new TransactionStatus(null, true);
		TransactionInterceptor txInterceptor = (TransactionInterceptor) factory.getBean("txInterceptor");
		MethodMapTransactionAttributeSource txAttSrc = (MethodMapTransactionAttributeSource) txInterceptor.getTransactionAttributeSource();
		ptm.getTransaction((TransactionDefinition) txAttSrc.methodMap.values().iterator().next());
		//ptm.getTransaction(null);
		ptmControl.setReturnValue(txStatus);
		ptm.commit(txStatus);
		ptmControl.setVoidCallable();
		ptmControl.activate();
		PlatformTransactionManagerFacade.delegate = ptm;
		
		// TODO same as old age to avoid ordering effect for now
		int age = 666;
		testBean.setAge(age);
		ptmControl.verify();
		
		assertTrue(testBean.getAge() == age);
	}

}
