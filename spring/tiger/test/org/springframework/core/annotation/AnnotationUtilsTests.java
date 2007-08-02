/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.core.annotation;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotationDeclaringClass;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.isAnnotationDeclaredLocally;
import static org.springframework.core.annotation.AnnotationUtils.isAnnotationInherited;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public class AnnotationUtilsTests extends TestCase {

	public void testFindMethodAnnotationOnLeaf() throws SecurityException, NoSuchMethodException {

		final Method m = Leaf.class.getMethod("annotatedOnLeaf", (Class[]) null);
		assertNotNull(m.getAnnotation(Order.class));
		assertNotNull(getAnnotation(m, Order.class));
		assertNotNull(findAnnotation(m, Order.class));
	}

	public void testFindMethodAnnotationOnRoot() throws SecurityException, NoSuchMethodException {

		final Method m = Leaf.class.getMethod("annotatedOnRoot", (Class[]) null);
		assertNotNull(m.getAnnotation(Order.class));
		assertNotNull(getAnnotation(m, Order.class));
		assertNotNull(findAnnotation(m, Order.class));
	}

	public void testFindMethodAnnotationOnRootButOverridden() throws SecurityException, NoSuchMethodException {

		final Method m = Leaf.class.getMethod("overrideWithoutNewAnnotation", (Class[]) null);
		assertNull(m.getAnnotation(Order.class));
		assertNull(getAnnotation(m, Order.class));
		assertNotNull(findAnnotation(m, Order.class));
	}

	public void testFindMethodAnnotationNotAnnotated() throws SecurityException, NoSuchMethodException {

		final Method m = Leaf.class.getMethod("notAnnotated", (Class[]) null);
		assertNull(findAnnotation(m, Order.class));
	}

	public void testFindMethodAnnotationOnBridgeMethod() throws Exception {

		final Method m = SimpleFoo.class.getMethod("something", Object.class);
		assertTrue(m.isBridge());
		assertNull(m.getAnnotation(Order.class));
		assertNull(getAnnotation(m, Order.class));
		assertNotNull(findAnnotation(m, Order.class));
		assertNull(m.getAnnotation(Transactional.class));
		assertNotNull(getAnnotation(m, Transactional.class));
		assertNotNull(findAnnotation(m, Transactional.class));
	}

	// TODO consider whether we want this to handle annotations on interfaces
	// public void testFindMethodAnnotationFromInterfaceImplementedByRoot()
	// throws Exception {
	// Method m = Leaf.class.getMethod("fromInterfaceImplementedByRoot",
	// (Class[]) null);
	// Order o = findAnnotation(Order.class, m, Leaf.class);
	// assertNotNull(o);
	// }

	public void testFindAnnotationDeclaringClass() throws Exception {

		// no class-level annotation
		assertNull(findAnnotationDeclaringClass(Transactional.class, NonAnnotatedInterface.class));
		assertNull(findAnnotationDeclaringClass(Transactional.class, NonAnnotatedClass.class));

		// inherited class-level annotation; note: @Transactional is inherited
		assertEquals(InheritedAnnotationInterface.class, findAnnotationDeclaringClass(Transactional.class,
				InheritedAnnotationInterface.class));
		assertNull(findAnnotationDeclaringClass(Transactional.class, SubInheritedAnnotationInterface.class));
		assertEquals(InheritedAnnotationClass.class, findAnnotationDeclaringClass(Transactional.class,
				InheritedAnnotationClass.class));
		assertEquals(InheritedAnnotationClass.class, findAnnotationDeclaringClass(Transactional.class,
				SubInheritedAnnotationClass.class));

		// non-inherited class-level annotation; note: @Order is not inherited,
		// but findAnnotationDeclaringClass() should still find it.
		assertEquals(NonInheritedAnnotationInterface.class, findAnnotationDeclaringClass(Order.class,
				NonInheritedAnnotationInterface.class));
		assertNull(findAnnotationDeclaringClass(Order.class, SubNonInheritedAnnotationInterface.class));
		assertEquals(NonInheritedAnnotationClass.class, findAnnotationDeclaringClass(Order.class,
				NonInheritedAnnotationClass.class));
		assertEquals(NonInheritedAnnotationClass.class, findAnnotationDeclaringClass(Order.class,
				SubNonInheritedAnnotationClass.class));
	}

	public void testIsAnnotationDeclaredLocally() throws Exception {

		// no class-level annotation
		assertFalse(isAnnotationDeclaredLocally(Transactional.class, NonAnnotatedInterface.class));
		assertFalse(isAnnotationDeclaredLocally(Transactional.class, NonAnnotatedClass.class));

		// inherited class-level annotation; note: @Transactional is inherited
		assertTrue(isAnnotationDeclaredLocally(Transactional.class, InheritedAnnotationInterface.class));
		assertFalse(isAnnotationDeclaredLocally(Transactional.class, SubInheritedAnnotationInterface.class));
		assertTrue(isAnnotationDeclaredLocally(Transactional.class, InheritedAnnotationClass.class));
		assertFalse(isAnnotationDeclaredLocally(Transactional.class, SubInheritedAnnotationClass.class));

		// non-inherited class-level annotation; note: @Order is not inherited
		assertTrue(isAnnotationDeclaredLocally(Order.class, NonInheritedAnnotationInterface.class));
		assertFalse(isAnnotationDeclaredLocally(Order.class, SubNonInheritedAnnotationInterface.class));
		assertTrue(isAnnotationDeclaredLocally(Order.class, NonInheritedAnnotationClass.class));
		assertFalse(isAnnotationDeclaredLocally(Order.class, SubNonInheritedAnnotationClass.class));
	}

	public void testIsAnnotationInherited() throws Exception {

		// no class-level annotation
		assertFalse(isAnnotationInherited(Transactional.class, NonAnnotatedInterface.class));
		assertFalse(isAnnotationInherited(Transactional.class, NonAnnotatedClass.class));

		// inherited class-level annotation; note: @Transactional is inherited
		assertFalse(isAnnotationInherited(Transactional.class, InheritedAnnotationInterface.class));
		// isAnnotationInherited() does not currently traverse interface
		// hierarchies. Thus the following, though perhaps counter intuitive,
		// must be false:
		assertFalse(isAnnotationInherited(Transactional.class, SubInheritedAnnotationInterface.class));
		assertFalse(isAnnotationInherited(Transactional.class, InheritedAnnotationClass.class));
		assertTrue(isAnnotationInherited(Transactional.class, SubInheritedAnnotationClass.class));

		// non-inherited class-level annotation; note: @Order is not inherited
		assertFalse(isAnnotationInherited(Order.class, NonInheritedAnnotationInterface.class));
		assertFalse(isAnnotationInherited(Order.class, SubNonInheritedAnnotationInterface.class));
		assertFalse(isAnnotationInherited(Order.class, NonInheritedAnnotationClass.class));
		assertFalse(isAnnotationInherited(Order.class, SubNonInheritedAnnotationClass.class));
	}

	public static interface AnnotatedInterface {

		@Order(0)
		void fromInterfaceImplementedByRoot();
	}

	public static class Root implements AnnotatedInterface {

		@Order(27)
		public void annotatedOnRoot() {

		}

		public void overrideToAnnotate() {

		}

		@Order(27)
		public void overrideWithoutNewAnnotation() {

		}

		public void notAnnotated() {

		}

		public void fromInterfaceImplementedByRoot() {

		}
	}

	public static class Leaf extends Root {

		@Order(25)
		public void annotatedOnLeaf() {

		}

		@Override
		@Order(1)
		public void overrideToAnnotate() {

		}

		@Override
		public void overrideWithoutNewAnnotation() {

		}
	}

	public static abstract class Foo<T> {

		@Order(1)
		public abstract void something(T arg);
	}

	public static class SimpleFoo extends Foo<String> {

		@Transactional
		public void something(final String arg) {

		}
	}

	@Transactional
	public static interface InheritedAnnotationInterface {
	}

	public static interface SubInheritedAnnotationInterface extends InheritedAnnotationInterface {
	}

	@Order
	public static interface NonInheritedAnnotationInterface {
	}

	public static interface SubNonInheritedAnnotationInterface extends NonInheritedAnnotationInterface {
	}

	public static class NonAnnotatedClass {
	}

	public static interface NonAnnotatedInterface {
	}

	@Transactional
	public static class InheritedAnnotationClass {
	}

	public static class SubInheritedAnnotationClass extends InheritedAnnotationClass {
	}

	@Order
	public static class NonInheritedAnnotationClass {
	}

	public static class SubNonInheritedAnnotationClass extends NonInheritedAnnotationClass {
	}

}
