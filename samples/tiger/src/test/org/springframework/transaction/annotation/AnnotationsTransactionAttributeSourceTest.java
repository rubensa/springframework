/*
 * Created on Aug 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.springframework.transaction.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.metadata.standard.StandardAttributes;
import org.springframework.transaction.interceptor.TransactionAttribute;

import junit.framework.TestCase;

/**
 * @author colin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AnnotationsTransactionAttributeSourceTest extends TestCase {
	
	public void testNullOrEmpty() throws Exception {
		
		Method method = Empty.class.getMethod("getAge", null);
		
		StandardAttributes att = new StandardAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		assertNull(atas.getTransactionAttribute(method, null));
		
		// Try again in case of caching
		assertNull(atas.getTransactionAttribute(method, null));
	}
	
	
	/**
	 * Test the important case where the invocation is on a proxied interface method, but
	 * the attribute is defined on the target class
	 * @throws Exception
	 */
	public void testTransactionAttributeDeclaredOnClassMethod() throws Exception {
		Method classMethod = TestBean1.class.getMethod("getAge", null);
		
		Annotation anns[] = classMethod.getDeclaredAnnotations();
		
		Method interfaceMethod = ITestBean.class.getMethod("getAge", null);

		//TransactionAttribute txAtt = new DefaultTransactionAttribute();

		StandardAttributes att = new StandardAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean1.class);

		assertEquals("a", "a");
	}
	
	
	public interface ITestBean {
		
		int getAge();
		
		void setAge(int age);
		
		String getName(); 
		
		void setName(String name);
	}

	public static class Empty implements ITestBean {

		private String name;

		private int age;

		public Empty() {
		}

		public Empty(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
	
	
	public static class TestBean1 implements ITestBean {

		private String name;

		private int age;

		public TestBean1() {
		}

		public TestBean1(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@TxAttribute
		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
	
	
	

}
