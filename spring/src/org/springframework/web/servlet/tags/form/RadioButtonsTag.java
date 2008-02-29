/*
 * Copyright 2002-2008 the original author or authors.
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

/**
 * Databinding-aware JSP tag for rendering multiple HTML '<code>input</code>'
 * elements with a '<code>type</code>' of '<code>radio</code>'.
 *
 * <p>Rendered elements are marked as 'checked' if the configured
 * {@link #setItems(Object) value} matches the bound value.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 */
public class RadioButtonsTag extends AbstractMultiCheckedElementTag {

	protected String getInputType() {
		return "radio";
	}

}
