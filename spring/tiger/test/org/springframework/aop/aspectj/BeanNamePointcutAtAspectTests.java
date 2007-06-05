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
package org.springframework.aop.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test for correct application of the bean() PCD for &#64;AspectJ-based aspects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class BeanNamePointcutAtAspectTests extends AbstractDependencyInjectionSpringContextTests {
	protected ITestBean testBean1;
	protected ITestBean testBean2;	
	protected CounterAspect counterAspect;
	
	public BeanNamePointcutAtAspectTests() {
		setPopulateProtectedVariables(true);
	}
	
	protected String getConfigPath() {
		return "bean-name-pointcut-atAspect-tests.xml";
	}
	
	protected void onSetUp() throws Exception {
		counterAspect.count = 0;
		super.onSetUp();
	}
	
	public void testMatchingBeanName() {
		assertTrue("Expected a proxy", testBean1 instanceof Advised);
		testBean1.setAge(20);
		assertEquals(1, counterAspect.count);
	}

	public void testNonMatchingBeanName() {
		assertFalse("Didn't expect a proxy", testBean2 instanceof Advised);
		testBean2.setAge(20);
		assertEquals(0, counterAspect.count);
	}
	
	public void testProgrammaticProxyCreattion() {
		ITestBean testBean = new TestBean();

		AspectJProxyFactory factory = new AspectJProxyFactory();
        factory.setTarget(testBean);

        CounterAspect myCounterAspect = new CounterAspect();
        factory.addAspect(myCounterAspect);

        ITestBean proxyTestBean = factory.getProxy();

		assertTrue("Expected a proxy", proxyTestBean instanceof Advised);
		proxyTestBean.setAge(20);
		assertEquals("Programmatically created proxy shouldn't match bean()", 0, myCounterAspect.count);
	}

	
	@Aspect
	public static class CounterAspect {
		int count;

		@Before("execution(* setAge(..)) && bean(testBean1)")
		public void increment1ForAnonymousPointcut() {
			count++;
		}

//		@Pointcut("execution(* setAge(..)) && bean(testBean1)")
//		public void testBean1SetAge() {}
//
//		@Pointcut("execution(* setAge(..)) && bean(testBean2)")
//		public void testBean2SetAge() {}
//		
//		@Before("testBean1SetAge()")
//		public void increment1() {
//			count++;
//		}
//
//		@Before("testBean2SetAge()")
//		public void increment2() {
//			count++;
//		}
	}
}
