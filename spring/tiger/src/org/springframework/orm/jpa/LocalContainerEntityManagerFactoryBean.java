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

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.util.ClassUtils;

/**
 * Powerful FactoryBean that creates a fully Spring-configured EntityManagerFactory
 * according to the container contract for JPA bootstrapping
 *
 * <p>As with LocalEntityManagerFactoryBean, configuration settings are usually read in
 * from a <code>META-INF/persistence.xml</code> config file, residing in the class path,
 * according to the general JPA configuration contract. However, this FactoryBean is
 * more flexible in that you can override the location of the <code>persistence.xml</code>
 * file, specify the JDBC DataSources to link to, etc. Furthermore, it allows for
 * pluggable class instrumentation through the Spring LoadTimeWeaver abstraction,
 * instead of being tied to a special VM agent specified on JVM startup.
 *
 * <p>Internally, this FactoryBean parses the <code>persistence.xml</code> file itself
 * and creates a corresponding PersistenceUnitInfo object (with further configuration
 * merged in, such as JDBC DataSources and the LoadTimeWeaver), to be passed to the
 * JPA PersistenceProvider. This corresponds to full-fledged local JPA container.
 *
 * <p>The exposed EntityManagerFactory object will implement all the interfaces of
 * the underlying native EntityManagerFactory returned by the PersistenceProvider,
 * plus the EntityManagerFactoryInfo interface which exposes additional metadata.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see #setPersistenceXmlLocation
 * @see #setJpaProperties
 * @see #setJpaVendorAdapter
 * @see #setLoadTimeWeaver
 * @see #setDataSource
 * @see EntityManagerFactoryInfo
 * @see LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.support.SharedEntityManagerBean
 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory
 */
public class LocalContainerEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean
		implements ResourceLoaderAware {

	private PersistenceUnitManager persistenceUnitManager;

	private final DefaultPersistenceUnitManager internalPersistenceUnitManager =
			new DefaultPersistenceUnitManager();

	private PersistenceUnitInfo persistenceUnitInfo;


	/**
	 * Set the PersistenceUnitManager to use for obtaining the JPA persistence unit
	 * that this FactoryBean is supposed to build an EntityManagerFactory for.
	 * <p>The default is to rely on the local settings specified on this FactoryBean,
	 * such as "persistenceXmlLocation", "dataSource" and "loadTimeWeaver".
	 * <p>For reuse of existing persistence unit configuration or more advanced forms
	 * of custom persistence unit handling, consider defining a separate
	 * PersistenceUnitManager bean (typically a DefaultPersistenceUnitManager instance)
	 * and linking it in here. <code>persistence.xml</code> location, DataSource
	 * configuration and LoadTimeWeaver will be defined on that separate
	 * DefaultPersistenceUnitManager bean in such a scenario.
	 * @see #setPersistenceXmlLocation
	 * @see #setDataSource
	 * @see #setLoadTimeWeaver
	 * @see org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager
	 */
	public void setPersistenceUnitManager(PersistenceUnitManager persistenceUnitManager) {
		this.persistenceUnitManager = persistenceUnitManager;
	}

	/**
	 * Set the location of the <code>persistence.xml</code> file
	 * we want to use. This is a Spring resource location.
	 * <p>Default is "classpath:META-INF/persistence.xml".
	 * <p><b>NOTE: Only applied if no external PersistenceUnitManager specified.</b>
	 * @param persistenceXmlLocation a Spring resource String
	 * identifying the location of the <code>persistence.xml</code> file
	 * that this LocalContainerEntityManagerFactoryBean should parse
	 * @see #setPersistenceUnitManager
	 */
	public void setPersistenceXmlLocation(String persistenceXmlLocation) {
		this.internalPersistenceUnitManager.setPersistenceXmlLocations(new String[] {persistenceXmlLocation});
	}

	/**
	 * Specify the JDBC DataSource that the JPA persistence provider is supposed
	 * to use for accessing the database. This is an alternative to keeping the
	 * JDBC configuration in <code>persistence.xml</code>, passing in a Spring-managed
	 * DataSource instead.
	 * <p>In JPA speak, a DataSource passed in here will be used as "nonJtaDataSource"
	 * on the PersistenceUnitInfo passed to the PersistenceProvider, overriding
	 * data source configuration in <code>persistence.xml</code> (if any).
	 * <p><b>NOTE: Only applied if no external PersistenceUnitManager specified.</b>
	 * @see javax.persistence.spi.PersistenceUnitInfo#getNonJtaDataSource()
	 * @see #setPersistenceUnitManager
	 */
	public void setDataSource(DataSource dataSource) {
		this.internalPersistenceUnitManager.setDataSourceLookup(new SingleDataSourceLookup(dataSource));
		this.internalPersistenceUnitManager.setDefaultDataSource(dataSource);
	}

	/**
	 * Specify the Spring LoadTimeWeaver to use for class instrumentation according
	 * to the JPA class transformer contract.
	 * <p>It is a not required to specify a LoadTimeWeaver: Most providers will be
	 * able to provide a subset of their functionality without class instrumentation
	 * as well, or operate with their VM agent specified on JVM startup.
	 * <p>In terms of Spring-provided weaving options, the most important ones are
	 * InstrumentationLoadTimeWeaver, which requires a Spring-specific (but very general)
	 * VM agent specified on JVM startup, and ReflectiveLoadTimeWeaver, which interacts
	 * with an underlying ClassLoader based on specific extended methods being available
	 * on it (for example, interacting with Spring's TomcatInstrumentableClassLoader).
	 * <p><b>NOTE: Only applied if no external PersistenceUnitManager specified.</b>
	 * @see org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver
	 * @see org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver
	 * @see org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader
	 * @see #setPersistenceUnitManager
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.internalPersistenceUnitManager.setLoadTimeWeaver(loadTimeWeaver);
	}
	/**
	 * Set the PersistenceUnitPostProcessors to be applied to the
	 * PersistenceUnitInfo used for creating this EntityManagerFactory.
	 * <p>Such post-processors can, for example, register further entity
	 * classes and jar files, in addition to the metadata read in from
	 * <code>persistence.xml</code>.
	 * <p><b>NOTE: Only applied if no external PersistenceUnitManager specified.</b>
	 * @see #setPersistenceUnitManager
	 */
	public void setPersistenceUnitPostProcessors(PersistenceUnitPostProcessor[] postProcessors) {
		this.internalPersistenceUnitManager.setPersistenceUnitPostProcessors(postProcessors);
	}


	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.internalPersistenceUnitManager.setResourceLoader(resourceLoader);
	}


	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		PersistenceUnitManager managerToUse = this.persistenceUnitManager;
		if (this.persistenceUnitManager == null) {
			this.internalPersistenceUnitManager.afterPropertiesSet();
			managerToUse = this.internalPersistenceUnitManager;
		}

		this.persistenceUnitInfo = determinePersistenceUnitInfo(managerToUse);

		PersistenceProvider provider = getPersistenceProvider();
		if (provider == null) {
			String providerClassName = this.persistenceUnitInfo.getPersistenceProviderClassName();
			try {
				Class providerClass = ClassUtils.forName(providerClassName);
				provider = (PersistenceProvider) BeanUtils.instantiateClass(providerClass);
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Cannot resolve provider class name '" + providerClassName + "'", ex);
			}
		}
		if (provider == null) {
			throw new IllegalStateException("Unable to determine persistence provider. " +
					"Please check configuration of " + getClass().getName() + "; " +
					"ideally specify the appropriate JpaVendorAdapter class for this provider.");
		}

		this.nativeEntityManagerFactory =
				provider.createContainerEntityManagerFactory(this.persistenceUnitInfo, getJpaPropertyMap());
		postProcessEntityManagerFactory(this.nativeEntityManagerFactory, this.persistenceUnitInfo);

		return this.nativeEntityManagerFactory;
	}


	/**
	 * Determine the PersistenceUnitInfo to use for the EntityManagerFactory
	 * created by this bean.
	 * <p>The default implementation reads in all persistence unit infos from
	 * <code>persistence.xml</code>, as defined in the JPA specification.
	 * If no entity manager name was specified, it takes the first info in the
	 * array as returned by the reader. Otherwise, it checks for a matching name.
	 * @param persistenceUnitManager the PersistenceUnitManager to obtain from
	 * @return the chosen PersistenceUnitInfo
	 */
	protected PersistenceUnitInfo determinePersistenceUnitInfo(PersistenceUnitManager persistenceUnitManager) {
		if (getPersistenceUnitName() != null) {
			return persistenceUnitManager.obtainPersistenceUnitInfo(getPersistenceUnitName());
		}
		else {
			return persistenceUnitManager.obtainDefaultPersistenceUnitInfo();
		}
	}

	/**
	 * Hook method allowing subclasses to customize the EntityManagerFactory
	 * after its creation via the PersistenceProvider.
	 * <p>The default implementation is empty.
	 * @param emf the newly created EntityManagerFactory we are working with
	 * @param pui the PersistenceUnitInfo used to configure the EntityManagerFactory
	 * @see javax.persistence.spi.PersistenceProvider#createContainerEntityManagerFactory
	 */
	protected void postProcessEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
	}


	public PersistenceUnitInfo getPersistenceUnitInfo() {
		return this.persistenceUnitInfo;
	}

	public String getPersistenceUnitName() {
		if (this.persistenceUnitInfo != null) {
			return this.persistenceUnitInfo.getPersistenceUnitName();
		}
		return super.getPersistenceUnitName();
	}

	public DataSource getDataSource() {
		if (this.persistenceUnitInfo != null) {
			return this.persistenceUnitInfo.getNonJtaDataSource();
		}
		return this.internalPersistenceUnitManager.getDefaultDataSource();
	}

}
