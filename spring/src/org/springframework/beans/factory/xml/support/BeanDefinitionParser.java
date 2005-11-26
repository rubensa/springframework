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

package org.springframework.beans.factory.xml.support;

import org.w3c.dom.Element;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Interface used by the {@link org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser}
 * to handle custom, top-level (directly under <code>&lt;beans&gt;</code>) tags. Implementations are
 * free to turn the metdata in the custom tag into as many <code>BeanDefinitions</code> as required.
 * <p>The parser locates a {@link BeanDefinitionParser} from the {@link NamespaceHandler} for the
 * namespace in which the custom tag resides.
 *
 * @author Rob Harrop
 * @since 1.3
 * @see NamespaceHandler
 * @see BeanDefinitionDecorator
 */
public interface BeanDefinitionParser {

    /**
     * Parse the specified {@link Element} and register resulting <code>BeanDefinitions</code>
     * with the supplied {@link BeanDefinitionRegistry}.
     */
    void parse(Element element, BeanDefinitionRegistry registry);
}
