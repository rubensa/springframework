/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * Property editor for Boolean properties.
 *
 * <p>This is not meant to be used as system PropertyEditor but rather as
 * locale-specific Boolean editor within custom controller code, to parse
 * UI-caused boolean strings into Boolean properties of beans, and
 * evaluate them in the UI form.
 *
 * <p>In web MVC code, this editor will typically be registered with
 * <code>binder.registerCustomEditor</code> calls in an implementation
 * of BaseCommandController's <code>initBinder</code> method.
 *
 * @author Juergen Hoeller
 * @since 10.06.2003
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 * @see org.springframework.web.bind.BindInitializer#initBinder
 */
public class CustomBooleanEditor extends PropertyEditorSupport {

	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";

	public static final String VALUE_ON = "on";
	public static final String VALUE_OFF = "off";

	public static final String VALUE_YES = "yes";
	public static final String VALUE_NO = "no";

	private final String trueString;

	private final String falseString;

	private final boolean allowEmpty;

	/**
	 * Create a new CustomBooleanEditor instance,
	 * with "true" and "false" as recognized String values.
	 * <p>The "allowEmpty" parameter states if an empty String should
	 * be allowed for parsing, i.e. get interpreted as null value.
	 * Else, an IllegalArgumentException gets thrown in that case.
	 * @param allowEmpty if empty strings should be allowed
	 */
	public CustomBooleanEditor(boolean allowEmpty) {
		this(VALUE_TRUE, VALUE_FALSE, allowEmpty);
	}

	/**
	 * Create a new CustomBooleanEditor instance,
	 * with configurable String values for true and false.
	 * <p>The "allowEmpty" parameter states if an empty String should
	 * be allowed for parsing, i.e. get interpreted as null value.
	 * Else, an IllegalArgumentException gets thrown in that case.
	 * @param trueString the String value that represents true:
	 * for example, "true" (VALUE_TRUE), "on" (VALUE_ON),
	 * "yes" (VALUE_YES) or some custom value
	 * @param falseString the String value that represents false:
	 * for example, "false" (VALUE_FALSE), "off" (VALUE_OFF),
	 * "no" (VALUE_NO) or some custom value
	 * @param allowEmpty if empty strings should be allowed
	 * @see #VALUE_TRUE
	 * @see #VALUE_FALSE
	 * @see #VALUE_ON
	 * @see #VALUE_OFF
	 * @see #VALUE_YES
	 * @see #VALUE_NO
	 */
	public CustomBooleanEditor(String trueString, String falseString, boolean allowEmpty) {
		this.trueString = trueString;
		this.falseString = falseString;
		this.allowEmpty = allowEmpty;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		}
		else if (text.equalsIgnoreCase(this.trueString)) {
			setValue(Boolean.TRUE);
		}
		else if (text.equalsIgnoreCase(this.falseString)) {
			setValue(Boolean.FALSE);
		}
		else
			throw new IllegalArgumentException("Invalid boolean value [" + text + "]");
	}

	public String getAsText() {
		if (Boolean.TRUE.equals(getValue())) {
			return this.trueString;
		}
		else if (Boolean.FALSE.equals(getValue())) {
			return this.falseString;
		}
		else {
			return "";
		}
	}

}
