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

package org.springframework.web.servlet.tags.form;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.TestBean;

import javax.servlet.jsp.tagext.Tag;
import java.io.StringReader;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class CheckboxTagTests extends AbstractFormTagTests {

	private CheckboxTag tag;

	private TestBean bean;

	protected void onSetUp() {
		this.tag = new CheckboxTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWithSingleValueBooleanChecked() throws Exception {
		this.tag.setPath("jedi");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("jedi", checkboxElement.attribute("name").getValue());
		assertEquals("true", checkboxElement.attribute("checked").getValue());
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}

	public void testWithSingleValueBooleanUnchecked() throws Exception {
		this.bean.setJedi(false);
		this.tag.setPath("jedi");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("jedi", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("true", checkboxElement.attribute("value").getValue());
	}

	public void testWithSingleValueNull() throws Exception {
		this.bean.setName(null);
		this.tag.setPath("name");
		this.tag.setValue("Rob Harrop");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("name", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("Rob Harrop", checkboxElement.attribute("value").getValue());
	}

	public void testWithSingleValueNotNull() throws Exception {
		this.bean.setName("Rob Harrop");
		this.tag.setPath("name");
		this.tag.setValue("Rob Harrop");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("name", checkboxElement.attribute("name").getValue());
		assertEquals("true", checkboxElement.attribute("checked").getValue());
		assertEquals("Rob Harrop", checkboxElement.attribute("value").getValue());
	}

	public void testWithMultiValueChecked() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setValue("foo");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("stringArray", checkboxElement.attribute("name").getValue());
		assertEquals("true", checkboxElement.attribute("checked").getValue());
		assertEquals("foo", checkboxElement.attribute("value").getValue());
	}

	public void testWithMultiValueUnchecked() throws Exception {
		this.tag.setPath("stringArray");
		this.tag.setValue("abc");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		// wrap the output so it is valid XML
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element checkboxElement = (Element) document.getRootElement().elements().get(0);
		assertEquals("input", checkboxElement.getName());
		assertEquals("checkbox", checkboxElement.attribute("type").getValue());
		assertEquals("stringArray", checkboxElement.attribute("name").getValue());
		assertNull(checkboxElement.attribute("checked"));
		assertEquals("abc", checkboxElement.attribute("value").getValue());
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		this.bean.setName("Rob Harrop");
		this.bean.setJedi(true);
		this.bean.setStringArray(new String[]{"foo", "bar"});
		return this.bean;
	}
}
