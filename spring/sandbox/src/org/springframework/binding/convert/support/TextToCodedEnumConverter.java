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

import java.util.Locale;

import org.springframework.enums.CodedEnum;
import org.springframework.enums.CodedEnumResolver;
import org.springframework.enums.support.StaticCodedEnumResolver;
import org.springframework.util.StringUtils;

/**
 * Converts textual representations of <coded>CodedEnum</code> instances to a
 * specific instance of <code>CodedEnum</code>
 * @author Keith Donal
 */
public class TextToCodedEnumConverter extends AbstractConverter {

	private CodedEnumResolver enumResolver = StaticCodedEnumResolver.instance();

	public void setEnumResolver(CodedEnumResolver enumResolver) {
		this.enumResolver = enumResolver;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { CodedEnum.class };
	}

	protected Locale getLocale() {
		return null;
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String text = (String)source;
		if (StringUtils.hasText(text)) {
			return this.enumResolver.getEnum(targetClass.getName(), (Comparable)source, getLocale());
		}
		else {
			return null;
		}
	}
}