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

package org.springframework.aop.framework;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * Convenient proxy factory bean superclass for proxy factory
 * beans that create only singletons.
 * 
 * <p>Manages pre- and post-interceptors (references, rather than
 * interceptor names, as in {@link ProxyFactoryBean}) and provides
 * consistent interface management.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig
		implements FactoryBean, BeanClassLoaderAware, InitializingBean {

	private Object target;

	private Class[] proxyInterfaces;

	private Object[] preInterceptors;

	private Object[] postInterceptors;

	/** Default is global AdvisorAdapterRegistry */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	private transient ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private Object proxy;


	/**
	 * Set the target object, that is, the bean to be wrapped with a transactional proxy.
	 * <p>The target may be any object, in which case a SingletonTargetSource will
	 * be created. If it is a TargetSource, no wrapper TargetSource is created:
	 * This enables the use of a pooling or prototype TargetSource etc.
	 * @see org.springframework.aop.TargetSource
	 * @see org.springframework.aop.target.SingletonTargetSource
	 * @see org.springframework.aop.target.LazyInitTargetSource
	 * @see org.springframework.aop.target.PrototypeTargetSource
	 * @see org.springframework.aop.target.CommonsPoolTargetSource
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * Specify the set of interfaces being proxied.
	 * <p>If not specified (the default), the AOP infrastructure works
	 * out which interfaces need proxying by analyzing the target,
	 * proxying all the interfaces that the target object implements.
	 */
	public void setProxyInterfaces(Class[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * Set additional interceptors (or advisors) to be applied before the
	 * implicit transaction interceptor, e.g. PerformanceMonitorInterceptor.
	 * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
	 */
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * Set additional interceptors (or advisors) to be applied after the
	 * implicit transaction interceptor, e.g. HibernateInterceptors for
	 * eagerly binding Sessions to the current thread when using JTA.
	 * <p>Note that this is just necessary if you rely on those interceptors in general:
	 * HibernateTemplate and JdoTemplate work nicely with JtaTransactionManager through
	 * implicit on-demand thread binding.
	 * @see org.springframework.orm.hibernate3.HibernateInterceptor
	 * @see org.springframework.orm.jdo.JdoInterceptor
	 */
	public void setPostInterceptors(Object[] postInterceptors) {
		this.postInterceptors = postInterceptors;
	}

	/**
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	public void afterPropertiesSet() {
		if (this.target == null) {
			throw new IllegalArgumentException("Property 'target' is required");
		}
		if (this.target instanceof String) {
			throw new IllegalArgumentException("'target' needs to be a bean reference, not a bean name as value");
		}

		ProxyFactory proxyFactory = new ProxyFactory();

		if (this.preInterceptors != null) {
			for (int i = 0; i < this.preInterceptors.length; i++) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(this.preInterceptors[i]));
			}
		}

		// Add the main interceptor (typically an Advisor).
		proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));

		if (this.postInterceptors != null) {
			for (int i = 0; i < this.postInterceptors.length; i++) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(this.postInterceptors[i]));
			}
		}

		proxyFactory.copyFrom(this);

		TargetSource targetSource = createTargetSource(this.target);
		proxyFactory.setTargetSource(targetSource);

		if (this.proxyInterfaces != null) {
			proxyFactory.setInterfaces(this.proxyInterfaces);
		}
		else if (!isProxyTargetClass()) {
			// Rely on AOP infrastructure to tell us what interfaces to proxy.
			proxyFactory.setInterfaces(
					ClassUtils.getAllInterfacesForClass(targetSource.getTargetClass(), this.beanClassLoader));
		}

		this.proxy = getProxy(proxyFactory);
	}

	/**
	 * Determine a TargetSource for the given target (or TargetSource).
	 * @param target target. If this is an implementation of TargetSource it is
	 * used as our TargetSource; otherwise it is wrapped in a SingletonTargetSource.
	 * @return a TargetSource for this object
	 */
	protected TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource) {
			return (TargetSource) target;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}

	/**
	 * Return the proxy object to expose.
	 * <p>The default implementation uses a <code>getProxy</code> call with
	 * the factory's bean class loader. Can be overridden to specify a
	 * custom class loader.
	 * @param aopProxy the prepared AopProxy instance to get the proxy from
	 * @return the proxy object to expose
	 * @see AopProxy#getProxy(ClassLoader)
	 */
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy(this.beanClassLoader);
	}


	public Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	public Class getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		if (this.proxyInterfaces != null && this.proxyInterfaces.length == 1) {
			return this.proxyInterfaces[0];
		}
		if (this.target instanceof TargetSource) {
			return ((TargetSource) this.target).getTargetClass();
		}
		if (this.target != null) {
			return this.target.getClass();
		}
		return null;
	}

	public final boolean isSingleton() {
		return true;
	}


	/**
	 * Create the "main" interceptor for this proxy factory bean.
	 * Typically an Advisor, but can also be any type of Advice.
	 * <p>Pre-interceptors will be applied before, post-interceptors
	 * will be applied after this interceptor.
	 */
	protected abstract Object createMainInterceptor();

}
