package org.springframework.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;

/**
 * @author Rob Harrop
 */
public class ReflectionUtilsTests extends TestCase {

	public void testInvokeMethod() throws Exception {
		String rob = "Rob Harrop";
		String juergen = "Juergen Hoeller";

		TestBean bean = new TestBean();
		bean.setName(rob);

		Method getName = TestBean.class.getMethod("getName", null);
		Method setName = TestBean.class.getMethod("setName", new Class[]{String.class});

		Object name = ReflectionUtils.invokeMethod(getName, bean);
		assertEquals("Incorrect name returned", rob, name);

		ReflectionUtils.invokeMethod(setName, bean, new Object[]{juergen});
		assertEquals("Incorrect name set", juergen, bean.getName());
	}
	
	public void testCopySrcToDestinationOfIncorrectClass() {
		TestBean src = new TestBean();
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	
	public void testRejectsNullSrc() {
		TestBean src = null;
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testRejectsNullDest() {
		TestBean src = new TestBean();
		String dest = null;
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testValidCopy() {
		TestBean src = new TestBean();
		TestBean dest = new TestBean();
		testValidCopy(src, dest);
	}
	
	public static class TestBeanSubclassWithNewField extends TestBean {
		private int magic;
		protected String prot = "foo";
	}
	
	public static class TestBeanSubclassWithFinalField extends TestBean {
		private final String foo = "will break naive copy that doesn't exclude statics";
	}
	
	public void testValidCopyOnSubTypeWithNewField() {
		TestBeanSubclassWithNewField src = new TestBeanSubclassWithNewField();
		TestBeanSubclassWithNewField dest = new TestBeanSubclassWithNewField();
		src.magic = 11;
		
		// Will check inherited fields are copied
		testValidCopy(src, dest);
		
		// Check subclass fields were copied
		assertEquals(src.magic, dest.magic);
		assertEquals(src.prot, dest.prot);
	}
	
	public void testValidCopyToSubType() {
		TestBean src = new TestBean();
		TestBeanSubclassWithNewField dest = new TestBeanSubclassWithNewField();
		dest.magic = 11;
		testValidCopy(src, dest);
		// Should have left this one alone
		assertEquals(11, dest.magic);
	}
	
	public void testValidCopyToSubTypeWithFinalField() {
		TestBeanSubclassWithFinalField src = new TestBeanSubclassWithFinalField();
		TestBeanSubclassWithFinalField dest = new TestBeanSubclassWithFinalField();
		// Check that this doesn't fail due to attempt to assign final
		testValidCopy(src, dest);
	}
	
	private void testValidCopy(TestBean src, TestBean dest) {
		src.setName("freddie");
		src.setAge(15);
		src.setSpouse(new TestBean());
		assertFalse(src.getAge() == dest.getAge());
		
		ReflectionUtils.shallowCopyFieldState(src, dest);
		assertEquals(src.getAge(), dest.getAge());
		assertEquals(src.getSpouse(), dest.getSpouse());
		assertEquals(src.getDoctor(), dest.getDoctor());
	}
	
	static class ListSavingMethodCallback implements ReflectionUtils.MethodCallback {
		private List methodNames = new LinkedList();
		private List methods = new LinkedList();
		
		public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
			methodNames.add(m.getName());
			methods.add(m);
		}
		
		public List getMethodNames() {
			return methodNames;
		}
		
		public List getMethods() {
			return methods;
		}
	};


	public void testDoWithProtectedMethods() {
		ListSavingMethodCallback mc = new ListSavingMethodCallback();
		ReflectionUtils.doWithMethods(TestBean.class, mc, 
				new ReflectionUtils.MethodFilter() {
					public boolean matches(Method m) {
						return Modifier.isProtected(m.getModifiers());
					}
		});
		assertFalse(mc.getMethodNames().isEmpty());
		assertTrue("Must find protected method on Object", mc.getMethodNames().contains("clone"));
		assertTrue("Must find protected method on Object", mc.getMethodNames().contains("finalize"));
		assertFalse("Public, not protected", mc.getMethodNames().contains("hashCode"));
		assertFalse("Public, not protected", mc.getMethodNames().contains("absquatulate"));
	}
	
	public static class TestBeanSubclass extends TestBean {
		public void absquatulate() {
			throw new UnsupportedOperationException();
		}
	}
	
	public void testDuplicatesFound() {
		ListSavingMethodCallback mc = new ListSavingMethodCallback();
		ReflectionUtils.doWithMethods(TestBeanSubclass.class, mc, ReflectionUtils.DECLARED_METHODS);
		int absquatulateCount = 0;
		for (Iterator it = mc.getMethodNames().iterator(); it.hasNext() ;) {
			String name = (String) it.next();
			if (name.equals("absquatulate")) {
				++absquatulateCount;
			}
		}
		assertEquals("Found 2 absquatulates", 2, absquatulateCount);
	}
}
