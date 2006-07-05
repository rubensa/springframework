/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.io.support.ResourcePatternResolver;

/** 
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods, inherited from ListableBeanFactory.
 * This avoids the need for applications to use singletons.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the MessageSource interface.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the ResourceLoader interface.
 * <li>The ability to publish events. Implementations must provide a means
 * of registering event listeners.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard bean factory lifecycle capabilities,
 * ApplicationContext implementations need to detect ApplicationContextAware
 * beans and invoke the setApplicationContext method accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ApplicationContextAware#setApplicationContext
 * @see ConfigurableApplicationContext
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the parent context, or <code>null</code> if there is no parent,
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or <code>null</code> if there is no parent
	 */
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose
	 * of initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * ConfigurableApplicationContext interface offers access to the
	 * AutowireCapableBeanFactory interface too. The present method mainly
	 * serves as convenient, specific facility on the ApplicationContext
	 * interface itself.
	 * @throws IllegalStateException if the context does not support
	 * the AutowireCapableBeanFactory interface or does not hold an autowire-capable
	 * bean factory yet (usually if <code>refresh()</code> has never been called)
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context
	*/
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

}
