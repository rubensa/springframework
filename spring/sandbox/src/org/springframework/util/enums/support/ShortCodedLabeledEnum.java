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

/**
 * Implementation of LabeledEnum which uses Short as the code type.
 * <p>
 * Should almost always be subclassed, but for some simple situations it may be
 * used directly. Note that you will not be able to use unique type based
 * functionality like LabeledEnumResolver.getEnumsAsCollection() in this case.
 * @author Keith Donald
 */
public class ShortCodedLabeledEnum extends AbstractLabeledEnum {

	private Short code;

	protected ShortCodedLabeledEnum(int code) {
		this(code, null);
	}

	protected ShortCodedLabeledEnum(int code, String label) {
		super(label);
		this.code = new Short((short)code);
	}

	public Comparable getCode() {
		return code;
	}

	public short getShortCode() {
		return ((Short)getCode()).shortValue();
	}
}