package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;


/**
 * 
 * @author Rod Johnson
 * @since 02-Dec-02
 */
public class BeanFactoryBootstrapTests extends TestCase {
	
	Properties _savedProps;
	
	
	/**
	 * Constructor for BeanFactoryBootstrapTests.
	 * @param arg0
	 */
	public BeanFactoryBootstrapTests(String arg0) {
		super(arg0);
	}
	
	/** How to test many singletons? */
	public void testGetInstanceWithNullPropertiesFails() throws BeansException {
		System.setProperties(null);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with no system properties");
		}
		catch (BootstrapException ex) {
			// OK
		}
	}
	
	public void testGetInstanceWithUnknownBeanFactoryClassFails() throws BeansException {
		System.setProperties(null);
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class", 
		"org.springframework.beans.factory.support.xxxxXmlBeanFactory");
		
		System.setProperties(p);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with invalid class");
		}
		catch (BootstrapException ex) {
			// OK
		}
	}
	
	public void testGetInstanceWithMistypedBeanFactoryClassFails() throws BeansException {
		System.setProperties(null);
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class", 
		"java.awt.Point");
		
		System.setProperties(p);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with mistyped class");
		}
		catch (BootstrapException ex) {
			// OK
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
//	public void testXmlBeanFactory() throws Exception {
//		Properties p = new Properties();
//		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class", 
//		"XmlBeanFactory");
//		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".url", 
//		"c:/checkouts/book/framework/src/org/springframework/beans/factory/support/bs.xml");
//		
//		
//		System.setProperties(p);
//		System.getProperties().list(System.out);
//		
//		BeanFactoryBootstrap.reinitialize();
//
//		try {
//			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
//			
//			BeanFactory bf1 = BeanFactoryBootstrap.getInstance().getBeanFactory();
//			BeanFactory bf2 = BeanFactoryBootstrap.getInstance().getBeanFactory();
//			assertTrue("Two instances identical", bf1==bf2);
//			
//			System.out.println("Got bean factory");
//			assertNotNull("Bsb instance is not null", bsb);
//			TestBean tb = (TestBean) bsb.getBeanFactory().getBean("test");
//			assertNotNull("Test bean is not null", tb);
//			System.out.println(tb);
//			assertTrue("Property set", tb.getFoo().equals("bar"));
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
//	}
	
	
	public void testDummyBeanFactory() throws Exception {
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class", 
		"org.springframework.beans.factory.support.BeanFactoryBootstrapTests$DummyBeanFactory");
		
		
		System.setProperties(p);
		System.getProperties().list(System.out);
		
		BeanFactoryBootstrap.reinitialize();

		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			System.out.println("Got bean factory");
			assertNotNull("Bsb instance is not null", bsb);
			assertTrue("Is dummy", bsb.getBeanFactory() instanceof DummyBeanFactory);
			TestBean tb = (TestBean) bsb.getBeanFactory().getBean("test");
			assertNotNull("Test bean is not null", tb);
			System.out.println(tb);
			//assertTrue("Property set", tb.getFoo().equals("bar"));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public static class DummyBeanFactory implements BeanFactory {
		
		public Map m = new HashMap();
		
		 {
			m.put("test", new TestBean());
			m.put("s", new String());
		}
		
		public Object getBean(String name) throws BeansException {
			Object bean = m.get(name);
			if (bean == null)
				throw new NoSuchBeanDefinitionException(name, "no message");
			return bean;
		}

		public Object getBean(String name, Class requiredType) throws BeansException {
			return getBean(name);
		}

		public boolean containsBean(String name) throws BeansException {
			return m.containsKey(name);
		}

		public boolean isSingleton(String name) {
			return true;
		}

		/**
		 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang.String)
		 */
		public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
			throw new UnsupportedOperationException("getAliases");
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// save and restore System properties, which get destroyed for the tests
		_savedProps = System.getProperties();		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		System.setProperties(_savedProps);
	}

}
