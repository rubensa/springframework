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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.binding.format.support.SimpleFormatterFactory;
import org.springframework.core.enums.LabeledEnum;

/**
 * Default, local implementation of a conversion service.
 * 
 * @author Keith Donald
 */
public class DefaultConversionService extends GenericConversionService {

	/**
	 * Creates a new default conversion service, installing the default
	 * converters.
	 */
	public DefaultConversionService() {
		addDefaultConverters();
	}

	protected void addDefaultConverters() {
		addConverter(new TextToClass());
		addConverter(new TextToNumber(new SimpleFormatterFactory()));
		addConverter(new TextToBoolean());
		addConverter(new TextToLabeledEnum());
		addDefaultAlias(String.class);
		addDefaultAlias(Short.class);
		addDefaultAlias(Integer.class);
		addAlias("int", Integer.class);
		addDefaultAlias(Byte.class);
		addDefaultAlias(Long.class);
		addDefaultAlias(Float.class);
		addDefaultAlias(Double.class);
		addDefaultAlias(BigInteger.class);
		addDefaultAlias(BigDecimal.class);
		addDefaultAlias(Boolean.class);
		addDefaultAlias(Class.class);
		addDefaultAlias(LabeledEnum.class);
	}
}