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

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Base implementation for {@link BeanDefinitionDecorator BeanDefinitionDecorators} wishing
 * to add an {@link org.aopalliance.intercept.MethodInterceptor interceptor} to the resulting
 * bean.
 * <p/>This base class controls the creation of the {@link ProxyFactoryBean} bean definition
 * and wraps the original as an inner-bean definition for the <code>target</code> property of
 * {@link ProxyFactoryBean}.
 * <p/>Chaining is correctly handled, ensuring that only one {@link ProxyFactoryBean} definition
 * is created. If a previous {@link BeanDefinitionDecorator} already created the {@link org.springframework.aop.framework.ProxyFactoryBean}
 * then the interceptor is simply added to the existing definition.
 * <p/>Sub-classes have only to create the <code>BeanDefinition</code> to the interceptor they
 * wish to add.
 *
 * @author Rob Harrop
 * @since 1.3
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public abstract class AbstractInterceptorDrivenBeanDefinitionDecorator implements BeanDefinitionDecorator {

    public final BeanDefinitionHolder decorate(Element element, BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        // get the root bean name - will be the name of the generated proxy factory bean
        String existingBeanName = definitionHolder.getBeanName();
        BeanDefinition existingDefinition = definitionHolder.getBeanDefinition();

        // delegate to subclass for interceptor def
        BeanDefinition interceptorDefinition = createInterceptorDefinition(element);

        // generate name and register the interceptor
        String interceptorName = existingBeanName + "." + getInterceptorNameSuffix(interceptorDefinition);
        BeanDefinitionReaderUtils.registerBeanDefinition(new BeanDefinitionHolder(interceptorDefinition, interceptorName), registry);

        BeanDefinitionHolder result = definitionHolder;

        if (!isProxyFactoryBeanDefinition(existingDefinition)) {

            // create the proxy definitionHolder
            RootBeanDefinition proxyDefinition = new RootBeanDefinition();
            // create proxy factory bean definitionHolder
            proxyDefinition.setBeanClass(ProxyFactoryBean.class);

            // set up property values
            MutablePropertyValues mpvs = new MutablePropertyValues();
            proxyDefinition.setPropertyValues(mpvs);

            // set the target
            mpvs.addPropertyValue("target", existingDefinition);

            // create the interceptor names list
            ManagedList interceptorList = new ManagedList();
            mpvs.addPropertyValue("interceptorNames", interceptorList);

            result = new BeanDefinitionHolder(proxyDefinition, existingBeanName);
        }

        addInterceptorNameToList(interceptorName, result.getBeanDefinition());

        return result;

    }

    private void addInterceptorNameToList(String interceptorName, BeanDefinition beanDefinition) {
        ManagedList list = (ManagedList) beanDefinition.getPropertyValues().getPropertyValue("interceptorNames").getValue();
        list.add(interceptorName);
    }

    private boolean isProxyFactoryBeanDefinition(BeanDefinition existingDefinition) {
        return ((RootBeanDefinition) existingDefinition).getBeanClass().equals(ProxyFactoryBean.class);
    }

    protected String getInterceptorNameSuffix(BeanDefinition interceptorDefinition) {
        String name = ((RootBeanDefinition) interceptorDefinition).getBeanClassName();
        return StringUtils.uncapitalize(ClassUtils.getShortName(name));
    }

    /**
     * Sub-classes should implement this method to return the <code>BeanDefinition</code> for
     * the interceptor they wish to apply to the bean being decorated.
     */
    protected abstract BeanDefinition createInterceptorDefinition(Element element);

}
