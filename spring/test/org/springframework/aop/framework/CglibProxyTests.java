/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import net.sf.cglib.CodeGenerationException;

import org.aopalliance.intercept.AspectException;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13-Mar-2003
 * @version $Id$
 */
public class CglibProxyTests extends AbstractAopProxyTests {
	
	public CglibProxyTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * @see org.springframework.aop.framework.AbstractAopProxyTests#setInMode(org.springframework.aop.framework.AdvisedSupport)
	 */
	protected Object createProxy(AdvisedSupport as) {
		as.setProxyTargetClass(true);
		Object proxy = as.createAopProxy().getProxy();
		assertTrue(AopUtils.isCglibProxy(proxy));
		return proxy;
	}
	
	protected AopProxy createAopProxy(AdvisedSupport as) {
		as.setProxyTargetClass(true);
		return new AopProxy(as);
	}
	
	protected boolean requiresTarget() {
		return true;
	}
	
	public void testNullConfig() {
		try {
			// TODO change
			AopProxy aop = new AopProxy(null);
			aop.getProxy();
			fail("Shouldn't allow null interceptors");
		} 
		catch (AopConfigException ex) {
			// Ok
		}
	}

	
	public void testNoTarget() {
		AdvisedSupport pc =
			new AdvisedSupport(new Class[] { ITestBean.class });
		pc.addInterceptor(new NopInterceptor());
		try {
			AopProxy aop = createAopProxy(pc);
			aop.getProxy();
			fail("Shouldn't allow no target with CGLIB proxy");
			// TODO fix
		} 
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	

	public void testProxyCanBeClassNotInterface() throws Throwable {
		TestBean raw = new TestBean();
		raw.setAge(32);
		mockTargetSource.setTarget(raw);
		AdvisedSupport pc = new AdvisedSupport(new Class[] {});
		pc.setTargetSource(mockTargetSource);
		AopProxy aop = new AopProxy(pc);

		Object proxy = aop.getProxy();
		assertTrue("Proxy is CGLIB enhanced", proxy.getClass().getName().indexOf("$$") != -1);
		assertTrue(proxy instanceof ITestBean);
		assertTrue(proxy instanceof TestBean);
		TestBean tb = (TestBean) proxy;
		assertEquals("Correct age", 32, tb.getAge());
	}

	
	public void testCGLIBProxyingGivesMeaningfulExceptionIfAskedToProxyNonvisibleClass() {
		class YouCantSeeThis {
			void hidden() {
			}
		};
		YouCantSeeThis mine = new YouCantSeeThis();
		try {
			ProxyFactory pf = new ProxyFactory(mine);
			pf.getProxy();
			fail("Shouldn't be able to proxy non-visible class with CGLIB");
		}
		catch (AspectException ex) {
			// Check that stack trace is preserved
			assertTrue(ex.getRootCause() instanceof CodeGenerationException);
			
			// Check that error message is helpful
			
			// TODO check why these methods fail with NPE on AOP Alliance code
			//assertTrue(ex.getMessage().indexOf("final") != -1);
			//assertTrue(ex.getMessage().indexOf("visible") != -1);
		}
		
	}


}
