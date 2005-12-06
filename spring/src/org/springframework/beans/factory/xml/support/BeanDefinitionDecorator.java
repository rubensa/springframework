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

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;

/**
 * Interface used by the {@link org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser}
 * to handle custom, nested (directly under a <code>&lt;bean&gt;</code>) tags. Implementations are
 * free to turn the metdata in the custom tag into as many <code>BeanDefinitions</code> as required and
 * to transform the <code>BeanDefinition</code> of the enclosing <code>&lt;bean&gt;</code> tag,
 * potentially even returning a completely different <code>BeanDefinition</code> to replace
 * the original.
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} should be aware that they may be part of
 * a chain. In particular, a {@link BeanDefinitionDecorator} should be aware that a previous
 * {@link BeanDefinitionDecorator} may have replaced the original <code>BeanDefinition</code> with
 * a {@link org.springframework.aop.framework.ProxyFactoryBean} definition allowing for custom
 * {@link org.aopalliance.intercept.MethodInterceptor interceptors} to be added.
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} that wish to add an interceptor to the
 * enclosing bean should extend {@link AbstractInterceptorDrivenBeanDefinitionDecorator} which handles
 * the chaining ensuring that only one proxy is created and that it contains all interceptors from the
 * chain.
 * <p>The parser locates a {@link BeanDefinitionDecorator} from the {@link NamespaceHandler} for the
 * namespace in which the custom tag resides.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see BeanDefinitionParser
 */
public interface BeanDefinitionDecorator {


    /**
     * Parse the specified {@link Element} and decorate the supplied <code>BeanDefinition</code>, returning
     * the decorated definition.
     * <p/>Implementations may choose to return a completely new definition, which will replace
     * the original definition in the resulting <code>BeanFactory</code>.
     * <p/>The supplied supplied {@link BeanDefinitionRegistry} can be used to register any additional beans
     * needed to support the main definition.
     */
    BeanDefinitionHolder decorate(Element element, BeanDefinitionHolder definition, BeanDefinitionRegistry registry);
}
