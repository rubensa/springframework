/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.DefaultInterceptionAroundAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcutAroundAdvisor;
import org.springframework.aop.support.SimpleIntroductionAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAroundAdvisor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13-Mar-2003
 * @version $Id$
 */
public abstract class AbstractAopProxyTests extends TestCase {
	
	protected MockTargetSource mockTargetSource = new MockTargetSource();

	public AbstractAopProxyTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Make a clean target source available if code wants to use it.
	 * The target must be set. Verification will be automatic in tearDown
	 * to ensure that it was used appropriately by code.
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		mockTargetSource.reset();
	}

	protected void tearDown() {
		mockTargetSource.verify();
	}
	
	/**
	 * Set in CGLIB or JDK mode
	 * @param as
	 */
	protected abstract Object createProxy(AdvisedSupport as);
	
	protected abstract AopProxy createAopProxy(AdvisedSupport as);
	
	/**
	 * Is a target always required?
	 * @return
	 */
	protected boolean requiresTarget() {
		return false;
	}
	
	
	public void testNoInterceptorsAndNoTarget() {
		AdvisedSupport pc =
			new AdvisedSupport(new Class[] { ITestBean.class });
		// Add no interceptors
		try {
			AopProxy aop = createAopProxy(pc);
			aop.getProxy();
			fail("Shouldn't allow no interceptors");
		} catch (AopConfigException ex) {
			// Ok
		}
	}
	
	private static class CheckMethodInvocationIsSameInAndOutInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			Method m = mi.getMethod();
			Object retval = mi.proceed();
			assertEquals("Method invocation has same method on way back", m, mi.getMethod());
			return retval;
		}
	}
	
	/**
	 * ExposeInvocation must be set to true
	 */
	private static class CheckMethodInvocationViaThreadLocalIsSameInAndOutInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			String task = "get invocation on way IN";
			try {
				MethodInvocation current = AopContext.currentInvocation();
				assertEquals(mi, current);
				Object retval = mi.proceed();
				task = "get invocation on way OUT";
				assertEquals(current, AopContext.currentInvocation());
				return retval;
			}
			catch (AspectException ex) {
				System.err.println(task + " for " + mi.getMethod());
				ex.printStackTrace();
				//fail("Can't find invocation: " + ex);
				throw ex;
			}
		}
	}
	
	/**
	 * Same thing for a proxy.
	* Only works when exposeProxy is set to true.
	* Checks that the proxy is the same on the way in and out.
	*/
	public static class ProxyMatcherInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			Object proxy = AopContext.currentProxy();
			Object ret = mi.proceed();
			// TODO why does this cause stack overflow?
			//assertEquals(proxy, AopContext.currentProxy());
			assertTrue(proxy == AopContext.currentProxy());
			return ret;
		}
	}
	
	/**
	 * Simple test that if we set values we can get them out again
	 *
	 */
	public void testValuesStick() {
		int age1 = 33;
		int age2 = 37;
		String name = "tony";
	
		TestBean target1 = new TestBean();
		target1.setAge(age1);
		ProxyFactory pf1 = new ProxyFactory(target1);
		pf1.addAdvisor(new DefaultInterceptionAroundAdvisor(new NopInterceptor()));
		pf1.addAdvisor(new SimpleIntroductionAdvice(new TimestampIntroductionInterceptor()));
		ITestBean tb = (ITestBean) target1;
		
		assertEquals(age1, tb.getAge());
		tb.setAge(age2);
		assertEquals(age2, tb.getAge());
		assertNull(tb.getName());
		tb.setName(name);
		assertEquals(name, tb.getName());
	}
	
	/**
	 * Check that the two MethodInvocations necessary are independent and
	 * don't conflict.
	 * Check also proxy exposure.
	 */
	public void testOneAdvisedObjectCallsAnother() {
		int age1 = 33;
		int age2 = 37;
		
		TestBean target1 = new TestBean();
		ProxyFactory pf1 = new ProxyFactory(target1);
		// Permit proxy and invocation checkers to get context from AopContext
		pf1.setExposeProxy(true);
		pf1.setExposeInvocation(true);
		assertTrue(pf1.getExposeInvocation());
		NopInterceptor di1 = new NopInterceptor();
		pf1.addInterceptor(0, di1);
		pf1.addInterceptor(1, new ProxyMatcherInterceptor());
		pf1.addInterceptor(2, new CheckMethodInvocationIsSameInAndOutInterceptor());
		pf1.addInterceptor(1, new CheckMethodInvocationViaThreadLocalIsSameInAndOutInterceptor());
		ITestBean advised1 = (ITestBean) pf1.getProxy();
		advised1.setAge(age1); // = 1 invocation
		
		TestBean target2 = new TestBean();
		ProxyFactory pf2 = new ProxyFactory(target2);
		pf2.setExposeInvocation(true);
		pf2.setExposeProxy(true);
		assertTrue(pf2.getExposeInvocation());
		NopInterceptor di2 = new NopInterceptor();
		pf2.addInterceptor(0, di2);
		pf2.addInterceptor(1, new ProxyMatcherInterceptor());
		pf2.addInterceptor(2, new CheckMethodInvocationIsSameInAndOutInterceptor());
		pf2.addInterceptor(1, new CheckMethodInvocationViaThreadLocalIsSameInAndOutInterceptor());
		//System.err.println(pf2.toProxyConfigString());
		ITestBean advised2 = (ITestBean) createProxy(pf2);
		advised2.setAge(age2);
		advised1.setSpouse(advised2); // = 2 invocations
		
		assertEquals("Advised one has correct age", age1, advised1.getAge()); // = 3 invocations
		assertEquals("Advised two has correct age", age2, advised2.getAge());
		// Means extra call on advised 2
		assertEquals("Advised one spouse has correct age", age2, advised1.getSpouse().getAge()); // = 4 invocations on 1 and another one on 2
		
		assertEquals("one was invoked correct number of times", 4, di1.getCount());
		// Got hit by call to advised1.getSpouse().getAge()
		assertEquals("one was invoked correct number of times", 3, di2.getCount());
	}
	
	
	public void testReentrance() {
		int age1 = 33;
	
		TestBean target1 = new TestBean();
		ProxyFactory pf1 = new ProxyFactory(target1);
		NopInterceptor di1 = new NopInterceptor();
		pf1.addInterceptor(0, di1);
		ITestBean advised1 = (ITestBean) createProxy(pf1);
		advised1.setAge(age1); // = 1 invocation
		advised1.setSpouse(advised1); // = 2 invocations
	
		assertEquals("one was invoked correct number of times", 2, di1.getCount());
		
		assertEquals("Advised one has correct age", age1, advised1.getAge()); // = 3 invocations
		assertEquals("one was invoked correct number of times", 3, di1.getCount());
		
		// = 5 invocations, as reentrant call to spouse is advised also
		assertEquals("Advised spouse has correct age", age1, advised1.getSpouse().getAge()); 
		
		assertEquals("one was invoked correct number of times", 5, di1.getCount());
	}

	
	public interface INeedsToSeeProxy {
		int getCount();
		void incrementViaThis();
		void incrementViaProxy();
		void increment();
	}
	
	public static class NeedsToSeeProxy implements INeedsToSeeProxy {
		private int count;
		public int getCount() {
			return count;
		}
		public void incrementViaThis() {
			this.increment();
		}
		public void incrementViaProxy() {
			INeedsToSeeProxy thisViaProxy = (INeedsToSeeProxy) AopContext.currentProxy();
			thisViaProxy.increment();
			Advised advised = (Advised) thisViaProxy;
			checkAdvised(advised);
		}
		
		protected void checkAdvised(Advised advised) {
		}
	
		public void increment() {
			++count;
		}
	};
	
	public static class TargetChecker extends NeedsToSeeProxy {
		protected void checkAdvised(Advised advised) {
			// TODO replace this check: no longer possible
			//assertEquals(advised.getTarget(), this);
		}
	};
	
	public void testTargetCanGetProxy() {
		NopInterceptor di = new NopInterceptor();
		INeedsToSeeProxy target = new TargetChecker();
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.setExposeProxy(true);
		assertTrue(proxyFactory.getExposeProxy());
	
		proxyFactory.addInterceptor(0, di);
		INeedsToSeeProxy proxied = (INeedsToSeeProxy) createProxy(proxyFactory);
		assertEquals(0, di.getCount());
		assertEquals(0, target.getCount());
		proxied.incrementViaThis();
		assertEquals("Increment happened", 1, target.getCount());
		
		assertEquals("Only one invocation via AOP as use of this wasn't proxied", 1, di.getCount());
		// 1 invocation
		assertEquals("Increment happened", 1, proxied.getCount());
		proxied.incrementViaProxy(); // 2 invoocations
		assertEquals("Increment happened", 2, target.getCount());
		assertEquals("3 more invocations via AOP as the first call was reentrant through the proxy", 4, di.getCount());
	}

			
	public void testTargetCantGetProxyByDefault() {
		NeedsToSeeProxy et = new NeedsToSeeProxy();
		ProxyFactory pf1 = new ProxyFactory(et);
		assertFalse(pf1.getExposeProxy());
		INeedsToSeeProxy proxied = (INeedsToSeeProxy) createProxy(pf1);
		try {
			proxied.incrementViaProxy();
			fail("Should have failed to get proxy as exposeProxy wasn't set to true");
		}
		catch (AspectException ex) {
			// Ok
		}
	}
	

	public void testContext() throws Throwable {
		testContext(true);
	}

	public void testNoContext() throws Throwable {
		testContext(false);
	}

	/**
	 * @param context if true, want context
	 * @throws Throwable
	 */
	private void testContext(final boolean context) throws Throwable {
		final String s = "foo";
		// Test return value
		MethodInterceptor mi = new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				if (!context) {
					assertNoInvocationContext();
				} else {
					assertTrue("have context", AopContext.currentInvocation() != null);
				}
				return s;
			}
		};
		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class });
		pc.setExposeInvocation(context);
		pc.addInterceptor(mi);
		// Keep CGLIB happy
		if (requiresTarget()) {
			pc.setTarget(new TestBean());
		}
		AopProxy aop = createAopProxy(pc);

		assertNoInvocationContext();
		ITestBean tb = (ITestBean) aop.getProxy();
		assertNoInvocationContext();
		assertTrue("correct return value", tb.getName() == s);
	}
	
	public static class OwnSpouse extends TestBean {
		public ITestBean getSpouse() {
			return this;
		}
	}

	/**
	 * Test that the proxy returns itself when the
	 * target returns <code>this</code>
	 * @throws Throwable
	 */
	public void testTargetReturnsThis() throws Throwable {
		// Test return value
		TestBean raw = new OwnSpouse();
	
		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class });
		pc.setTarget(raw);

		ITestBean tb = (ITestBean) createProxy(pc);
		assertTrue("this return is wrapped in proxy", tb.getSpouse() == tb);
	}

	

/*
	public void testCanAttach() throws Throwable {
		final TrapInterceptor tii = new TrapInvocationInterceptor();

		ProxyConfig pc = new ProxyConfigSupport(new Class[] { ITestBean.class }, false);
		pc.addInterceptor(tii);
		pc.addInterceptor(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				assertTrue("Saw same interceptor", invocation == tii.invocation);
				return null;
			}
		});
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		ITestBean tb = (ITestBean) aop.getProxy();
		tb.getSpouse();
		assertTrue(tii.invocation != null);
		
		// TODO strengthen this
	//	assertTrue(tii.invocation.getProxy() == tb);
		assertTrue(tii.invocation.getThis() == null);
	}
*/

	/**
	 * TODO also test undeclared: decide what it should do!
	 */
	public void testDeclaredException() throws Throwable {
		final Exception expectedException = new Exception();
		// Test return value
		MethodInterceptor mi = new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				throw expectedException;
			}
		};
		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class });
		pc.setExposeInvocation(true);
		pc.addInterceptor(mi);
		
		// We don't care about the object
		mockTargetSource.setTarget(new Object());
		pc.setTargetSource(mockTargetSource);
		AopProxy aop = createAopProxy(pc);

		try {
			ITestBean tb = (ITestBean) aop.getProxy();
			// Note: exception param below isn't used
			tb.exceptional(expectedException);
			fail("Should have thrown exception raised by interceptor");
		} 
		catch (Exception thrown) {
			assertEquals("exception matches", expectedException, thrown);
		}
	}
	
	/**
	 * Check that although a method is eligible for advice chain optimization and
	 * direct reflective invocation, it doesn't happen if we've asked to see the proxy,
	 * so as to guarantee a consistent programming model.
	 * @throws Throwable
	 */
	public void testTargetCanGetInvocationEvenIfNoAdviceChain() throws Throwable {
		NeedsToSeeProxy target = new NeedsToSeeProxy();
		AdvisedSupport pc = new AdvisedSupport(new Class[] { INeedsToSeeProxy.class } );
		pc.setTarget(target);
		assertTrue(pc.canOptimizeOutEmptyAdviceChain());
		pc.setExposeInvocation(true);
		assertFalse(pc.canOptimizeOutEmptyAdviceChain());
		pc.setExposeInvocation(false);
		assertTrue(pc.canOptimizeOutEmptyAdviceChain());
		pc.setExposeProxy(true);
		assertFalse(pc.canOptimizeOutEmptyAdviceChain());
		
		// Now let's try it with the special target
		AopProxy aop = createAopProxy(pc);
		INeedsToSeeProxy proxied = (INeedsToSeeProxy) aop.getProxy();
		// It will complain if it can't get the proxy
		proxied.incrementViaProxy();
	}
	
	/**
	 * Static for CGLIB visibility
	 */
	static class ContextTestBean2 extends ContextTestBean {
		protected void assertions(MethodInvocation invocation) {
			assertTrue(invocation.getThis() == this);
			assertTrue("Invocation should be on ITestBean: " + invocation.getMethod(), 
					ITestBean.class.isAssignableFrom(invocation.getMethod().getDeclaringClass()));;
		}
	}

	public void testTargetCanGetInvocation() throws Throwable {
		final ContextTestBean2 expectedTarget = new ContextTestBean2();
		
		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class, IOther.class });
		pc.setExposeInvocation(true);
		TrapTargetInterceptor tii = new TrapTargetInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				// Assert that target matches BEFORE invocation returns
				assertEquals("Target is correct", expectedTarget, invocation.getThis());
				return super.invoke(invocation);
			}
		};
		pc.addInterceptor(tii);
		pc.setTarget(expectedTarget);
		AopProxy aop = createAopProxy(pc);

		ITestBean tb = (ITestBean) aop.getProxy();
		tb.getName();
		// Not safe to trap invocation
		//assertTrue(tii.invocation == target.invocation);
		
		//assertTrue(target.invocation.getProxy() == tb);

	//	((IOther) tb).absquatulate();
		//MethodInvocation minv =  tii.invocation;
		//assertTrue("invoked on iother, not " + minv.getMethod().getDeclaringClass(), minv.getMethod().getDeclaringClass() == IOther.class);
		//assertTrue(target.invocation == tii.invocation);
	}

	/**
	 * Throw an exception if there is an Invocation
	 */
	private void assertNoInvocationContext() {
		try {
			AopContext.currentInvocation();
			fail("Expected no invocation context");
		} catch (AspectException ex) {
			// ok
		}
	}
	
	

	/**
	 * Test stateful interceptor
	 * @throws Throwable
	 */
	public void testMixin() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { Lockable.class, ITestBean.class });
		pc.addAdvisor(new SimpleIntroductionAdvice(new LockMixin(), Lockable.class));
		pc.setTarget(tb);
		
		int newAge = 65;
		ITestBean itb = (ITestBean) createProxy(pc);
		itb.setAge(newAge);
		assertTrue(itb.getAge() == newAge);

		Lockable lockable = (Lockable) itb;
		assertFalse(lockable.locked());
		lockable.lock();
		
		assertTrue(itb.getAge() == newAge);
		try {
			itb.setAge(1);
			fail("Setters should fail when locked");
		} 
		catch (LockedException ex) {
			// ok
		}
		assertTrue(itb.getAge() == newAge);
		
		// Unlock
		assertTrue(lockable.locked());
		lockable.unlock();
		itb.setAge(1);
		assertTrue(itb.getAge() == 1);
	}
	
	
	public void testReplaceArgument() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		pc.setTarget(tb);
		pc.addAdvisor(new StringSetterNullReplacementAdvice());
	
		ITestBean t = (ITestBean) pc.getProxy();
		int newAge = 5;
		t.setAge(newAge);
		assertTrue(t.getAge() == newAge);
		String newName = "greg";
		t.setName(newName);
		assertEquals(newName, t.getName());
		
		t.setName(null);
		// Null replacement magic should work
		assertTrue(t.getName().equals(""));
	}
	
	public void testCanCastProxyToProxyConfig() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(tb);
		NopInterceptor di = new NopInterceptor();
		pc.addInterceptor(0, di);

		ITestBean t = (ITestBean) createProxy(pc);
		assertEquals(0, di.getCount());
		t.setAge(23);
		assertEquals(23, t.getAge());
		assertEquals(2, di.getCount());
		
		Advised config = (Advised) t;
		assertEquals("Have 1 advisor", 1, config.getAdvisors().length);
		assertEquals(di, ((InterceptionAroundAdvisor) config.getAdvisors()[0]).getInterceptor());
		NopInterceptor di2 = new NopInterceptor();
		config.addInterceptor(1, di2);
		t.getName();
		assertEquals(3, di.getCount());
		assertEquals(1, di2.getCount());
		config.removeInterceptor(di);
		t.getAge();
		// Unchanged
		assertEquals(3, di.getCount());
		assertEquals(2, di2.getCount());
	}
	
	
	public static class NoInterfaces {
		private int age;
		public int getAge() { 
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
	}
	
	
	
	public void testCannotAddIntroductionInterceptorExceptInIntroductionAdvice() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addInterceptor(0, new TimestampIntroductionInterceptor());
			fail("Shouldn't be able to add introduction interceptor except via introduction advice");
		}
		catch (AopConfigException ex) {
			assertTrue(ex.getMessage().indexOf("ntroduction") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) createProxy(pc);
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	/**
	 * Check that the introduction advice isn't allowed to introduce interfaces
	 * that are unsupported by the IntroductionInterceptor
	 * @throws Throwable
	 */
	public void testCannotAddIntroductionAdviceWithUnimplementedInterface() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addAdvisor(0, new SimpleIntroductionAdvice(new TimestampIntroductionInterceptor(), ITestBean.class));
			fail("Shouldn't be able to add introduction advice introducing an unimplemented interface");
		}
		catch (AopConfigException ex) {
			//assertTrue(ex.getMessage().indexOf("ntroduction") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) createProxy(pc);
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	/**
	 * Should only be able to introduce interfaces, not classes
	 * @throws Throwable
	 */
	public void testCannotAddIntroductionAdviceToIntroduceClass() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addAdvisor(0, new SimpleIntroductionAdvice(new TimestampIntroductionInterceptor(), TestBean.class));
			fail("Shouldn't be able to add introduction advice that introduces a class, rather than an interface");
		}
		catch (AopConfigException ex) {
			assertTrue(ex.getMessage().indexOf("interface") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) createProxy(pc);
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	public void testAdviceSupportListeners() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		
		ProxyFactory pc = new ProxyFactory(target);
		CountingAdvisorListener l = new CountingAdvisorListener(pc);
		pc.addListener(l);
		RefreshCountingAdvisorChainFactory acf = new RefreshCountingAdvisorChainFactory();
		// Should be automatically added as a listener
		pc.setAdvisorChainFactory(acf);
		assertFalse(pc.isActive());
		assertEquals(0, l.activates);
		assertEquals(0, acf.refreshes);
		ITestBean proxied = (ITestBean) createProxy(pc);
		assertEquals(1, acf.refreshes);
		assertEquals(1, l.activates);
		assertTrue(pc.isActive());
		assertEquals(target.getAge(), proxied.getAge());
		assertEquals(0, l.adviceChanges);
		NopInterceptor di = new NopInterceptor();
		pc.addInterceptor(0, di);
		assertEquals(1, l.adviceChanges);
		assertEquals(2, acf.refreshes);
		assertEquals(target.getAge(), proxied.getAge());
		pc.removeInterceptor(di);
		assertEquals(2, l.adviceChanges);
		assertEquals(3, acf.refreshes);
		assertEquals(target.getAge(), proxied.getAge());
		pc.getProxy();
		assertEquals(1, l.activates);
		
		pc.removeListener(l);
		assertEquals(2, l.adviceChanges);
		pc.addAdvisor(new DefaultInterceptionAroundAdvisor(new NopInterceptor()));
		// No longer counting
		assertEquals(2, l.adviceChanges);
	}
	
	public void testExistingProxyChangesTarget() throws Throwable {
		TestBean tb1 = new TestBean();
		tb1.setAge(33);
		
		TestBean tb2 = new TestBean();
		tb2.setAge(26);
		TestBean tb3 = new TestBean();
		tb3.setAge(37);
		ProxyFactory pc = new ProxyFactory(tb1);
		NopInterceptor nop = new NopInterceptor();
		pc.addInterceptor(nop);
		ITestBean proxy = (ITestBean) createProxy(pc);
		assertEquals(nop.getCount(), 0);
		assertEquals(tb1.getAge(), proxy.getAge());
		assertEquals(nop.getCount(), 1);
		// Change to a new static target
		pc.setTarget(tb2);
		assertEquals(tb2.getAge(), proxy.getAge());
		assertEquals(nop.getCount(), 2);
		// Change to a new dynamic target
		HotSwappableTargetSource ts = new HotSwappableTargetSource(tb3);
		pc.setTargetSource(ts);
		assertEquals(tb3.getAge(), proxy.getAge());
		assertEquals(nop.getCount(), 3);
		ts.swap(tb1);
		assertEquals(tb1.getAge(), proxy.getAge());
		assertEquals(nop.getCount(), 4);
	}
	
	
	public static class CountingAdvisorListener implements AdvisedSupportListener {
		public int adviceChanges;
		public int activates;
		private AdvisedSupport expectedSource;
		
		public CountingAdvisorListener(AdvisedSupport expectedSource) {
			this.expectedSource = expectedSource;
		}
		
		public void adviceChanged(AdvisedSupport as) {
			assertEquals(expectedSource, as);
			++adviceChanges;
		}

		public void activated(AdvisedSupport as) {
			assertEquals(expectedSource, as);
			++activates;
		}
	}
	
	public class RefreshCountingAdvisorChainFactory implements AdvisorChainFactory {
		public int refreshes;
		
		public void adviceChanged(AdvisedSupport pc) {
			++refreshes;
		}

		public List getInterceptorsAndDynamicInterceptionAdvice(Advised pc, Object proxy, Method method, Class targetClass) {
			return AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(pc, proxy, method, targetClass);
		}
		
		public void activated(AdvisedSupport as) {
			++refreshes;
		}
	}
	
	/**
	 * Fires on setter methods that take a string. Replaces null arg
	 * with ""
	 */
	public static class StringSetterNullReplacementAdvice extends DynamicMethodMatcherPointcutAroundAdvisor {
		
		private static MethodInterceptor cleaner = new MethodInterceptor() {
			public Object invoke(MethodInvocation mi) throws Throwable {
				// We know it can only be invoked if there's a single parameter of type string
				mi.setArgument(0, "");
				return mi.proceed();
			}
		};
		
		public StringSetterNullReplacementAdvice() {
			super(cleaner);
		}

		public boolean matches(Method m, Class targetClass, Object[] args){//, AttributeRegistry attributeRegistry) {
			return args[0] == null;
		}

		public boolean matches(Method m, Class targetClass){//, AttributeRegistry attributeRegistry) {
			return m.getName().startsWith("set") &&
				m.getParameterTypes().length == 1 &&
				m.getParameterTypes()[0].equals(String.class);
		}
	}
	
	
	public void testDynamicMethodPointcutThatAlwaysAppliesStatically() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		TestDynamicPointcutAdvice dp = new TestDynamicPointcutAdvice(new NopInterceptor(), "getAge");
		pc.addAdvisor(dp);
		pc.setTarget(tb);
		ITestBean it = (ITestBean) createProxy(pc);
		assertEquals(dp.count, 0);
		int age = it.getAge();
		assertEquals(dp.count, 1);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(dp.count, 2);
	}
	
	public void testDynamicMethodPointcutThatAppliesStaticallyOnlyToSetters() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		// Could apply dynamically to getAge/setAge but not to getName
		TestDynamicPointcutAdvice dp = new TestDynamicPointcutForSettersOnly(new NopInterceptor(), "Age");
		pc.addAdvisor(dp);
		this.mockTargetSource.setTarget(tb);
		pc.setTargetSource(mockTargetSource);
		ITestBean it = (ITestBean) createProxy(pc);
		assertEquals(dp.count, 0);
		int age = it.getAge();
		// Statically vetoed
		assertEquals(0, dp.count);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(dp.count, 1);
		// Applies statically but not dynamically
		it.setName("joe");
		assertEquals(dp.count, 1);
	}
	
	
	public void testStaticMethodPointcut() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		NopInterceptor di = new NopInterceptor();
		TestStaticPointcutAdvice sp = new TestStaticPointcutAdvice(di, "getAge");
		pc.addAdvisor(sp);
		pc.setTarget(tb);
		ITestBean it = (ITestBean) createProxy(pc);
		assertEquals(di.getCount(), 0);
		int age = it.getAge();
		assertEquals(di.getCount(), 1);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(di.getCount(), 2);
	}
	
	public static interface IOverloads {
		void overload();
		int overload(int i);
		String overload(String foo);
		void noAdvice();
	}
	
	public static class Overloads implements IOverloads {
		public void overload() {
		}
		public int overload(int i) {
			return i;
		}
		public String overload(String s) {
			return s;
		}
		public void noAdvice() {
		}
	}
	
	public void testOverloadedMethodsWithDifferentAdvice() throws Throwable {
		Overloads target = new Overloads();
		ProxyFactory pc = new ProxyFactory(target);
		NopInterceptor overLoadVoids = new NopInterceptor();
		pc.addAdvisor(new StaticMethodMatcherPointcutAroundAdvisor(overLoadVoids) {
			public boolean matches(Method m, Class targetClass) {
				return m.getName().equals("overload") && m.getParameterTypes().length == 0;
			}
		});
		NopInterceptor overLoadInts = new NopInterceptor();
		pc.addAdvisor(new StaticMethodMatcherPointcutAroundAdvisor(overLoadInts) {
			public boolean matches(Method m, Class targetClass) {
				return m.getName().equals("overload") && m.getParameterTypes().length == 1 &&
					m.getParameterTypes()[0].equals(int.class);
			}
		});

		IOverloads proxy = (IOverloads) createProxy(pc);
		assertEquals(0, overLoadInts.getCount());
		assertEquals(0, overLoadVoids.getCount());
		proxy.overload();
		assertEquals(0, overLoadInts.getCount());
		assertEquals(1, overLoadVoids.getCount());
		assertEquals(25, proxy.overload(25));
		assertEquals(1, overLoadInts.getCount());
		assertEquals(1, overLoadVoids.getCount());
		proxy.noAdvice();
		assertEquals(1, overLoadInts.getCount());
		assertEquals(1, overLoadVoids.getCount());
	}
	
	
	protected static class TestDynamicPointcutAdvice extends DynamicMethodMatcherPointcutAroundAdvisor {
		
		private String pattern;
		public int count;
		
		public TestDynamicPointcutAdvice(MethodInterceptor mi, String pattern) {
			super(mi);
			this.pattern = pattern;
		}
		/**
		 * @see org.springframework.aop.framework.DynamicMethodPointcut#applies(java.lang.reflect.Method, java.lang.Object[], org.aopalliance.AttributeRegistry)
		 */
		public boolean matches(Method m, Class targetClass, Object[] args) {
			boolean run = m.getName().indexOf(pattern) != -1;
			if (run) ++count;
			return run;
		}
	}
	
	protected static class TestDynamicPointcutForSettersOnly extends TestDynamicPointcutAdvice {
		public TestDynamicPointcutForSettersOnly(MethodInterceptor mi, String pattern) {
			super(mi, pattern);
		}
		
		public boolean matches(Method m, Class clazz) {
			return m.getName().startsWith("set");
		}
	}
	
	protected static class TestStaticPointcutAdvice extends StaticMethodMatcherPointcutAroundAdvisor {
		
		private String pattern;
		private int count;
	
		public TestStaticPointcutAdvice(MethodInterceptor mi, String pattern) {
			super(mi);
			this.pattern = pattern;
		}
		public boolean matches(Method m, Class targetClass) {
			boolean run = m.getName().indexOf(pattern) != -1;
			if (run) ++count;
			return run;
		}

	}


	/**
	 * Note that trapping the Invocation as in previous version of this test
	 * isn't safe, as invocations may be reused
	 * and hence cleared at the end of each invocation.
	 * So we trap only the targe.
	 */
	protected static class TrapTargetInterceptor implements MethodInterceptor {

		public Object target;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			this.target = invocation.getThis();
			return invocation.proceed();
		}
	}

	protected abstract static class ContextTestBean extends TestBean {

		public String getName() {
			MethodInvocation invocation = AopContext.currentInvocation();
			assertions(invocation);
			return super.getName();
		}

		public void absquatulate() {
			MethodInvocation invocation = AopContext.currentInvocation();
			assertions(invocation);
			super.absquatulate();
		}
		
		protected abstract void assertions(MethodInvocation invocation);
	}

	public static class EqualsTestBean extends TestBean {

		public ITestBean getSpouse() {
			return this;
		}
	};
	
	
	public static class AllInstancesAreEqual implements IOther {
		public boolean equals(Object o) {
			return o instanceof AllInstancesAreEqual;
		}
		/**
		 * @see org.springframework.beans.IOther#absquatulate()
		 */
		public void absquatulate() {
		}
	}

	/*
	public void testEquals() {
		IOther a = new AllInstancesAreEqual();
		IOther b = new AllInstancesAreEqual();
		NopInterceptor i = new NopInterceptor();
		ProxyFactory pfa = new ProxyFactory(a);
		pfa.addInterceptor(i);
		ProxyFactory pfb = new ProxyFactory(b);
		IOther proxyA = (IOther) createProxy(pfa);
		IOther proxyB = (IOther) createProxy(pfb);
		
		assertTrue(a.equals(b));
		assertTrue(proxyA.equals(proxyB));
		assertTrue(a.equals(proxyA));
		assertFalse(proxyA.equals(a));
		
		// Equality checks were handled by the proxy
		assertEquals(0, i.getCount());
	}
	*/
	

}
