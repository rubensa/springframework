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

import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.tagext.Tag;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class FormTagTests extends AbstractHtmlElementTagTests {

	private FormTag tag;

	protected void onSetUp() {
		this.tag = new FormTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWriteForm() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";

		this.tag.setAction(action);
		this.tag.setCommandName(commandName);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "action", action);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);


	}

	private void assertFormTagOpened(String output) {
		assertTrue(output.startsWith("<form "));
	}

	private void assertFormTagClosed(String output) {
		assertTrue(output.endsWith("</form>"));
	}

	protected void extendRequest(MockHttpServletRequest request) {
		//
	}
}
