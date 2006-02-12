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

package org.springframework.validation;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.springframework.beans.BeanWithObjectProperty;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.SerializablePerson;
import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.util.StringUtils;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public class DataBinderTests extends TestCase {

	public void testBindingNoErrors() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		assertTrue(binder.isIgnoreUnknownFields());
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", new Integer(32)));
		pvs.addPropertyValue(new PropertyValue("nonExisting", "someValue"));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", rod.getName().equals("Rod"));
		assertTrue("changed age correctly", rod.getAge() == 32);

		Map m = binder.getBindingResult().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("person");
		assertTrue("Same object", tb.equals(rod));
	}

	public void testBindingNoErrorsNotIgnoreUnknown() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setIgnoreUnknownFields(false);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", new Integer(32)));
		pvs.addPropertyValue(new PropertyValue("nonExisting", "someValue"));

		try {
			binder.bind(pvs);
			fail("Should have thrown NotWritablePropertyException");
		}
		catch (NotWritablePropertyException ex) {
			// expected
		}
	}

	public void testBindingWithErrors() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		pvs.addPropertyValue(new PropertyValue("touchy", "m.y"));
		binder.bind(pvs);

		try {
			binder.close();
			fail("Should have thrown BindException");
		}
		catch (BindException ex) {
			assertTrue("changed name correctly", rod.getName().equals("Rod"));
			//assertTrue("changed age correctly", rod.getAge() == 32);

			Map map = binder.getBindingResult().getModel();
			//assertTrue("There are 3 element in map", m.size() == 1);
			TestBean tb = (TestBean) map.get("person");
			assertTrue("Same object", tb.equals(rod));

			BindingResult br = (BindingResult) map.get(BindingResult.MODEL_KEY_PREFIX + "person");
			assertTrue("Added itself to map", br == binder.getBindingResult());
			assertTrue(br.hasErrors());
			assertTrue("Correct number of errors", br.getErrorCount() == 2);

			assertTrue("Has age errors", br.hasFieldErrors("age"));
			assertTrue("Correct number of age errors", br.getFieldErrorCount("age") == 1);
			assertEquals("32x", binder.getBindingResult().getFieldValue("age"));
			assertEquals("32x", binder.getBindingResult().getFieldError("age").getRejectedValue());
			assertEquals(0, tb.getAge());

			assertTrue("Has touchy errors", br.hasFieldErrors("touchy"));
			assertTrue("Correct number of touchy errors", br.getFieldErrorCount("touchy") == 1);
			assertEquals("m.y", binder.getBindingResult().getFieldValue("touchy"));
			assertEquals("m.y", binder.getBindingResult().getFieldError("touchy").getRejectedValue());
			assertNull(tb.getTouchy());
		}
	}

	public void testBindingWithErrorsAndCustomEditors() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.registerCustomEditor(String.class, "touchy", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix_" + text);
			}
			public String getAsText() {
				return getValue().toString().substring(7);
			}
		});
		binder.registerCustomEditor(TestBean.class, "spouse", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean(text, 0));
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		pvs.addPropertyValue(new PropertyValue("touchy", "m.y"));
		pvs.addPropertyValue(new PropertyValue("spouse", "Kerry"));
		binder.bind(pvs);

		try {
			binder.close();
			fail("Should have thrown BindException");
		}
		catch (BindException ex) {
			assertTrue("changed name correctly", rod.getName().equals("Rod"));
			//assertTrue("changed age correctly", rod.getAge() == 32);

			Map model = binder.getBindingResult().getModel();
			//assertTrue("There are 3 element in map", m.size() == 1);
			TestBean tb = (TestBean) model.get("person");
			assertTrue("Same object", tb.equals(rod));

			BindingResult br = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "person");
			assertTrue("Added itself to map", br == binder.getBindingResult());
			assertTrue(br.hasErrors());
			assertTrue("Correct number of errors", br.getErrorCount() == 2);

			assertTrue("Has age errors", br.hasFieldErrors("age"));
			assertTrue("Correct number of age errors", br.getFieldErrorCount("age") == 1);
			assertEquals("32x", binder.getBindingResult().getFieldValue("age"));
			assertEquals("32x", binder.getBindingResult().getFieldError("age").getRejectedValue());
			assertEquals(0, tb.getAge());

			assertTrue("Has touchy errors", br.hasFieldErrors("touchy"));
			assertTrue("Correct number of touchy errors", br.getFieldErrorCount("touchy") == 1);
			assertEquals("m.y", binder.getBindingResult().getFieldValue("touchy"));
			assertEquals("m.y", binder.getBindingResult().getFieldError("touchy").getRejectedValue());
			assertNull(tb.getTouchy());

			assertTrue("Does not have spouse errors", !br.hasFieldErrors("spouse"));
			assertEquals("Kerry", binder.getBindingResult().getFieldValue("spouse"));
			assertNotNull(tb.getSpouse());
		}
	}

	public void testBindingWithCustomEditorOnObjectField() {
		BeanWithObjectProperty tb = new BeanWithObjectProperty();
		DataBinder binder = new DataBinder(tb);
		binder.registerCustomEditor(Integer.class, "object", new CustomNumberEditor(Integer.class, true));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("object", "1"));
		binder.bind(pvs);
		assertEquals(new Integer(1), tb.getObject());
	}

	public void testBindingWithAllowedFields() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setAllowedFields(new String[]{"name", "myparam"});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", rod.getName().equals("Rod"));
		assertTrue("did not change age", rod.getAge() == 0);

		Map m = binder.getBindingResult().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("target");
		assertTrue("Same object", tb.equals(rod));
	}

	public void testBindingWithAllowedFieldsUsingAsterisks() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setAllowedFields(new String[]{"nam*", "*ouchy"});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("touchy", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", "Rod".equals(rod.getName()));
		assertTrue("changed touchy correctly", "Rod".equals(rod.getTouchy()));
		assertTrue("did not change age", rod.getAge() == 0);

		Map m = binder.getBindingResult().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("person");
		assertTrue("Same object", tb.equals(rod));
	}

	/**
	 * Tests for required field, both null, non-existing and empty strings.
	 */
	public void testBindingWithRequiredFields() throws Exception {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());

		DataBinder binder = new DataBinder(tb, "person");
		binder.setRequiredFields(new String[]{"touchy", "name", "age", "date", "spouse.name"});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("touchy", ""));
		pvs.addPropertyValue(new PropertyValue("name", null));
		pvs.addPropertyValue(new PropertyValue("age", null));
		pvs.addPropertyValue(new PropertyValue("spouse.name", "     "));

		binder.bind(pvs);

		BindingResult br = binder.getBindingResult();
		assertEquals("Wrong number of errors", 5, br.getErrorCount());

		assertEquals("required", br.getFieldError("touchy").getCode());
		assertEquals("", br.getFieldValue("touchy"));
		assertEquals("required", br.getFieldError("name").getCode());
		assertEquals("", br.getFieldValue("name"));
		assertEquals("required", br.getFieldError("age").getCode());
		assertEquals("", br.getFieldValue("age"));
		assertEquals("required", br.getFieldError("date").getCode());
		assertEquals("", br.getFieldValue("date"));
		assertEquals("required", br.getFieldError("spouse.name").getCode());
		assertEquals("", br.getFieldValue("spouse.name"));
	}

	public void testBindingWithNestedObjectCreation() throws Exception {
		TestBean tb = new TestBean();

		DataBinder binder = new DataBinder(tb, "person");
		binder.registerCustomEditor(ITestBean.class, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean());
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("spouse", "someValue"));
		pvs.addPropertyValue(new PropertyValue("spouse.name", "test"));
		binder.bind(pvs);

		assertNotNull(tb.getSpouse());
		assertEquals("test", tb.getSpouse().getName());
	}

	public void testCustomEditorForSingleProperty() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "value"));
		pvs.addPropertyValue(new PropertyValue("touchy", "value"));
		pvs.addPropertyValue(new PropertyValue("spouse.name", "sue"));
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("spouse.name", "someCode", "someMessage");

		assertEquals("", binder.getBindingResult().getNestedPath());
		assertEquals("value", binder.getBindingResult().getFieldValue("name"));
		assertEquals("prefixvalue", binder.getBindingResult().getFieldError("name").getRejectedValue());
		assertEquals("prefixvalue", tb.getName());
		assertEquals("value", binder.getBindingResult().getFieldValue("touchy"));
		assertEquals("value", binder.getBindingResult().getFieldError("touchy").getRejectedValue());
		assertEquals("value", tb.getTouchy());

		assertTrue(binder.getBindingResult().hasFieldErrors("spouse.*"));
		assertEquals(1, binder.getBindingResult().getFieldErrorCount("spouse.*"));
		assertEquals("spouse.name", binder.getBindingResult().getFieldError("spouse.*").getField());
	}

	public void testCustomEditorForPrimitiveProperty() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(int.class, "age", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new Integer(99));
			}
			public String getAsText() {
				return "argh";
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", ""));
		binder.bind(pvs);

		assertEquals("argh", binder.getBindingResult().getFieldValue("age"));
		assertEquals(99, tb.getAge());
	}

	public void testCustomEditorForAllStringProperties() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "value"));
		pvs.addPropertyValue(new PropertyValue("touchy", "value"));
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");

		assertEquals("value", binder.getBindingResult().getFieldValue("name"));
		assertEquals("prefixvalue", binder.getBindingResult().getFieldError("name").getRejectedValue());
		assertEquals("prefixvalue", tb.getName());
		assertEquals("value", binder.getBindingResult().getFieldValue("touchy"));
		assertEquals("prefixvalue", binder.getBindingResult().getFieldError("touchy").getRejectedValue());
		assertEquals("prefixvalue", tb.getTouchy());
	}

	public void testCustomEditorWithOldValueAccess() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				if (getValue() == null || !text.equalsIgnoreCase(getValue().toString())) {
					setValue(text);
				}
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "value"));
		binder.bind(pvs);
		assertEquals("value", tb.getName());

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "vaLue"));
		binder.bind(pvs);
		assertEquals("value", tb.getName());
	}

	public void testValidatorNoErrors() {
		TestBean tb = new TestBean();
		tb.setAge(33);
		tb.setName("Rod");
		try {
			tb.setTouchy("Rod");
		}
		catch (Exception e) {
			fail("Should not throw any Exception");
		}
		TestBean tb2 = new TestBean();
		tb2.setAge(34);
		tb.setSpouse(tb2);
		DataBinder db = new DataBinder(tb, "tb");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("spouse.age", "argh");
		db.bind(pvs);
		Errors errors = db.getBindingResult();
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);

		errors.setNestedPath("spouse");
		assertEquals("spouse.", errors.getNestedPath());
		assertEquals("argh", errors.getFieldValue("age"));
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);

		errors.setNestedPath("");
		assertEquals("", errors.getNestedPath());
		errors.pushNestedPath("spouse");
		assertEquals("spouse.", errors.getNestedPath());
		errors.pushNestedPath("spouse");
		assertEquals("spouse.spouse.", errors.getNestedPath());
		errors.popNestedPath();
		assertEquals("spouse.", errors.getNestedPath());
		errors.popNestedPath();
		assertEquals("", errors.getNestedPath());
		try {
			errors.popNestedPath();
		}
		catch (IllegalStateException ex) {
			// expected, because stack was empty
		}
		errors.pushNestedPath("spouse");
		assertEquals("spouse.", errors.getNestedPath());
		errors.setNestedPath("");
		assertEquals("", errors.getNestedPath());
		try {
			errors.popNestedPath();
		}
		catch (IllegalStateException ex) {
			// expected, because stack was reset by setNestedPath
		}

		errors.pushNestedPath("spouse");
		assertEquals("spouse.", errors.getNestedPath());

		assertEquals(1, errors.getErrorCount());
		assertTrue(!errors.hasGlobalErrors());
		assertEquals(1, errors.getFieldErrorCount("age"));
		assertTrue(!errors.hasFieldErrors("name"));
	}

	public void testValidatorWithErrors() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		Errors errors = new BeanBindingResult(tb, "tb");
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);
		errors.setNestedPath("spouse.");
		assertEquals("spouse.", errors.getNestedPath());
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);

		errors.setNestedPath("");
		assertEquals(6, errors.getErrorCount());
		assertEquals(2, errors.getGlobalErrorCount());
		assertEquals("NAME_TOUCHY_MISMATCH", errors.getGlobalError().getCode());
		assertEquals("NAME_TOUCHY_MISMATCH", ((ObjectError) errors.getGlobalErrors().get(0)).getCode());
		assertEquals("NAME_TOUCHY_MISMATCH.tb", ((ObjectError) errors.getGlobalErrors().get(0)).getCodes()[0]);
		assertEquals("NAME_TOUCHY_MISMATCH", ((ObjectError) errors.getGlobalErrors().get(0)).getCodes()[1]);
		assertEquals("tb", ((ObjectError) errors.getGlobalErrors().get(0)).getObjectName());
		assertEquals("GENERAL_ERROR", ((ObjectError) errors.getGlobalErrors().get(1)).getCode());
		assertEquals("GENERAL_ERROR.tb", ((ObjectError) errors.getGlobalErrors().get(1)).getCodes()[0]);
		assertEquals("GENERAL_ERROR", ((ObjectError) errors.getGlobalErrors().get(1)).getCodes()[1]);
		assertEquals("msg", ((ObjectError) errors.getGlobalErrors().get(1)).getDefaultMessage());
		assertEquals("arg", ((ObjectError) errors.getGlobalErrors().get(1)).getArguments()[0]);
		assertEquals(2, errors.getFieldErrorCount("age"));
		assertEquals("TOO_YOUNG", errors.getFieldError("age").getCode());
		assertEquals("TOO_YOUNG", ((FieldError) errors.getFieldErrors("age").get(0)).getCode());
		assertEquals("tb", ((FieldError) errors.getFieldErrors("age").get(0)).getObjectName());
		assertEquals("age", ((FieldError) errors.getFieldErrors("age").get(0)).getField());
		assertEquals(new Integer(0), ((FieldError) errors.getFieldErrors("age").get(0)).getRejectedValue());
		assertEquals("AGE_NOT_ODD", ((FieldError) errors.getFieldErrors("age").get(1)).getCode());
		assertEquals(1, errors.getFieldErrorCount("name"));
		assertEquals("NOT_ROD", errors.getFieldError("name").getCode());
		assertEquals("NOT_ROD.tb.name", errors.getFieldError("name").getCodes()[0]);
		assertEquals("NOT_ROD.name", errors.getFieldError("name").getCodes()[1]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("name").getCodes()[2]);
		assertEquals("NOT_ROD", errors.getFieldError("name").getCodes()[3]);
		assertEquals("name", ((FieldError) errors.getFieldErrors("name").get(0)).getField());
		assertEquals(null, ((FieldError) errors.getFieldErrors("name").get(0)).getRejectedValue());
		assertEquals(1, errors.getFieldErrorCount("spouse.age"));
		assertEquals("TOO_YOUNG", errors.getFieldError("spouse.age").getCode());
		assertEquals("tb", ((FieldError) errors.getFieldErrors("spouse.age").get(0)).getObjectName());
		assertEquals(new Integer(0), ((FieldError) errors.getFieldErrors("spouse.age").get(0)).getRejectedValue());
	}

	public void testBindingStringArrayToIntegerSet() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(Set.class, new CustomCollectionEditor(TreeSet.class) {
			protected Object convertElement(Object element) {
				return new Integer(element.toString());
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("set", new String[] {"10", "20", "30"});
		binder.bind(pvs);

		assertEquals(tb.getSet(), binder.getBindingResult().getFieldValue("set"));
		assertTrue(tb.getSet() instanceof TreeSet);
		assertEquals(3, tb.getSet().size());
		assertTrue(tb.getSet().contains(new Integer(10)));
		assertTrue(tb.getSet().contains(new Integer(20)));
		assertTrue(tb.getSet().contains(new Integer(30)));
	}

	public void testBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0].name", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1].name", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals(1, errors.getFieldErrorCount("array[0].name"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0].name").getCode());
		assertEquals("NOT_ROD.tb.array[0].name", errors.getFieldError("array[0].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array.name", errors.getFieldError("array[0].name").getCodes()[1]);
		assertEquals("NOT_ROD.array[0].name", errors.getFieldError("array[0].name").getCodes()[2]);
		assertEquals("NOT_ROD.array.name", errors.getFieldError("array[0].name").getCodes()[3]);
		assertEquals("NOT_ROD.name", errors.getFieldError("array[0].name").getCodes()[4]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("array[0].name").getCodes()[5]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0].name").getCodes()[6]);
		assertEquals(1, errors.getFieldErrorCount("map[key1].name"));
		assertEquals(1, errors.getFieldErrorCount("map['key1'].name"));
		assertEquals(1, errors.getFieldErrorCount("map[\"key1\"].name"));
		assertEquals("NOT_ROD", errors.getFieldError("map[key1].name").getCode());
		assertEquals("NOT_ROD.tb.map[key1].name", errors.getFieldError("map[key1].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.map.name", errors.getFieldError("map[key1].name").getCodes()[1]);
		assertEquals("NOT_ROD.map[key1].name", errors.getFieldError("map[key1].name").getCodes()[2]);
		assertEquals("NOT_ROD.map.name", errors.getFieldError("map[key1].name").getCodes()[3]);
		assertEquals("NOT_ROD.name", errors.getFieldError("map[key1].name").getCodes()[4]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("map[key1].name").getCodes()[5]);
		assertEquals("NOT_ROD", errors.getFieldError("map[key1].name").getCodes()[6]);
	}

	public void testBindingToNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.list[0].name", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0].nestedIndexedBean.list[0].name", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals(1, errors.getFieldErrorCount("array[0].nestedIndexedBean.list[0].name"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCode());
		assertEquals("NOT_ROD.tb.array[0].nestedIndexedBean.list[0].name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array[0].nestedIndexedBean.list.name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[1]);
		assertEquals("NOT_ROD.tb.array.nestedIndexedBean.list.name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[2]);
		assertEquals("NOT_ROD.array[0].nestedIndexedBean.list[0].name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[3]);
		assertEquals("NOT_ROD.array[0].nestedIndexedBean.list.name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[4]);
		assertEquals("NOT_ROD.array.nestedIndexedBean.list.name",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[5]);
		assertEquals("NOT_ROD.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[6]);
		assertEquals("NOT_ROD.java.lang.String",
				errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[7]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[8]);
	}

	public void testEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.addPropertyValue("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertEquals("listtest1", ((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName());
		assertEquals("listtest2", ((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName());
		assertEquals("test1", binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name"));
		assertEquals("test2", binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name"));
	}

	public void testSpecificEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array[0].nestedIndexedBean.list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.addPropertyValue("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertEquals("listtest1", ((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName());
		assertEquals("test2", ((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName());
		assertEquals("test1", binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name"));
		assertEquals("test2", binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name"));
	}

	public void testInnerSpecificEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list[0].name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.addPropertyValue("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertEquals("listtest1", ((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName());
		assertEquals("test2", ((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName());
		assertEquals("test1", binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name"));
		assertEquals("test2", binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name"));
	}

	public void testDirectBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "array", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0]", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1]", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertEquals("arraya", errors.getFieldValue("array[0]"));
		assertEquals(1, errors.getFieldErrorCount("array[0]"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCode());
		assertEquals("NOT_ROD.tb.array[0]", errors.getFieldError("array[0]").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array", errors.getFieldError("array[0]").getCodes()[1]);
		assertEquals("NOT_ROD.array[0]", errors.getFieldError("array[0]").getCodes()[2]);
		assertEquals("NOT_ROD.array", errors.getFieldError("array[0]").getCodes()[3]);
		assertEquals("NOT_ROD.org.springframework.beans.DerivedTestBean", errors.getFieldError("array[0]").getCodes()[4]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCodes()[5]);
		assertEquals("arraya", errors.getFieldValue("array[0]"));

		assertEquals(1, errors.getFieldErrorCount("map[key1]"));
		assertEquals("NOT_ROD", errors.getFieldError("map[key1]").getCode());
		assertEquals("NOT_ROD.tb.map[key1]", errors.getFieldError("map[key1]").getCodes()[0]);
		assertEquals("NOT_ROD.tb.map", errors.getFieldError("map[key1]").getCodes()[1]);
		assertEquals("NOT_ROD.map[key1]", errors.getFieldError("map[key1]").getCodes()[2]);
		assertEquals("NOT_ROD.map", errors.getFieldError("map[key1]").getCodes()[3]);
		assertEquals("NOT_ROD.org.springframework.beans.TestBean", errors.getFieldError("map[key1]").getCodes()[4]);
		assertEquals("NOT_ROD", errors.getFieldError("map[key1]").getCodes()[5]);

		assertEquals(1, errors.getFieldErrorCount("map[key0]"));
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCode());
		assertEquals("NOT_NULL.tb.map[key0]", errors.getFieldError("map[key0]").getCodes()[0]);
		assertEquals("NOT_NULL.tb.map", errors.getFieldError("map[key0]").getCodes()[1]);
		assertEquals("NOT_NULL.map[key0]", errors.getFieldError("map[key0]").getCodes()[2]);
		assertEquals("NOT_NULL.map", errors.getFieldError("map[key0]").getCodes()[3]);
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCodes()[4]);
	}

	public void testDirectBindingToEmptyIndexedFieldWithRegisteredSpecificEditor() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "map[key0]", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		Errors errors = binder.getBindingResult();
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertEquals(1, errors.getFieldErrorCount("map[key0]"));
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCode());
		assertEquals("NOT_NULL.tb.map[key0]", errors.getFieldError("map[key0]").getCodes()[0]);
		assertEquals("NOT_NULL.tb.map", errors.getFieldError("map[key0]").getCodes()[1]);
		assertEquals("NOT_NULL.map[key0]", errors.getFieldError("map[key0]").getCodes()[2]);
		assertEquals("NOT_NULL.map", errors.getFieldError("map[key0]").getCodes()[3]);
		// This next code is only generated because of the registered editor, using the
		// registered type of the editor as guess for the content type of the collection.
		assertEquals("NOT_NULL.org.springframework.beans.TestBean", errors.getFieldError("map[key0]").getCodes()[4]);
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCodes()[5]);
	}

	public void testDirectBindingToEmptyIndexedFieldWithRegisteredGenericEditor() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "map", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		Errors errors = binder.getBindingResult();
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertEquals(1, errors.getFieldErrorCount("map[key0]"));
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCode());
		assertEquals("NOT_NULL.tb.map[key0]", errors.getFieldError("map[key0]").getCodes()[0]);
		assertEquals("NOT_NULL.tb.map", errors.getFieldError("map[key0]").getCodes()[1]);
		assertEquals("NOT_NULL.map[key0]", errors.getFieldError("map[key0]").getCodes()[2]);
		assertEquals("NOT_NULL.map", errors.getFieldError("map[key0]").getCodes()[3]);
		// This next code is only generated because of the registered editor, using the
		// registered type of the editor as guess for the content type of the collection.
		assertEquals("NOT_NULL.org.springframework.beans.TestBean", errors.getFieldError("map[key0]").getCodes()[4]);
		assertEquals("NOT_NULL", errors.getFieldError("map[key0]").getCodes()[5]);
	}

	public void testCustomEditorWithSubclass() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0]", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals("arraya", errors.getFieldValue("array[0]"));
		assertEquals(1, errors.getFieldErrorCount("array[0]"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCode());
		assertEquals("NOT_ROD.tb.array[0]", errors.getFieldError("array[0]").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array", errors.getFieldError("array[0]").getCodes()[1]);
		assertEquals("NOT_ROD.array[0]", errors.getFieldError("array[0]").getCodes()[2]);
		assertEquals("NOT_ROD.array", errors.getFieldError("array[0]").getCodes()[3]);
		assertEquals("NOT_ROD.org.springframework.beans.DerivedTestBean", errors.getFieldError("array[0]").getCodes()[4]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCodes()[5]);
		assertEquals("arraya", errors.getFieldValue("array[0]"));
	}

	public void testBindToStringArrayWithArrayEditor() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String[].class, "stringArray", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(StringUtils.delimitedListToStringArray(text, "-"));
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("stringArray", "a1-b2");
		binder.bind(pvs);
		assertTrue(!binder.getBindingResult().hasErrors());
		assertEquals(2, tb.getStringArray().length);
		assertEquals("a1", tb.getStringArray()[0]);
		assertEquals("b2", tb.getStringArray()[1]);
	}

	public void testBindToStringArrayWithComponentEditor() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "stringArray", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("X" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("stringArray", new String[] {"a1", "b2"});
		binder.bind(pvs);
		assertTrue(!binder.getBindingResult().hasErrors());
		assertEquals(2, tb.getStringArray().length);
		assertEquals("Xa1", tb.getStringArray()[0]);
		assertEquals("Xb2", tb.getStringArray()[1]);
	}

	public void testBindingErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		FieldError ageError = errors.getFieldError("age");
		assertEquals("typeMismatch", ageError.getCode());

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages1");
		String msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field age did not have correct type", msg);

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages2");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field Age did not have correct type", msg);

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages3");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field Person Age did not have correct type", msg);
	}

	public void testAddAllErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();

		BeanBindingResult errors2 = new BeanBindingResult(rod, "person");
		errors.rejectValue("name", "badName");
		errors.addAllErrors(errors2);

		FieldError ageError = errors.getFieldError("age");
		assertEquals("typeMismatch", ageError.getCode());
		FieldError nameError = errors.getFieldError("name");
		assertEquals("badName", nameError.getCode());
	}

	public void testBindingWithResortedList() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		MutablePropertyValues pvs = new MutablePropertyValues();
		TestBean tb1 = new TestBean("tb1", 99);
		TestBean tb2 = new TestBean("tb2", 99);
		pvs.addPropertyValue("list[0]", tb1);
		pvs.addPropertyValue("list[1]", tb2);
		binder.bind(pvs);
		assertEquals(tb1.getName(), binder.getBindingResult().getFieldValue("list[0].name"));
		assertEquals(tb2.getName(), binder.getBindingResult().getFieldValue("list[1].name"));
		tb.getList().set(0, tb2);
		tb.getList().set(1, tb1);
		assertEquals(tb2.getName(), binder.getBindingResult().getFieldValue("list[0].name"));
		assertEquals(tb1.getName(), binder.getBindingResult().getFieldValue("list[1].name"));
	}

	public void testRejectWithoutDefaultMessage() throws Exception {
		TestBean tb = new TestBean();
		tb.setName("myName");
		tb.setAge(99);

		BeanBindingResult ex = new BeanBindingResult(tb, "tb");
		ex.reject("invalid");
		ex.rejectValue("age", "invalidField");

		StaticMessageSource ms = new StaticMessageSource();
		ms.addMessage("invalid", Locale.US, "general error");
		ms.addMessage("invalidField", Locale.US, "invalid field");

		assertEquals("general error", ms.getMessage(ex.getGlobalError(), Locale.US));
		assertEquals("invalid field", ms.getMessage(ex.getFieldError("age"), Locale.US));
	}

	public void testBindExceptionSerializable() throws Exception {
		SerializablePerson tb = new SerializablePerson();
		tb.setName("myName");
		tb.setAge(99);

		BindException ex = new BindException(tb, "tb");
		ex.reject("invalid", "someMessage");
		ex.rejectValue("age", "invalidField", "someMessage");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(ex);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);

		BindException ex2 = (BindException) ois.readObject();
		assertTrue(ex2.hasGlobalErrors());
		assertEquals("invalid", ex2.getGlobalError().getCode());
		assertTrue(ex2.hasFieldErrors("age"));
		assertEquals("invalidField", ex2.getFieldError("age").getCode());
		assertEquals(new Integer(99), ex2.getFieldValue("age"));

		ex2.rejectValue("name", "invalidField", "someMessage");
		assertTrue(ex2.hasFieldErrors("name"));
		assertEquals("invalidField", ex2.getFieldError("name").getCode());
		assertEquals("myName", ex2.getFieldValue("name"));
	}

	public void testTrackDisallowedFields() throws Exception {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.setAllowedFields(new String[]{"name", "age"});

		String name = "Rob Harrop";
		String beanName = "foobar";

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("name", name);
		mpvs.addPropertyValue("beanName", beanName);

		binder.bind(mpvs);

		assertEquals(name, testBean.getName());
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertEquals(1, disallowedFields.length);
		assertEquals("beanName", disallowedFields[0]);
	}

	private static class TestBeanValidator implements Validator {

		public boolean supports(Class clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", null, "simply too young");
			}
			if (tb.getAge() % 2 == 0) {
				errors.rejectValue("age", "AGE_NOT_ODD", null, "your age isn't odd");
			}
			if (tb.getName() == null || !tb.getName().equals("Rod")) {
				errors.rejectValue("name", "NOT_ROD", "are you sure you're not Rod?");
			}
			if (tb.getTouchy() == null || !tb.getTouchy().equals(tb.getName())) {
				errors.reject("NAME_TOUCHY_MISMATCH", "name and touchy do not match");
			}
			if (tb.getAge() == 0) {
				errors.reject("GENERAL_ERROR", new String[]{"arg"}, "msg");
			}
		}
	}


	private static class SpouseValidator implements Validator {

		public boolean supports(Class clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", null, "simply too young");
			}
		}
	}

}
