/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ejb.support;

import javax.ejb.EnterpriseBean;

import org.springframework.aop.util.WeakReferenceMonitor;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextJndiBeanFactoryLocator;

/**
 * <p>
 * Superclass for all EJBs. Package-visible: not intended for direct subclassing. Provides
 * a standard way of loading a BeanFactory. Subclasses act as a facade, with the business
 * logic deferred to beans in the BeanFactory.
 * </p>
 * 
 * <p>
 * Default is to use a ContextJndiBeanFactoryLocator, which will initialize an XML
 * ApplicationContext from the classpath (based on a JNDI name specified). For a slightly
 * lighter weight implementation when ApplicationContext usage is not required,
 * setBeanFactoryLocator may be called (<i>before</i> your EJB's ejbCreate method is
 * invoked, for example, in setSessionContext) with a JndiBeanFactoryLocator, which will
 * load an XML BeanFactory from the classpath. For use of a shared ApplicationContext
 * between mutliple EJBs, where the container classloader setup supports this visibility,
 * you may instead use a ContextSingletonBeanFactoryLocator. Alternately,
 * setBeanFactoryLocator may be called with a completely custom implementation of
 * BeanFactoryLocator.
 * </p>
 * 
 * <p>
 * Note that we cannot use final for our implementation of EJB lifecycle methods, as this
 * violates the EJB specification.
 * </p>
 * 
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @see #setBeanFactoryLocator
 * @see org.springframework.context.access.ContextJndiBeanFactoryLocator
 * @see org.springframework.beans.factory.access.JndiBeanFactoryLocator
 */
abstract class AbstractEnterpriseBean implements EnterpriseBean {

	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	/**
	 * Helper strategy that knows how to locate a Spring BeanFactory (or
	 * ApplicationContext).
	 */
	private BeanFactoryLocator beanFactoryLocator;

	/** factoryKey to be used with BeanFactoryLocator */
	private String beanFactoryLocatorKey;

	/** Spring BeanFactory that provides the namespace for this EJB */
	private BeanFactoryReference beanFactoryReference;

	/**
	 * Set the BeanFactoryLocator to use for this EJB. Default is a
	 * ContextJndiBeanFactoryLocator.
	 * <p>
	 * Can be invoked before loadBeanFactory, for example in constructor or
	 * setSessionContext if you want to override the default locator.
	 * <p>
	 * Note that the BeanFactory is automatically loaded by the ejbCreate
	 * implementations of AbstractStatelessSessionBean and
	 * AbstractMessageDriverBean but needs to be explicitly loaded in custom
	 * AbstractStatefulSessionBean ejbCreate methods.
	 * 
	 * @see AbstractStatelessSessionBean#ejbCreate
	 * @see AbstractMessageDrivenBean#ejbCreate
	 * @see AbstractStatefulSessionBean#loadBeanFactory
	 * @see org.springframework.context.access.ContextJndiBeanFactoryLocator
	 * @see org.springframework.beans.factory.access.JndiBeanFactoryLocator
	 */
	public void setBeanFactoryLocator(BeanFactoryLocator beanFactoryLocator) {
		this.beanFactoryLocator = beanFactoryLocator;
	}

	/**
	 * Set the bean factory locator key.
	 * <p>
	 * In case of the default BeanFactoryLocator implementation,
	 * ContextJndiBeanFactoryLocator, this is the JNDI path. The default value
	 * of this property is "java:comp/env/ejb/BeanFactoryPath".
	 * <p>
	 * Can be invoked before loadBeanFactory, for example in constructor or
	 * setSessionContext if you want to override the default locator key.
	 * 
	 * @see #BEAN_FACTORY_PATH_ENVIRONMENT_KEY
	 */
	public void setBeanFactoryLocatorKey(String factoryKey) {
		this.beanFactoryLocatorKey = factoryKey;
	}

	/**
	 * Load a Spring BeanFactory namespace. Subclasses must invoke this method.
	 * <p>
	 * Package-visible as it shouldn't be called directly by user-created
	 * subclasses.
	 * 
	 * @see org.springframework.ejb.support.AbstractStatelessSessionBean#ejbCreate()
	 */
	void loadBeanFactory() throws BeansException {
		if (beanFactoryLocator == null) {
			beanFactoryLocator = new ContextJndiBeanFactoryLocator();
		}
		if (beanFactoryLocatorKey == null) {
			beanFactoryLocatorKey = BEAN_FACTORY_PATH_ENVIRONMENT_KEY;
		}

		BeanFactoryReference targetBeanFactoryRef = beanFactoryLocator
				.useBeanFactory(this.beanFactoryLocatorKey);

		// we can not rely on the container to call ejbRemove() (it's skipped in
		// the case of system exceptions), so ensure the the bean factory
		// reference is eventually released
		this.beanFactoryReference = (BeanFactoryReference) WeakReferenceMonitor.monitor(
				targetBeanFactoryRef, false, new WeakReferenceMonitor.ReleaseListener() {
					public void notifyRelease(Object object) {
						BeanFactoryReference bfr = (BeanFactoryReference) object;
						// this may or may not actually do anything, depending
						// on whether or not the container calls ejbRemove()
						bfr.release();
					}
				});
	}

	/**
	 * <p>
	 * Unload the Spring BeanFactory instance. The default ejbRemove method
	 * invokes this method, but subclasses which override ejbRemove must invoke
	 * this method themselves.
	 * </p>
	 * <p>
	 * Package-visible as it shouldn't be called directly by user-created
	 * subclasses.
	 * </p>
	 */
	void unloadBeanFactory() throws FatalBeanException {
		// we will not ever get here if the container skips calling ejbRemove, but the
		// WeakReferenceMonitor will still clean up (later) in that case
		if (this.beanFactoryReference != null) {
			this.beanFactoryReference.release();
			this.beanFactoryReference = null;
		}
	}

	/**
	 * May be called after ejbCreate().
	 * 
	 * @return the bean factory
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactoryReference.getFactory();
	}

	/**
	 * EJB lifecycle method, implemented to invoke onEjbRemote and unload the
	 * BeanFactory afterwards.
	 * <p>
	 * Don't override it (although it can't be made final): code your shutdown
	 * in onEjbRemove.
	 * 
	 * @see #onEjbRemove
	 */
	public void ejbRemove() {
		onEjbRemove();
		unloadBeanFactory();
	}

	/**
	 * Subclasses must implement this method to do any initialization they would
	 * otherwise have done in an ejbRemove() method. The BeanFactory will be
	 * unloaded afterwards.
	 * <p>
	 * This implementation is empty, to be overridden in subclasses. The same
	 * restrictions apply to the work of this method as to an ejbRemove()
	 * method.
	 */
	protected void onEjbRemove() {
		// empty
	}
}
