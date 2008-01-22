/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans.factory.aspectj;

import java.io.Serializable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;


/**
 * Concrete aspect that uses the {@link Configurable }
 * annotation to identify which classes need autowiring.
 *
 * <p>The bean name to look up will be taken from the
 * <code>&#64;Configurable</code> annotation if specified, otherwise the
 * default bean name to look up will be the FQN of the class being configured.
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.beans.factory.annotation.Configurable
 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
 */
public aspect AnnotationBeanConfigurerAspect 
	extends AbstractInterfaceDrivenDependencyInjectionAspect 
	implements BeanFactoryAware, InitializingBean, DisposableBean {

	private BeanConfigurerSupport beanConfigurerSupport = new BeanConfigurerSupport();
	
	public pointcut inConfigurableBean() : @this(Configurable);
	
	public pointcut preConstructionConfiguration() : preConstructionConfigurationSupport(*); 

	declare parents: @Configurable * implements ConfigurableObject;

	public void configureBean(Object bean) {
		beanConfigurerSupport.configureBean(bean);
	}

	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		beanConfigurerSupport.setBeanFactory(beanFactory);
		beanConfigurerSupport.setBeanWiringInfoResolver(new AnnotationBeanWiringInfoResolver());
	}

	public void afterPropertiesSet() throws Exception {
		beanConfigurerSupport.afterPropertiesSet();
	}

	public void destroy() throws Exception {
		beanConfigurerSupport.destroy();
	}

	
	/*
	 * An intermediary to match preConstructionConfiguration signature (that doesn't expose the annotation object)
	 */
	private pointcut preConstructionConfigurationSupport(Configurable c) : @this(c) && if(c.preConstruction());
	
	/*
	 * This declaration shouldn't be needed, 
	 * except for an AspectJ bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=214559)
	 */
	declare parents: @Configurable Serializable+ 
		implements ConfigurableDeserializationSupport;

}


