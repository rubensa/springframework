/*
 * Created on Aug 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.springframework.transaction.annotations;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.springframework.metadata.annotations.AnnotationsAttributes;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Test for AnnotationsTransactionAttributesSource
 * 
 * @author colin
 */
public class AnnotationsTransactionAttributeSourceTest extends TestCase {
	
	public void testNullOrEmpty() throws Exception {
		
		Method method = Empty.class.getMethod("getAge", (Class[]) null);
		
		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		assertNull(atas.getTransactionAttribute(method, null));
		
		// Try again in case of caching
		assertNull(atas.getTransactionAttribute(method, null));
	}
	
	
	/**
	 * Test the important case where the invocation is on a proxied interface method, but
	 * the attribute is defined on the target class
	 */
	public void testTransactionAttributeDeclaredOnClassMethod() throws Exception {
		Method classMethod = TestBean1.class.getMethod("getAge", (Class[]) null);
		
		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(classMethod, TestBean1.class);
		
		RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
		rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
	}
	
    /**
     * Test case where attribute is on the interface method
     */
	public void testTransactionAttributeDeclaredOnInterfaceMethodOnly() throws Exception {
		Method interfaceMethod = ITestBean2.class.getMethod("getAge", (Class[]) null);

		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean2.class);
		
		RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
	}
	
	/**
	 * Test that when an attribute exists on both class and interface, class takes precedence
	 */
	public void testTransactionAttributeDeclaredOnTargetClassMethodTakesPrecedenceOverAttributeDeclaredOnInterfaceMethod() throws Exception {
		Method classMethod = TestBean3.class.getMethod("getAge", (Class[]) null);
		Method interfaceMethod = ITestBean3.class.getMethod("getAge", (Class[]) null);

		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean3.class);
		
		RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
		rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
		rbta.getRollbackRules().add(new NoRollbackRuleAttribute(IOException.class));
		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
	}
	

	public void testRollbackRulesAreApplied() throws Exception {
		Method method = TestBean3.class.getMethod("getAge", (Class[]) null);
		
		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(method, TestBean3.class);

		RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
		rbta.getRollbackRules().add(new RollbackRuleAttribute("java.lang.Exception"));
		rbta.getRollbackRules().add(new NoRollbackRuleAttribute(IOException.class));

		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
		assertTrue(actual.rollbackOn(new Exception()));
		assertFalse(actual.rollbackOn(new IOException()));
		
		actual = atas.getTransactionAttribute(method, method.getDeclaringClass());

		rbta = new RuleBasedTransactionAttribute();
		rbta.getRollbackRules().add(new RollbackRuleAttribute("java.lang.Exception"));
		rbta.getRollbackRules().add(new NoRollbackRuleAttribute(IOException.class));
		
		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
		assertTrue(actual.rollbackOn(new Exception()));
		assertFalse(actual.rollbackOn(new IOException()));
	}
	
	
	// helper
	void assertRulesAreEqual(RuleBasedTransactionAttribute rule1,
			RuleBasedTransactionAttribute rule2) throws Exception {
		
		if (rule1.getIsolationLevel() != rule2.getIsolationLevel())
			throw new Exception("isolaation level does not match");
		if (rule1.getPropagationBehavior() != rule2.getPropagationBehavior())
			throw new Exception("propagation behaviour does not match");
		
		List rules1 = rule1.getRollbackRules();
		List rules2 = rule2.getRollbackRules();
		List intersect = ListUtils.intersection(rules1, rules2);
		if (rules1.size() != rules2.size() || intersect.size() != rules1.size())
			throw new Exception("rules have different sized lists of rollback rules");
	}
	
	/**
	 * Test that transaction attribute is inherited from class
	 * if not specified on method
	 * @throws Exception
	 */
	public void testDefaultsToClassTransactionAttribute() throws Exception {

		Method method = TestBean4.class.getMethod("getAge", (Class[]) null);
		AnnotationsAttributes att = new AnnotationsAttributes();
		AnnotationsTransactionAttributeSource atas = new AnnotationsTransactionAttributeSource(att);
		TransactionAttribute actual = atas.getTransactionAttribute(method, TestBean4.class);
		
		RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
		rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
		rbta.getRollbackRules().add(new NoRollbackRuleAttribute(IOException.class));
		assertRulesAreEqual(rbta, (RuleBasedTransactionAttribute) actual);
	}

	
	public interface ITestBean {
		
		int getAge();
		
		void setAge(int age);
		
		String getName(); 
		
		void setName(String name);
	}
	
	public interface ITestBean2 {
		
		@TxAttribute
		int getAge();
		
		void setAge(int age);
		
		String getName(); 
		
		void setName(String name);
	}

	public interface ITestBean3 {
		
		@TxAttribute()
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

		@TxAttribute(rollbackFor=Exception.class)	
		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

	public static class TestBean2 implements ITestBean2 {

		private String name;

		private int age;

		public TestBean2() {
		}

		public TestBean2(String name, int age) {
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

	public static class TestBean3 implements ITestBean3 {

		private String name;

		private int age;

		public TestBean3() {
		}

		public TestBean3(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@TxAttribute(rollbackFor=Exception.class, noRollbackFor={IOException.class})
		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}


	@TxAttribute(rollbackFor=Exception.class, noRollbackFor={IOException.class})
	public static class TestBean4 implements ITestBean3 {

		private String name;

		private int age;

		public TestBean4() {
		}

		public TestBean4(String name, int age) {
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

}
