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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Custom {@link java.beans.PropertyEditor} for {@link Properties} objects.
 *
 * <p>Handles conversion from content {@link String} to <code>Properties</code> object.
 * Also handles {@link Map} to <code>Properties</code> conversion, for populating
 * a <code>Properties</code> object via XML "map" entries.
 *
 * <p>The required format is defined in the standard <code>Properties</code>
 * documentation. Each property must be on a new line.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.Properties#load
 */
public class PropertiesEditor extends PropertyEditorSupport {
	
	/**
	 * Any of these characters, if they're first after whitespace or first
	 * on a line, mean that the line is a comment and should be ignored.
	 */
	private final static String COMMENT_MARKERS = "#!";


	/**
	 * Convert {@link String} into {@link Properties}, considering it as
	 * properties content.
	 * @param text the text to be so converted
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		Properties props = new Properties();
		if (text != null) {
			try {
				// Must use the ISO-8859-1 encoding because Properties.load(stream) expects it.
				props.load(new ByteArrayInputStream(text.getBytes("ISO-8859-1")));
			}
			catch (IOException ex) {
				// Should never happen.
				throw new IllegalArgumentException(
						"Failed to parse [" + text + "] into Properties: " + ex.getMessage());
			}
		}
		setValue(props);
	}

	/**
	 * Take {@link Properties} as-is; convert {@link Map} into <code>Properties</code>.
	 */
	public void setValue(Object value) {
		if (!(value instanceof Properties) && value instanceof Map) {
			Properties props = new Properties();
			props.putAll((Map) value);
			super.setValue(props);
		}
		else {
			super.setValue(value);
		}
	}

}
