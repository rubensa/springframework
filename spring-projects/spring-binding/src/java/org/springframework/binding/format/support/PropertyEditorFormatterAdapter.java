/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.format.support;

import java.beans.PropertyEditor;

import org.springframework.util.Assert;

/**
 * Adapts a property editor to the type converter interface.
 * @author Keith Donald
 */
public class PropertyEditorFormatterAdapter extends AbstractFormatter {

	private PropertyEditor propertyEditor;

	public PropertyEditorFormatterAdapter(PropertyEditor propertyEditor) {
		super();
		Assert.notNull(propertyEditor, "Property editor is required");
		this.propertyEditor = propertyEditor;
	}

	public PropertyEditor getPropertyEditor() {
		return propertyEditor;
	}

	protected String doFormatValue(Object value) {
		propertyEditor.setValue(value);
		return propertyEditor.getAsText();
	}

	protected Object doParseValue(String formattedValue, Class targetClass) {
		propertyEditor.setAsText(formattedValue);
		return propertyEditor.getValue();
	}
}