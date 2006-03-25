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
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.support.BindStatus;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rob Harrop
 */
public class SelectTagTests extends AbstractFormTagTests {

	private SelectTag tag;

	private TestBean bean;

	protected void onSetUp() {
		this.tag = new SelectTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWithList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems(Country.getCountries());
		assertList();
	}

	public void testWithResolvedList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		assertList();
	}

	private void assertList() throws JspException, DocumentException {
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		this.tag.setSize("5");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		validateOutput(output);
		assertContainsAttribute(output, "size", "5");
	}

	public void testWithListAndNoLabel() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		validateOutput(getWriter().toString());
	}

	public void testWithMap() throws Exception {
		this.tag.setPath("sex");
		this.tag.setItems("${sexes}");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		System.out.println(getWriter());

	}

	public void testWithInvalidList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${other}");
		this.tag.setItemValue("isoCode");
		try {
			this.tag.doStartTag();
			fail("Should not be able to use a non-Collection typed value as the value of 'items'.");
		}
		catch (JspException e) {
			String message = e.getMessage();
			assertTrue(message.indexOf("'items'") > -1);
			assertTrue(message.indexOf("'org.springframework.beans.TestBean'") > -1);
		}
	}

	public void testWithNestedOptions() throws Exception {
		this.tag.setPath("country");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);

		BindStatus value = (BindStatus) getPageContext().getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
		assertEquals("Selected country not exposed in page context", "UK", value.getValue());

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		this.tag.doFinally();

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));
		assertContainsAttribute(output, "name", "country");
	}

	public void testWithStringArray() throws Exception {
		this.tag.setPath("name");
		this.tag.setItems(getNames());
		assertStringArray();
	}

	public void testWithResolvedStringArray() throws Exception {
		this.tag.setPath("name");
		this.tag.setItems("${names}");
		assertStringArray();
	}

	public void testWithMultiList() throws Exception {
		List list = new ArrayList();
		list.add(Country.COUNTRY_UK);
		list.add(Country.COUNTRY_AT);
		this.bean.setSomeList(list);

		this.tag.setPath("someList");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		System.out.println(output);
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("someList", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "true", e.attribute("selected").getValue());

		e = (Element) rootElement.selectSingleNode("option[@value = 'AT']");
		assertEquals("AT node not selected", "true", e.attribute("selected").getValue());
	}

	public void testWithMultiMap() throws Exception {
		Map someMap = new HashMap();
		someMap.put("M", "Male");
		someMap.put("F", "Female");
		this.bean.setSomeMap(someMap);

		this.tag.setPath("someMap");
		this.tag.setItems("${sexes}");

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		System.out.println(output);
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("someMap", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 2, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'M']");
		assertEquals("M node not selected", "true", e.attribute("selected").getValue());

		e = (Element) rootElement.selectSingleNode("option[@value = 'F']");
		assertEquals("F node not selected", "true", e.attribute("selected").getValue());
	}

	private void assertStringArray() throws JspException, DocumentException {
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("name", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'Rob']");
		assertEquals("Rob node not selected", "true", e.attribute("selected").getValue());
	}

	private String[] getNames() {
		return new String[]{"Rod", "Rob", "Juergen", "Adrian"};
	}

	private Map getSexes() {
		Map sexes = new HashMap();
		sexes.put("F", "Female");
		sexes.put("M", "Male");
		return sexes;
	}

	protected void extendRequest(MockHttpServletRequest request) {
		super.extendRequest(request);
		request.setAttribute("countries", Country.getCountries());
		request.setAttribute("sexes", getSexes());
		request.setAttribute("other", new TestBean());
		request.setAttribute("names", getNames());
	}

	private void validateOutput(String output) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("country", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "true", e.attribute("selected").getValue());
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		this.bean.setName("Rob");
		this.bean.setCountry("UK");
		this.bean.setSex("M");
		return this.bean;
	}

}
