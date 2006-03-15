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

package org.springframework.orm.jpa;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that creates a local JPA EntityManagerFactory instance.
 * Behaves like a EntityManagerFactory instance when used as bean
 * reference, e.g. for JpaTemplate's "entityManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * EntityManagerFactory instance is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings are usually read in from a <code>persistence.xml</code>
 * config file, residing in the class path - according to the JPA spec's
 * bootstrap mechanism. See the Java Persistence API specification for details.
 *
 * <p>This EntityManager handling strategy is most appropriate for
 * applications that solely use JPA for data access. If you want to set up
 * your persistence provider for global transactions, you will need to
 * deploy it into a full J2EE application server and access the deployed
 * EntityManagerFactory through JNDI (-> JndiObjectFactoryBean).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see javax.persistence.EntityManagerFactory#close
 */
public class LocalEntityManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class persistenceProviderClass;

	private String entityManagerName;

	private EntityManagerFactory entityManagerFactory;

	private Properties jpaProperties;


	/**
	/**
	 * Set the PersistenceProvider implementation class to use for creating
	 * the EntityManagerFactory. If not specified (which is the default),
	 * the <code>Persistence</code> class will be used to create the
	 * EntityManagerFactory, relying on JPA's autodetection mechanism.
	 * @see javax.persistence.spi.PersistenceProvider
	 */
	public void setPersistenceProviderClass(Class persistenceProviderClass) {
		if (persistenceProviderClass != null &&
				!PersistenceProvider.class.isAssignableFrom(persistenceProviderClass)) {
			throw new IllegalArgumentException(
					"serviceFactoryClass must implement [javax.persistence.spi.PersistenceProvider]");
		}
		this.persistenceProviderClass = persistenceProviderClass;
	}

	/**
	 * Set the name of the EntityManager configuration for the factory.
	 * <p>Default is none, indicating the default EntityManager configuration.
	 * The persistence provider will throw an exception if ambiguous
	 * EntityManager configurations are found.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String)
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setEntityManagerName(String entityManagerName) {
		this.entityManagerName = entityManagerName;
	}

	/**
	 * Set JPA properties, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		this.jpaProperties = jpaProperties;
	}


	/**
	 * Initialize the EntityManagerFactory for the given configuration.
	 * @throws javax.persistence.PersistenceException in case of JPA initialization errors
	 */
	public final void afterPropertiesSet() throws PersistenceException {
		if (this.persistenceProviderClass != null) {
			// Create EntityManagerFactory directly through PersistenceProvider.
			PersistenceProvider pp =
					(PersistenceProvider) BeanUtils.instantiateClass(this.persistenceProviderClass);
			this.entityManagerFactory = pp.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
			if (this.entityManagerFactory == null) {
				throw new IllegalStateException(
						"PersistenceProvider [" + this.persistenceProviderClass.getName() +
						"] did not return an EntityManagerFactory for name '" + this.entityManagerName + "'");
			}
		}
		else {
			// Let JPA perform its PersistenceProvider autodetection.
			this.entityManagerFactory =
					Persistence.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
		}
	}


	/**
	 * Return the singleton EntityManagerFactory.
	 */
	public Object getObject() {
		return this.entityManagerFactory;
	}

	public Class getObjectType() {
		return (this.entityManagerFactory != null) ?
		    this.entityManagerFactory.getClass() : EntityManagerFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Close the EntityManagerFactory on bean factory shutdown.
	 */
	public void destroy() {
		logger.info("Closing JPA EntityManagerFactory");
		this.entityManagerFactory.close();
	}

}
