/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.support.DynamicMethodMatcherPointcutAdvice;
import org.springframework.aop.framework.support.SimpleIntroductionAdvice;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.TimeStamped;

/**
 * Test cases for AOP FactoryBean, using XML bean factory.
 * Note that this FactoryBean will work in any bean factory
 * implementation.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id$
 */
public class ProxyFactoryBeanTests extends TestCase {
	
	private BeanFactory factory;

	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public ProxyFactoryBeanTests(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("test.xml");
		this.factory = new XmlBeanFactory(is, null);
	}


	public void testIsDynamicProxy() {
		ITestBean test1 = (ITestBean) factory.getBean("test1");
		assertTrue("test1 is a dynamic proxy", Proxy.isProxyClass(test1.getClass()));
	}
	
	
	/**
	 * The instances are equal, but do not have object identity.
	 * Interceptors and interfaces and the target are the same.
	 */
	public void testSingletonInstancesAreEqual() {
		ITestBean test1 = (ITestBean) factory.getBean("test1");
		ITestBean test1_1 = (ITestBean) factory.getBean("test1");
		//assertTrue("Singleton instances ==", test1 == test1_1);
		assertEquals("Singleton instances ==", test1, test1_1);
		test1.setAge(25);
		assertEquals(test1.getAge(), test1_1.getAge());
		test1.setAge(250);
		assertEquals(test1.getAge(), test1_1.getAge());
		ProxyConfig pc1 = (ProxyConfig) test1;
		ProxyConfig pc2 = (ProxyConfig) test1_1;
		assertEquals(pc1.getAdvices(), pc2.getAdvices());
		int oldLength = pc1.getAdvices().size();
		DebugInterceptor di = new DebugInterceptor();
		pc1.addInterceptor(1, di);
		assertEquals(pc1.getAdvices(), pc2.getAdvices());
		assertEquals(oldLength + 1, pc2.getAdvices().size());
		assertEquals(di.getCount(), 0);
		test1.setAge(5);
		assertEquals(test1_1.getAge(), test1.getAge());
		assertEquals(di.getCount(), 3);
	}
	
	
	public void testPrototypeInstancesAreNotEqual() {
		ITestBean test2 = (ITestBean) factory.getBean("prototype");
		ITestBean test2_1 = (ITestBean) factory.getBean("prototype");
		assertTrue("Prototype instances !=", test2 != test2_1);
		assertTrue("Prototype instances equal", test2.equals(test2_1));
	}
	
	
	/**
	 * Uses its own bean factory XML for clarity
	 * @param beanName name of the ProxyFactoryBean definition that should
	 * be a prototype
	 */
	private void testPrototypeInstancesAreIndependent(String beanName) {
		// Initial count value set in bean factory XML 
		int INITIAL_COUNT = 10;
		
		InputStream is = getClass().getResourceAsStream("prototypeTests.xml");
		BeanFactory bf = new XmlBeanFactory(is);
		
		// Check it works without AOP
		SideEffectBean raw = (SideEffectBean) bf.getBean("prototypeTarget");
		assertEquals(INITIAL_COUNT, raw.getCount() );
		raw.doWork();
		assertEquals(INITIAL_COUNT+1, raw.getCount() );
		raw = (SideEffectBean) bf.getBean("prototypeTarget");
		assertEquals(INITIAL_COUNT, raw.getCount() );
		
		// Now try with advised instances
		SideEffectBean prototype2FirstInstance = (SideEffectBean) bf.getBean(beanName);
		assertEquals(INITIAL_COUNT, prototype2FirstInstance.getCount() );
		prototype2FirstInstance.doWork();
		assertEquals(INITIAL_COUNT + 1, prototype2FirstInstance.getCount() );

		SideEffectBean prototype2SecondInstance = (SideEffectBean) bf.getBean(beanName);
		assertEquals(INITIAL_COUNT, prototype2SecondInstance.getCount() );
		assertEquals(INITIAL_COUNT + 1, prototype2FirstInstance.getCount() );

	}
	
	public void testPrototypeInstancesAreIndependentWithInvokerInterceptor() {
		testPrototypeInstancesAreIndependent("prototype");
	}
	
	public void testPrototypeInstancesAreIndependentWithTargetName() {
		testPrototypeInstancesAreIndependent("prototype2");
	}
	
	/**
	 * Test invoker is automatically added to manipulate target
	 */
	public void testAutoInvoker() {
		String name = "Hieronymous";
		TestBean target = (TestBean) factory.getBean("test");
		target.setName(name);
		ITestBean autoInvoker = (ITestBean) factory.getBean("autoInvoker");
		assertTrue(autoInvoker.getName().equals(name));
	}

	public void testCanGetFactoryReferenceAndManipulate() {
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test1");
		assertTrue(config.getExposeInvocation() == false);
		assertTrue(config.getAdvices().size() == 2);
		
		ITestBean tb = (ITestBean) factory.getBean("test1");
		// no exception 
		tb.hashCode();
		
		final Exception ex = new UnsupportedOperationException("invoke");
		// Add evil interceptor to head of list
		config.addInterceptor(0, new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				throw ex;
			}
		});
		assertTrue(config.getAdvices().size() == 3);
		
		tb = (ITestBean) factory.getBean("test1"); 
		try {
			// Will fail now
			tb.hashCode();
			fail("Evil interceptor added programmatically should fail all method calls");
		} 
		catch (Exception thrown) {
			assertTrue(thrown == ex);
		}
	}
	
	/**
	 * Should see effect immediately on behaviour.
	 */
	public void testCanAddAndRemoveAspectInterfacesOnSingleton() {
		try {
			TimeStamped ts = (TimeStamped) factory.getBean("test1");
			fail("Shouldn't implement TimeStamped before manipulation");
		}
		catch (ClassCastException ex) {
		}
	
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test1");
		long time = 666L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor();
		ti.setTime(time);
		
		// add to front of interceptor chain
		int oldCount = config.getAdvices().size();
		config.addAdvice(0, new SimpleIntroductionAdvice(ti, TimeStamped.class));
		
		assertTrue(config.getAdvices().size() == oldCount + 1);
	
		TimeStamped ts = (TimeStamped) factory.getBean("test1");
		assertTrue(ts.getTimeStamp() == time);
	
		// Can remove
		config.removeInterceptor(ti);

		assertTrue(config.getAdvices().size() == oldCount);
	
		try {
			// Existing reference will fail
			ts.getTimeStamp();
			fail("Existing object won't implement this interface any more");
		}
		catch (RuntimeException ex) {
		}

	
		try {
			ts = (TimeStamped) factory.getBean("test1");
			fail("Should no longer implement TimeStamped");
		}
		catch (ClassCastException ex) {
		}
	
		// Now check non-effect of removing interceptor that isn't there
		config.removeInterceptor(new DebugInterceptor());
	
		assertTrue(config.getAdvices().size() == oldCount);
	
		ITestBean it = (ITestBean) ts;
		DebugInterceptor debugInterceptor = new DebugInterceptor();
		config.addInterceptor(0, debugInterceptor);
		it.getSpouse();
		assertEquals(1, debugInterceptor.getCount());
		config.removeInterceptor(debugInterceptor);
		it.getSpouse();
		// not invoked again
		assertTrue(debugInterceptor.getCount() == 1);
	}
	
	
	/**
	 * Try adding and removing interfaces and interceptors on prototype.
	 * Changes will only affect future references obtained from the factory.
	 * Each instance will be independent.
	 */
	public void testCanAddAndRemoveAspectInterfacesOnPrototype() {
		try {
			TimeStamped ts = (TimeStamped) factory.getBean("test2");
			fail("Shouldn't implement TimeStamped before manipulation");
		}
		catch (ClassCastException ex) {
		}
		
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test2");
		long time = 666L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor();
		ti.setTime(time);
		// Add to head of interceptor chain
		int oldCount = config.getAdvices().size();
		config.addAdvice(0, new SimpleIntroductionAdvice(ti, TimeStamped.class));
		assertTrue(config.getAdvices().size() == oldCount + 1);
		
		TimeStamped ts = (TimeStamped) factory.getBean("test2");
		assertEquals(time, ts.getTimeStamp());
		
		// Can remove
		config.removeInterceptor(ti);
		assertTrue(config.getAdvices().size() == oldCount);
		
		// Check no change on existing object reference
		assertTrue(ts.getTimeStamp() == time);
		
		try {
			ts = (TimeStamped) factory.getBean("test2");
			fail("Should no longer implement TimeStamped");
		}
		catch (ClassCastException ex) {
		}
		
		// Now check non-effect of removing interceptor that isn't there
		config.removeInterceptor(new DebugInterceptor());
		assertTrue(config.getAdvices().size() == oldCount);
		
		ITestBean it = (ITestBean) ts;
		DebugInterceptor debugInterceptor = new DebugInterceptor();
		config.addInterceptor(0, debugInterceptor);
		it.getSpouse();
		// Won't affect existing reference
		assertTrue(debugInterceptor.getCount() == 0);
		it = (ITestBean) factory.getBean("test2");
		it.getSpouse();
		assertEquals(1, debugInterceptor.getCount());
		config.removeInterceptor(debugInterceptor);
		it.getSpouse();
		
		// Still invoked wiht old reference
		assertEquals(2, debugInterceptor.getCount());
		
		// not invoked with new object
		it = (ITestBean) factory.getBean("test2");
		it.getSpouse();
		assertEquals(2, debugInterceptor.getCount());
		
		// Our own timestamped reference should still work
		assertEquals(time, ts.getTimeStamp());
	}
	
	
	
	/**
	 * Note that we can't add or remove interfaces without reconfiguring the
	 * singleton. 
	 * TODO address this?
	 *
	 */
	public void testCanAddAndRemoveAspectInterfacesOnSingletonByCasting() {
		ITestBean it = (ITestBean) factory.getBean("test1");
		ProxyConfig pc = (ProxyConfig) it;
		it.getAge();
		DebugInterceptor di = new DebugInterceptor();
		pc.addInterceptor(0, di);
		assertEquals(0, di.getCount());
		it.setAge(25);
		assertEquals(25, it.getAge());
		assertEquals(2, di.getCount());
	}
	
	
	public void testMethodPointcuts() {
		ITestBean tb = (ITestBean) factory.getBean("pointcuts");
		PointcutForVoid.reset();
		assertTrue("No methods intercepted", PointcutForVoid.methodNames.isEmpty());
		tb.getAge();
		assertTrue("Not void: shouldn't have intercepted", PointcutForVoid.methodNames.isEmpty());
		tb.setAge(1);
		tb.getAge();
		tb.setName("Tristan");
		tb.toString();
		assertTrue("Should have recorded 2 invocations, not " + PointcutForVoid.methodNames.size(),
				PointcutForVoid.methodNames.size() == 2);
		assertTrue(PointcutForVoid.methodNames.get(0).equals("setAge"));
		assertTrue(PointcutForVoid.methodNames.get(1).equals("setName"));
	}
	
	public void testNoInterceptorNames() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("noInterceptorNames");
			fail("Should require interceptor names");
		}
		catch (FatalBeanException ex) {
			// Ok
		}
	}
	
	public void testEmptyInterceptorNames() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("emptyInterceptorNames");
			fail("Interceptor names cannot be empty");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ok
		}
	}
	
	/**
	 * Globals must be followed by a target
	 *
	 */
	public void testGlobalsWithoutTarget() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("globalsWithoutTarget");
			fail("Should require target name");
		}
		catch (FatalBeanException ex) {
			// Ok
		}
	}
	
	/**
	 * Checks that globals get invoked,
	 * and that they can add aspect interfaces unavailable
	 * to other beans. These interfaces don't need
	 * to be included in proxiedInterface [].
	 */
	public void testGlobalsCanAddAspectInterfaces() {
		AddedGlobalInterface agi = (AddedGlobalInterface) factory.getBean("autoInvoker");
		assertTrue(agi.globalsAdded() == -1);
		
		ProxyFactoryBean pfb = (ProxyFactoryBean) factory.getBean("&validGlobals");
		// 2 globals + 2 explicit
		assertTrue(pfb.getAdvices().size() == 4);
		
		ApplicationListener l = (ApplicationListener) factory.getBean("validGlobals");
		agi = (AddedGlobalInterface) l;
		assertTrue(agi.globalsAdded() == -1);
		
		try {
			agi = (AddedGlobalInterface) factory.getBean("test1");
			fail("Aspect interface should't be implemeneted without globals");
		}
		catch (ClassCastException ex) {
		}
	}
	
	//public void testGlobalsCannotBeProxies
	
	/**
	 * Fires only on void methods. Saves list of methods intercepted.
	 */
	public static class PointcutForVoid extends DynamicMethodMatcherPointcutAdvice {
		
		public static List methodNames = new LinkedList();
		
		public static void reset() {
			methodNames.clear();
		}
		
		public PointcutForVoid() {
			super( new MethodInterceptor() {
				public Object invoke(MethodInvocation invocation) throws Throwable {
					methodNames.add(invocation.getMethod().getName());
					return invocation.proceed();
				}
			});
		}
		
		/** Should fire only if it returns null */
		public boolean matches(Method m, Class targetClass, Object[] args) {//, AttributeRegistry attributeRegistry) {
			//System.out.println(mi.getMethod().getReturnType());
			return m.getReturnType() == Void.TYPE;
		}

	}
	
	/**
	 * Aspect interface
	 */
	public interface AddedGlobalInterface {
		int globalsAdded();
	}
	
	/**
	 * Use as a global interceptor. Checks that 
	 * global interceptors can add aspect interfaces.
	 * NB: Add only via global interceptors in XML file.
	 */
	public static class GlobalAspectInterfaceInterceptor implements IntroductionInterceptor {
		public boolean implementsInterface(Class intf) {
			return intf.equals(AddedGlobalInterface.class);
		}
		public Object invoke(MethodInvocation mi) throws Throwable {
			//System.out.println("GlobalAspectInterfaceInterceptor.invoke");
			if (mi.getMethod().getDeclaringClass().equals(AddedGlobalInterface.class))
				return new Integer(-1);
			return mi.proceed();
		}
	}
	
	public static class GlobalIntroductionAdvice implements IntroductionAdvice {
		
		private IntroductionInterceptor gi = new GlobalAspectInterfaceInterceptor();

		/**
		 * @see org.springframework.aop.IntroductionAdvice#getClassFilter()
		 */
		public ClassFilter getClassFilter() {
			return ClassFilter.TRUE;
		}

		/**
		 * @see org.springframework.aop.IntroductionAdvice#getIntroductionInterceptor()
		 */
		public IntroductionInterceptor getIntroductionInterceptor() {
			return this.gi;
		}

		/**
		 * @see org.springframework.aop.IntroductionAdvice#getInterfaces()
		 */
		public Class[] getInterfaces() {
			return new Class[] { AddedGlobalInterface.class };
		}

		/**
		 * @see org.springframework.aop.Advice#isPerInstance()
		 */
		public boolean isPerInstance() {
			return false;
		}
		
	}
	
}
