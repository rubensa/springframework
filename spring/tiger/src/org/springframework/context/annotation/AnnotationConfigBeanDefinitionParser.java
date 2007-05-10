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

package org.springframework.context.annotation;

import org.w3c.dom.Element;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.ClassUtils;

/**
 * Parser for the &lt;context:annotation-config/&gt; element.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 */
public class AnnotationConfigBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * The bean name of the internally managed Autowired annotation processor.
	 */
	public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
					"org.springframework.context.config.internalAutowiredAnnotationProcessor";

	/**
	 * The bean name of the internally managed Required annotation processor.
	 */
	public static final String REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
					"org.springframework.context.config.internalRequiredAnnotationProcessor";

	/**
	 * The bean name of the internally managed JSR-250 annotation processor.
	 */
	public static final String COMMON_ANNOTATION_PROCESSOR_BEAN_NAME =
					"org.springframework.context.config.internalCommonAnnotationProcessor";


	private static final boolean jsr250Present = ClassUtils.isPresent(
			"javax.annotation.Resource", AnnotationConfigBeanDefinitionParser.class.getClassLoader());


	public BeanDefinition parse(Element element, ParserContext parserContext) {
		registerAnnotationConfigProcessors(parserContext.getReaderContext());
		return null;
	}

	protected void registerAnnotationConfigProcessors(XmlReaderContext readerContext) {
		BeanDefinitionRegistry registry = readerContext.getRegistry();
		if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			registry.registerBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
					new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
		}
		if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			registry.registerBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
					new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class));
		}

		// Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
		if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			registry.registerBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME,
					new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class));
		}
	}

}
