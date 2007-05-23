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

package org.springframework.beans.factory.wiring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

/**
 * Convenient superclass for configurers that can perform Dependency Injection
 * on objects (however they may be created).
 *
 * <p>Typically subclassed by AspectJ aspects.

 * <p>Subclasses may also need a metadata resolution strategy, in the
 * {@link BeanWiringInfoResolver} interface. The default implementation looks
 * for a bean with the same name as the fully-qualified class name. (This is
 * the default name of the bean in a Spring XML file if the '<code>id</code>'
 * attribute is not used.)

 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class BeanConfigurerSupport implements BeanFactoryAware, InitializingBean, DisposableBean  {

	/** Logger available to subclasses */
	protected Log logger = LogFactory.getLog(getClass());

	private BeanWiringInfoResolver beanWiringInfoResolver;

	private ConfigurableListableBeanFactory beanFactory;


	/**
	 * Set the <code>BeanWiringInfoResolver</code> to use.
	 * <p>Default behavior will be to look for a bean with the same name as the
	 * class.
	 * <p>As an alternative, consider using annotation-driven bean wiring.
	 * @param beanWiringInfoResolver the <code>BeanWiringInfoResolver</code> to use.
	 * @see ClassNameBeanWiringInfoResolver
	 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
	 */
	public void setBeanWiringInfoResolver(BeanWiringInfoResolver beanWiringInfoResolver) {
		Assert.notNull(beanWiringInfoResolver, "'beanWiringInfoResolver' cannot be null.");
		this.beanWiringInfoResolver = beanWiringInfoResolver;
	}

	/**
	 * Set the {@link BeanFactory} in which this aspect must configure beans.
	 * @throws IllegalArgumentException if the supplied <code>beanFactory</code> is
	 * not an {@link ConfigurableListableBeanFactory}
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
				 "Bean configurer aspect needs to run in an ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	/**
	 * If no {@link #setBeanWiringInfoResolver BeanWiringInfoResolver} was
	 * provided, use a {@link ClassNameBeanWiringInfoResolver} as the default.
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.beanWiringInfoResolver == null) {
			this.beanWiringInfoResolver = new ClassNameBeanWiringInfoResolver();
		}		
	}
	
	/**
	 * Release references to the {@link BeanFactory} and
	 * {@link BeanWiringInfoResolver} when the container is destroyed.
	 */
	public void destroy() {
		this.beanFactory = null;
		this.beanWiringInfoResolver = null;
	}


	/**
	 * Configure the bean instance.
	 * <p>Subclasses can override this to provide custom configuration logic.
	 * Typically called by an aspect, for all bean instances matched by a
	 * pointcut.
	 * @param beanInstance the bean instance to configure (must <b>not</b> be <code>null</code>)
	 */
	protected void configureBean(Object beanInstance) {
		if (this.beanWiringInfoResolver == null) {
			if(logger.isWarnEnabled()) {
				logger.warn("[" + getClass().getName() + "] has not been configured by Spring " +
					"and is unable to configure bean instances. Object with identity " +
					"hashcode " + System.identityHashCode(beanInstance) + " has not been configured: " +
					"Make sure this configurer runs in a Spring container. " +
					"For example, add it to a Spring application context as an XML bean definition.");
			}
			return;
		}
		
		BeanWiringInfo bwi = this.beanWiringInfoResolver.resolveWiringInfo(beanInstance);
		if (bwi == null) {
			// Skip the bean if no wiring info given.
			return;
		}

		if (this.beanFactory == null) {
			if(logger.isWarnEnabled()) {
				logger.warn("BeanFactory has not been set on [" + getClass().getName() + "]: " +
					"Make sure this configurer runs in a Spring container. " +
					"For example, add it to a Spring application context as an XML bean definition.");
			}
			return;
		}

		if (bwi.indicatesAutowiring() ||
				(bwi.isDefaultBeanName() && !this.beanFactory.containsBeanDefinition(bwi.getBeanName()))) {
			// Perform autowiring (also applying standard factory / post-processor callbacks).
			this.beanFactory.autowireBeanProperties(beanInstance, bwi.getAutowireMode(), bwi.getDependencyCheck());
			Object result = this.beanFactory.initializeBean(beanInstance, bwi.getBeanName());
			checkExposedObject(result, beanInstance);
		}
		else {
			// Perform explicit wiring based on the specified bean definition.
			Object result = this.beanFactory.configureBean(beanInstance, bwi.getBeanName());
			checkExposedObject(result, beanInstance);
		}
	}

	private void checkExposedObject(Object exposedObject, Object originalBeanInstance) {
		if (exposedObject != originalBeanInstance) {
			throw new IllegalStateException("Post-processor tried to replace bean instance [" + originalBeanInstance +
					"] with (proxy) object [" + exposedObject + "] - not supported for aspect-configured classes!");
		}
	}

}
