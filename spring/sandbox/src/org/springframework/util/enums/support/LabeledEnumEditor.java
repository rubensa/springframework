/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.enums.support;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.enums.LabeledEnum;
import org.springframework.util.enums.LabeledEnumResolver;

/**
 * Property Editor converts the string form of a CodedEnum into a CodedEnum
 * instance using a CodedEnumResolver.
 * @author Keith Donald
 */
public class LabeledEnumEditor extends PropertyEditorSupport {

	private Locale locale = Locale.getDefault();

	private LabeledEnumResolver enumResolver = StaticLabeledEnumResolver.instance();

	private boolean allowsEmpty = true;

	private Class enumClass;

	private PropertyEditor codeConverter;

	public LabeledEnumEditor() {
	}

	public LabeledEnumEditor(Class type) {
		setEnumClass(type);
	}

	public LabeledEnumEditor(Class type, LabeledEnumResolver enumResolver) {
		setEnumClass(type);
		setEnumResolver(enumResolver);
	}

	public LabeledEnumEditor(Class type, LabeledEnumResolver enumResolver, PropertyEditor codeConverter) {
		setEnumClass(type);
		setEnumResolver(enumResolver);
		setCodeConverter(codeConverter);
	}

	public void setEnumClass(Class type) {
		this.enumClass = type;
	}

	/**
	 * Set the resolver to used to lookup enums.
	 * 
	 * @param resolver the coded enum resolver
	 */
	public void setEnumResolver(LabeledEnumResolver resolver) {
		Assert.notNull(resolver, "The enum resolver is required");
		this.enumResolver = resolver;
	}

	public void setCodeConverter(PropertyEditor codeConverter) {
		this.codeConverter = codeConverter;
	}

	public void setAllowsEmpty(boolean allowsEmpty) {
		this.allowsEmpty = allowsEmpty;
	}

	/**
	 * Sets the locale to use when resolving enums.
	 * 
	 * @param locale the locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private Locale getLocale() {
		return locale;
	}

	public void setAsText(String encodedCode) throws IllegalArgumentException {
		if (!StringUtils.hasText(encodedCode)) {
			Assert.isTrue(allowsEmpty, "This property editor does not allow empty encoded enum code values");
			setValue(null);
			return;
		}

		String type;
		Comparable code;

		if (this.enumClass == null) {
			String[] keyParts = StringUtils.delimitedListToStringArray(encodedCode, ".");
			Assert.isTrue(keyParts.length == 2, "Enum string key must in the format '<type>.<code>'");
			type = keyParts[0];
			code = decodeFromString(keyParts[1]);
		}
		else {
			type = this.enumClass.getName();
			if (ShortCodedLabeledEnum.class.isAssignableFrom(this.enumClass)) {
				code = doShortConversion(encodedCode);
			}
			else if (LetterCodedLabeledEnum.class.isAssignableFrom(this.enumClass)) {
				code = doLetterConversion(encodedCode);
			}
			else if (StringCodedLabeledEnum.class.isAssignableFrom(this.enumClass)) {
				code = encodedCode;
			}
			else {
				if (codeConverter != null) {
					codeConverter.setAsText(encodedCode);
					code = (Comparable)codeConverter.getValue();
				}
				else {
					code = decodeFromString(encodedCode);
				}
			}
		}
		LabeledEnum ce = this.enumResolver.getEnum(type, code, getLocale());
		if (!allowsEmpty) {
			Assert.notNull(ce, "The encoded code '" + encodedCode + "' did not map to a valid enum instance for type "
					+ type);
			if (this.enumClass != null) {
				Assert.isInstanceOf(this.enumClass, ce);
			}
		}
		setValue(ce);
	}

	private Short doShortConversion(String encodedCode) {
		try {
			return Short.valueOf(encodedCode);
		}
		catch (NumberFormatException e) {
			IllegalArgumentException iae = new IllegalArgumentException("The encoded enum argument '" + encodedCode
					+ "' could not be converted to a Short, and the enum type is ShortCoded.");
			iae.initCause(e);
			throw iae;
		}
	}

	private Character doLetterConversion(String encodedCode) {
		Assert.isTrue(encodedCode.length() == 1, "Character letter codes should have length == 1, this one has '"
				+ encodedCode + "' of length " + encodedCode.length());
		char c = encodedCode.charAt(0);
		Assert.isTrue(Character.isLetter(c), "Character code '" + encodedCode + "' is not a letter");
		return new Character(c);
	}

	private Comparable decodeFromString(String encodedCode) {
		if (encodedCode.length() == 1) {
			char c = encodedCode.charAt(0);
			if (Character.isLetter(c)) {
				return new Character(c);
			}
			else if (Character.isDigit(c)) {
				return new Short((short)c);
			}
			else {
				throw new IllegalArgumentException("Invalid enum code '" + encodedCode + "'");
			}
		}
		else {
			try {
				return new Short(encodedCode);
			}
			catch (NumberFormatException e) {
				return encodedCode;
			}
		}
	}

	public String getAsText() {
		LabeledEnum e = (LabeledEnum)getValue();
		return (e != null ? e.getLabel() : "");
	}

}