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

package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 */
public abstract class AbstractSingleBeanDefinitionParser implements BeanDefinitionParser {

	public static final String ID_ATTRIBUTE = "id";

	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClass(element));
		doParse(element, definitionBuilder);
		BeanDefinition definition = definitionBuilder.getBeanDefinition();

		String id = element.getAttribute(ID_ATTRIBUTE);
		if (StringUtils.hasText(id)) {
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id);
			BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
			parserContext.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(holder));
		} else if(!parserContext.isNested()) {
			throw new IllegalArgumentException("Attribute '" + ID_ATTRIBUTE + "' is required for element '"
							+ element.getLocalName() + "' when used as a top-level tag.");
		}
		return definition;
	}

	protected abstract Class getBeanClass(Element element);

	protected void doParse(Element element, BeanDefinitionBuilder definitionBuilder) {
	}
}
