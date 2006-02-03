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

import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class RadioButtonTag extends AbstractHtmlInputElementTag {

	private String value;

	public void setValue(String value) {
		this.value = value;
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "radio");

		Object resolvedValue = evaluate("value", this.value);
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));

		if (ObjectUtils.nullSafeEquals(resolvedValue, getValue())) {
			tagWriter.writeAttribute("checked", "true");
		}

		tagWriter.endTag();
		return EVAL_PAGE;
	}
}
