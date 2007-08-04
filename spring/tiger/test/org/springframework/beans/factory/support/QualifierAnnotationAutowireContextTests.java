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

package org.springframework.beans.factory.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * @author Mark Fisher
 */
public class QualifierAnnotationAutowireContextTests extends TestCase {
	
	private static final String JUERGEN = "juergen";

	private static final String MARK = "mark";


	public void testAutowiredFieldWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredMethodParameterWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredConstructorArgumentWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e instanceof UnsatisfiedDependencyException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredFieldWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(TestQualifier.class.getName());
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldTestBean bean = (QualifiedFieldTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredMethodParameterWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(TestQualifier.class.getName());
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean = 
				(QualifiedMethodParameterTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredConstructorArgumentWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(TestQualifier.class.getName());
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentTestBean bean = 
				(QualifiedConstructorArgumentTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredFieldWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredMethodParameterWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredConstructorArgumentWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e instanceof UnsatisfiedDependencyException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredFieldResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(TestQualifier.class.getName());
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldTestBean bean = (QualifiedFieldTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredMethodParameterResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(TestQualifier.class.getName());
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean = 
				(QualifiedMethodParameterTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredConstructorArgumentResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(TestQualifier.class.getName());
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentTestBean bean = 
				(QualifiedConstructorArgumentTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredFieldResolvesQualifiedCandidateWithDefaultValueAndNoValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, but includes no value
		person1.addQualifier(TestQualifierWithDefaultValue.class.getName());
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithDefaultValueTestBean bean = 
				(QualifiedFieldWithDefaultValueTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredFieldDoesNotResolveCandidateWithDefaultValueAndConflictingValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, and non-default value specified
		person1.addQualifier(TestQualifierWithDefaultValue.class.getName(), "not the default");
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredFieldResolvesWithDefaultValueAndExplicitDefaultValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, and value matches the default
		person1.addQualifier(TestQualifierWithDefaultValue.class.getName(), "default");
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithDefaultValueTestBean bean = 
				(QualifiedFieldWithDefaultValueTestBean) context.getBean("autowired");
		assertEquals(JUERGEN, bean.getPerson().getName());
	}

	public void testAutowiredFieldResolvesWithMultipleQualifierValues() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		Map qualifierValues1 = new HashMap();
		qualifierValues1.put("number", 456);
		person1.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues1);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		Map qualifierValues2 = new HashMap();
		qualifierValues2.put("number", 123);
		person2.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithMultipleAttributesTestBean bean = 
				(QualifiedFieldWithMultipleAttributesTestBean) context.getBean("autowired");
		assertEquals(MARK, bean.getPerson().getName());
	}

	public void testAutowiredFieldDoesNotResolveWithMultipleQualifierValuesAndConflictingDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		Map qualifierValues1 = new HashMap();
		qualifierValues1.put("number", 456);
		person1.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues1);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		Map qualifierValues2 = new HashMap();
		qualifierValues2.put("number", 123);
		qualifierValues2.put("value", "not the default");
		person2.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredFieldResolvesWithMultipleQualifierValuesAndExplicitDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		Map qualifierValues1 = new HashMap();
		qualifierValues1.put("number", 456);
		person1.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues1);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		Map qualifierValues2 = new HashMap();
		qualifierValues2.put("number", 123);
		qualifierValues2.put("value", "default");
		person2.addQualifier(ClassUtils.getShortName(TestQualifierWithMultipleAttributes.class), qualifierValues2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithMultipleAttributesTestBean bean = 
				(QualifiedFieldWithMultipleAttributesTestBean) context.getBean("autowired");
		assertEquals(MARK, bean.getPerson().getName());
	}

	public void testAutowiredFieldDoesNotResolveWithMultipleQualifierValuesAndMultipleMatchingCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		Map qualifierValues1 = new HashMap();
		qualifierValues1.put("number", 123);
		person1.addQualifier(TestQualifierWithMultipleAttributes.class.getName(), qualifierValues1);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		Map qualifierValues2 = new HashMap();
		qualifierValues2.put("number", 123);
		qualifierValues2.put("value", "default");
		person2.addQualifier(ClassUtils.getShortName(TestQualifierWithMultipleAttributes.class), qualifierValues2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getRootCause() instanceof NoSuchBeanDefinitionException);
			assertEquals("autowired", e.getBeanName());
		}
	}

	public void testAutowiredFieldResolvesWithBaseQualifierAndDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(Qualifier.class.getName());
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedFieldWithBaseQualifierDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithBaseQualifierDefaultValueTestBean bean = 
				(QualifiedFieldWithBaseQualifierDefaultValueTestBean) context.getBean("autowired");
		assertEquals(MARK, bean.getPerson().getName());
	}

	public void testAutowiredFieldResolvesWithBaseQualifierAndNonDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue("the real juergen");
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(Qualifier.class.getName(), "juergen");
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue("juergen imposter");
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(Qualifier.class.getName(), "not really juergen");
		context.registerBeanDefinition("juergen1", person1);
		context.registerBeanDefinition("juergen2", person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean bean = 
				(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean) context.getBean("autowired");
		assertEquals("the real juergen", bean.getPerson().getName());
	}

	public void testAutowiredFieldDoesNotResolveWithBaseQualifierAndNonDefaultValueAndMultipleMatchingCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue("the real juergen");
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(Qualifier.class.getName(), "juergen");
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue("juergen imposter");
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(Qualifier.class.getName(), "juergen");
		context.registerBeanDefinition("juergen1", person1);
		context.registerBeanDefinition("juergen2", person2);
		context.registerBeanDefinition("autowired", 
				new RootBeanDefinition(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		try {
			context.refresh();
			fail("expected BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e instanceof UnsatisfiedDependencyException);
			assertEquals("autowired", e.getBeanName());
		}
	}


	private static class QualifiedFieldTestBean {

		@Autowired
		@TestQualifier
		private Person person;
		
		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedMethodParameterTestBean {

		private Person person;
		
		@Autowired
		public void setPerson(@TestQualifier Person person) {
			this.person = person;
		}

		public Person getPerson() {
			return this.person;
		}
	}
	
	
	private static class QualifiedConstructorArgumentTestBean {

		private Person person;
		
		@Autowired
		public QualifiedConstructorArgumentTestBean(@TestQualifier Person person) {
			this.person = person;
		}
		
		public Person getPerson() {
			return this.person;
		}

	}


	public static class QualifiedFieldWithDefaultValueTestBean {

		@Autowired
		@TestQualifierWithDefaultValue
		private Person person;
		
		public Person getPerson() {
			return this.person;
		}
	}


	public static class QualifiedFieldWithMultipleAttributesTestBean {

		@Autowired
		@TestQualifierWithMultipleAttributes(number=123)
		private Person person;
		
		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedFieldWithBaseQualifierDefaultValueTestBean {

		@Autowired
		@Qualifier
		private Person person;
		
		public Person getPerson() {
			return this.person;
		}
	}


	public static class QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean {

		private Person person;

		@Autowired
		public QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean(
				@Qualifier("juergen") Person person) {
			this.person = person;
		}
		
		public Person getPerson() {
			return this.person;
		}
	}


	private static class Person {
		
		private String name;
		
		public Person(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}

	}


	@Target({ElementType.FIELD, ElementType.PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public static @interface TestQualifier {
	}


	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public static @interface TestQualifierWithDefaultValue {

		String value() default "default";

	}


	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public static @interface TestQualifierWithMultipleAttributes {

		String value() default "default";

		int number();

	}

}
