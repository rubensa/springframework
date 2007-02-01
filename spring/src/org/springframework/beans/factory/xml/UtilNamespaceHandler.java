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

package org.springframework.beans.factory.xml;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * {@link NamespaceHandler} for the <code>util</code> namespace.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
		registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}


	private static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return FieldRetrievingFactoryBean.class;
		}

		protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("static-field");
			}
			return id;
		}
	}


	private static class PropertyPathBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return PropertyPathFactoryBean.class;
		}

		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			String path = element.getAttribute("path");
			if (!StringUtils.hasText(path)) {
				parserContext.getReaderContext().error("Attribute 'path' must not be empty", element);
			}
			int dotIndex = path.indexOf(".");
			if (dotIndex == -1) {
				parserContext.getReaderContext().error(
						"Attribute 'path' must follow pattern 'beanName.propertyName'", element);
			}
			String beanName = path.substring(0, dotIndex);
			String propertyPath = path.substring(dotIndex + 1);
			builder.addPropertyValue("targetBeanName", beanName);
			builder.addPropertyValue("propertyPath", propertyPath);
		}

		protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("path");
			}
			return id;
		}
	}


	private static class ListBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return ListFactoryBean.class;
		}

		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			String listClass = element.getAttribute("list-class");
			List parsedList = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceList", parsedList);
			if (StringUtils.hasText(listClass)) {
				builder.addPropertyValue("targetListClass", listClass);
			}
		}
	}


	private static class SetBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return SetFactoryBean.class;
		}

		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			String setClass = element.getAttribute("set-class");
			Set parsedSet = parserContext.getDelegate().parseSetElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceSet", parsedSet);
			if (StringUtils.hasText(setClass)) {
				builder.addPropertyValue("targetSetClass", setClass);
			}
		}
	}


	private static class MapBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return MapFactoryBean.class;
		}

		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			String mapClass = element.getAttribute("map-class");
			Map parsedMap = parserContext.getDelegate().parseMapElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceMap", parsedMap);
			if (StringUtils.hasText(mapClass)) {
				builder.addPropertyValue("targetMapClass", mapClass);
			}
		}
	}


	private static class PropertiesBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}

		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			super.doParse(element, parserContext, builder);
			Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
			builder.addPropertyValue("properties", parsedProps);
		}
	}

}
