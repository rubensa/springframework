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

package org.springframework.jndi;

import javax.naming.NamingException;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that looks up a
 * JNDI object. Exposes the object found in JNDI for bean references,
 * e.g. for data access object's "dataSource" property in case of a
 * {@link javax.sql.DataSource}.
 *
 * <p>The typical usage will be to register this as singleton factory
 * (e.g. for a certain JNDI-bound DataSource) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>The default behavior is to look up the JNDI object on startup and cache it.
 * This can be customized through the "lookupOnStartup" and "cache" properties,
 * using a {@link JndiObjectTargetSource} underneath. Note that you need to specify
 * a "proxyInterface" in such a scenario, since the actual JNDI object type is not
 * known in advance.
 *
 * <p>Of course, bean classes in a Spring environment may lookup e.g. a DataSource
 * from JNDI themselves. This class simply enables central configuration of the
 * JNDI name, and easy switching to non-JNDI alternatives. The latter is
 * particularly convenient for test setups, reuse in standalone clients, etc.
 *
 * <p>Note that switching to e.g. DriverManagerDataSource is just a matter of
 * configuration: Simply replace the definition of this FactoryBean with a
 * {@link org.springframework.jdbc.datasource.DriverManagerDataSource} definition!
 *
 * @author Juergen Hoeller
 * @since 22.05.2003
 * @see #setProxyInterface
 * @see #setLookupOnStartup
 * @see #setCache
 * @see JndiObjectTargetSource
 */
public class JndiObjectFactoryBean extends JndiObjectLocator implements FactoryBean, BeanClassLoaderAware {

	private Class[] proxyInterfaces;

	private boolean lookupOnStartup = true;

	private boolean cache = true;

	private Object defaultObject;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private Object jndiObject;


	/**
	 * Specify the proxy interface to use for the JNDI object.
	 * <p>Typically used in conjunction with "lookupOnStartup"=false and/or "cache"=false.
	 * Needs to be specified because the actual JNDI object type is not known
	 * in advance in case of a lazy lookup.
	 * @see #setProxyInterfaces
	 * @see #setLookupOnStartup
	 * @see #setCache
	 */
	public void setProxyInterface(Class proxyInterface) {
		this.proxyInterfaces = new Class[] {proxyInterface};
	}

	/**
	 * Specify multiple proxy interfaces to use for the JNDI object.
	 * <p>Typically used in conjunction with "lookupOnStartup"=false and/or "cache"=false.
	 * Note that proxy interfaces will be autodetected from a specified "expectedType",
	 * if necessary.
	 * @see #setExpectedType
	 * @see #setLookupOnStartup
	 * @see #setCache
	 */
	public void setProxyInterfaces(Class[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * Set whether to look up the JNDI object on startup. Default is "true".
	 * <p>Can be turned off to allow for late availability of the JNDI object.
	 * In this case, the JNDI object will be fetched on first access.
	 * <p>For a lazy lookup, a proxy interface needs to be specified.
	 * @see #setProxyInterface
	 * @see #setCache
	 */
	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
	}

	/**
	 * Set whether to cache the JNDI object once it has been located.
	 * Default is "true".
	 * <p>Can be turned off to allow for hot redeployment of JNDI objects.
	 * In this case, the JNDI object will be fetched for each invocation.
	 * <p>For hot redeployment, a proxy interface needs to be specified.
	 * @see #setProxyInterface
	 * @see #setLookupOnStartup
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Specify a default object to fall back to if the JNDI lookup fails.
	 * Default is none.
	 * <p>This can be an arbitrary bean reference or literal value.
	 * It is typically used for literal values in scenarios where the JNDI environment
	 * might define specific config settings but those are not required to be present.
	 * <p>Note: This is only supported for lookup on startup.
	 * @see #setLookupOnStartup
	 */
	public void setDefaultObject(Object defaultObject) {
		this.defaultObject = defaultObject;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	/**
	 * Look up the JNDI object and store it.
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		super.afterPropertiesSet();

		if (!this.lookupOnStartup || !this.cache) {
			// We need to create a proxy for this...
			if (this.proxyInterfaces == null) {
				Class expectedType = getExpectedType();
				if (expectedType != null) {
					if (expectedType.isInterface()) {
						this.proxyInterfaces = new Class[] {expectedType};
					}
					else {
						this.proxyInterfaces = ClassUtils.getAllInterfacesForClass(expectedType);
					}
				}
			}
			if (this.proxyInterfaces == null) {
				throw new IllegalArgumentException(
						"Cannot deactivate 'lookupOnStartup' or 'cache' without specifying a 'proxyInterface'");
			}
		}

		if (this.proxyInterfaces != null) {
			if (this.defaultObject != null) {
				throw new IllegalArgumentException(
						"'defaultObject' is not supported in combination with 'proxyInterface'");
			}
			// We need a proxy and a JndiObjectTargetSource.
			this.jndiObject = JndiObjectProxyFactory.createJndiObjectProxy(this);
		}

		else {
			if (this.defaultObject != null && getExpectedType() != null &&
					!getExpectedType().isInstance(this.defaultObject)) {
				throw new IllegalArgumentException("Default object [" + this.defaultObject +
						"] of type [" + this.defaultObject.getClass().getName() +
						"] is not of expected type [" + getExpectedType().getName() + "]");
			}
			// Locate specified JNDI object.
			this.jndiObject = lookupWithFallback();
		}
	}

	/**
	 * Lookup variant that that returns the specified "defaultObject"
	 * (if any) in case of lookup failure.
	 * @return the located object, or the "defaultObject" as fallback
	 * @throws NamingException in case of lookup failure without fallback
	 * @see #setDefaultObject
	 */
	protected Object lookupWithFallback() throws NamingException {
		try {
			return lookup();
		}
		catch (TypeMismatchNamingException ex) {
			// Always let TypeMismatchNamingException through -
			// we don't want to fall back to the defaultObject in this case.
			throw ex;
		}
		catch (NamingException ex) {
			if (this.defaultObject != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("JNDI lookup failed - returning specified default object instead", ex);
				}
				else if (logger.isInfoEnabled()) {
					logger.info("JNDI lookup failed - returning specified default object instead: " + ex);
				}
				return this.defaultObject;
			}
			throw ex;
		}
	}


	/**
	 * Return the singleton JNDI object.
	 */
	public Object getObject() {
		return this.jndiObject;
	}

	public Class getObjectType() {
		if (this.proxyInterfaces != null) {
			if (this.proxyInterfaces.length == 1) {
				return this.proxyInterfaces[0];
			}
			else if (this.proxyInterfaces.length > 1) {
				return createCompositeInterface(this.proxyInterfaces);
			}
		}
		if (this.jndiObject != null) {
			return this.jndiObject.getClass();
		}
		else {
			return getExpectedType();
		}
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Create a composite interface Class for the given interfaces,
	 * implementing the given interfaces in one single Class.
	 * <p>The default implementation builds a JDK proxy class for the
	 * given interfaces.
	 * @param interfaces the interfaces to merge
	 * @return the merged interface as Class
	 * @see java.lang.reflect.Proxy#getProxyClass
	 */
	protected Class createCompositeInterface(Class[] interfaces) {
		return ClassUtils.createCompositeInterface(interfaces, this.beanClassLoader);
	}


	/**
	 * Inner class to just introduce an AOP dependency when actually creating a proxy.
	 */
	private static class JndiObjectProxyFactory {

		private static Object createJndiObjectProxy(JndiObjectFactoryBean jof) throws NamingException {
			// Create a JndiObjectTargetSource that mirrors the JndiObjectFactoryBean's configuration.
			JndiObjectTargetSource targetSource = new JndiObjectTargetSource();
			targetSource.setJndiTemplate(jof.getJndiTemplate());
			targetSource.setJndiName(jof.getJndiName());
			targetSource.setExpectedType(jof.getExpectedType());
			targetSource.setResourceRef(jof.isResourceRef());
			targetSource.setLookupOnStartup(jof.lookupOnStartup);
			targetSource.setCache(jof.cache);
			targetSource.afterPropertiesSet();

			// Create a proxy with JndiObjectFactoryBean's proxy interface and the JndiObjectTargetSource.
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setInterfaces(jof.proxyInterfaces);
			proxyFactory.setTargetSource(targetSource);
			return proxyFactory.getProxy(jof.beanClassLoader);
		}
	}

}
