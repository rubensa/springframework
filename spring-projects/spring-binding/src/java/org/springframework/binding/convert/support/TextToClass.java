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
package org.springframework.binding.convert.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Converts a textual representation of a class object to a <code>Class</code>
 * instance.
 * @author Keith Donald
 */
public class TextToClass extends AbstractConverter {

	public static final String CLASS_PREFIX = "class:";

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Class.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String text = (String)source;
		if (StringUtils.hasText(text)) {
			try {
				return ClassUtils.forName(text.trim());
			}
			catch (ClassNotFoundException ex) {
				throw new ConversionException(source, Class.class, ex);
			}
		}
		else {
			return null;
		}
	}
}