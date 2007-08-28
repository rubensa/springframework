/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Abstract, generic extension of {@link AbstractContextLoader} which loads a
 * {@link GenericApplicationContext} from the <em>locations</em> provided to
 * {@link #loadContext(String...)}.
 * </p>
 * <p>
 * Concrete subclasses must provide an appropriate
 * {@link #createBeanDefinitionReader(GenericApplicationContext) BeanDefinitionReader}.
 * </p>
 *
 * @see #loadContext()
 * @author Sam Brannen
 * @version $Revision$
 * @since 2.1
 */
public abstract class AbstractGenericContextLoader extends AbstractContextLoader {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log	LOG	= LogFactory.getLog(AbstractGenericContextLoader.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Loads a Spring ApplicationContext from the supplied
	 * <code>locations</code>.
	 * </p>
	 * <p>
	 * Implementation details:
	 * </p>
	 * <ul>
	 * <li>Creates a standard {@link GenericApplicationContext} instance.</li>
	 * <li>Populates it from the specified config locations through a
	 * {@link #createBeanDefinitionReader(GenericApplicationContext) BeanDefinitionReader}.</li>
	 * <li>Calls {@link #customizeBeanFactory} to allow for customizing the
	 * context's DefaultListableBeanFactory.</li>
	 * <li>Delegates to {@link AnnotationConfigUtils} for
	 * {@link AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry) registering}
	 * annotation configuration processors.</li>
	 * <li>{@link ConfigurableApplicationContext#refresh() Refreshes} the
	 * context and registers a JVM shutdown hook for it.</li>
	 * </p>
	 * </ul>
	 * <p>
	 * Subclasses must provide an appropriate implementation of
	 * {@link #createBeanDefinitionReader(GenericApplicationContext)}.
	 * </p>
	 *
	 * @see org.springframework.test.context.ContextLoader#loadContext()
	 * @see GenericApplicationContext
	 * @see #customizeBeanFactory
	 * @see #createBeanDefinitionReader(GenericApplicationContext)
	 * @see BeanDefinitionReader
	 * @return a new application context
	 */
	@Override
	public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading ApplicationContext for locations [" + StringUtils.arrayToCommaDelimitedString(locations)
					+ "].");
		}

		final GenericApplicationContext context = new GenericApplicationContext();
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		createBeanDefinitionReader(context).loadBeanDefinitions(locations);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		context.registerShutdownHook();
		return context;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Customize the internal bean factory of the ApplicationContext created by
	 * this ContextLoader.
	 * </p>
	 * <p>
	 * The default implementation is empty but can be overridden in subclasses
	 * to customize DefaultListableBeanFactory's standard settings.
	 * </p>
	 *
	 * @param beanFactory the newly created bean factory for this context
	 * @see #loadContext()
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 */
	protected void customizeBeanFactory(final DefaultListableBeanFactory beanFactory) {

		/* no-op */
	}

	// ------------------------------------------------------------------------|

	/**
	 * Factory method for creating new {@link BeanDefinitionReader}s for
	 * loading bean definitions into the supplied
	 * {@link GenericApplicationContext context}.
	 *
	 * @param context The context for which the BeanDefinitionReader should be
	 *        created.
	 * @see #loadContext()
	 * @see BeanDefinitionReader
	 * @return A BeanDefinitionReader for the supplied context.
	 */
	protected abstract BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context);

	// ------------------------------------------------------------------------|

}
