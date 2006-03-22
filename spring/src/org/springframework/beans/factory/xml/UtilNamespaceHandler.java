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
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * {@link NamespaceHandler} for the <code>util</code> namespace.
 * @author Rob Harrop
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	public UtilNamespaceHandler() {
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
	}


	public static class PropertiesBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}
	}

	public static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return FieldRetrievingFactoryBean.class;
		}
	}

	public static class MapBeanDefinitionParser implements BeanDefinitionParser {

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			String id = element.getAttribute("id");
			String mapClass = element.getAttribute("map-class");

			Map parsedMap = parserContext.getHelper().parseMapElement(element);
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MapFactoryBean.class);
			builder.addPropertyValue("sourceMap", parsedMap);
			if (StringUtils.hasText(mapClass)) {
				builder.addPropertyValue("targetMapClass", mapClass);
			}
			parserContext.getRegistry().registerBeanDefinition(id, builder.getBeanDefinition());
			// cannot be used in a 'inner-bean' setting (use plain <map>)
			return null;
		}
	}
}
