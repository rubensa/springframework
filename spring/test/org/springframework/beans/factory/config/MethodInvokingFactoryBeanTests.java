package org.springframework.beans.factory.config;

import org.springframework.beans.FatalBeanException;

import junit.framework.TestCase;

/**
 * Tests MethodInvokingFactoryBean
 * 
 * @author colin sampaleanu
 * @since 2003-11-21
 * @version $Id$
 */
public class MethodInvokingFactoryBeanTests extends TestCase {

	public void testGetObject() throws Exception {

		// singleton, non-static
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		Integer i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, non-static
		tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.setSingleton(false);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

		// singleton, static
		String fqmn = TestClass1.class.getName() + ".staticMethod1";
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, static
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.afterPropertiesSet();
		mcfb.setSingleton(false);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

	}

	public void testGetObjectType() throws Exception {
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		assertTrue(mcfb.getObjectType().equals(int.class));
	}

	public void testAfterPropertiesSet() {

		String validationError = "improper validation of input properties";

		// assert that only static OR non static are set, but not both or none
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod("whatever");
		mcfb.setTarget(this);
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException e) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod("some.bogus.Method.name");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(TestClass1.class.getName() + ".method1");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// missing method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(this);
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// bogus method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(this);
		mcfb.setTargetMethod("bogus");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(TestClass1.class.getName() + ".staticMethod1");
		mcfb.afterPropertiesSet();

		// non-static method
		TestClass1 tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
	}

	// a test class to work with
	static class TestClass1 {

		public static int _staticField1;

		public int _field1 = 0;

		public int method1() {
			return ++_field1;
		}

		public static int staticMethod1() {
			return ++_staticField1;
		}
	}

}