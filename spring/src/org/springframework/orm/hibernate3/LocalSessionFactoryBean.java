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

package org.springframework.orm.hibernate3;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

/**
 * FactoryBean that creates a local Hibernate SessionFactory instance.
 * Behaves like a SessionFactory instance when used as bean reference, e.g.
 * for HibernateTemplate's "sessionFactory" property. Note that switching
 * to JndiObjectFactoryBean is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying JDBC DataSource) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings can either be read from a Hibernate XML file,
 * specified as "configLocation", or completely via this class. A typical
 * local configuration consists of one or more "mappingResources", various
 * "hibernateProperties" (not strictly necessary), and a "dataSource" that the
 * SessionFactory should use. The latter can also be specified via Hibernate
 * properties, but "dataSource" supports any Spring-configured DataSource,
 * instead of relying on Hibernate's own connection providers.
 *
 * <p>This SessionFactory handling strategy is appropriate for most types of
 * applications, from Hibernate-only single database apps to ones that need
 * distributed transactions. Either HibernateTransactionManager or
 * JtaTransactionManager can be used for transaction demarcation, the latter
 * only being necessary for transactions that span multiple databases.
 *
 * <p>Registering a SessionFactory with JNDI is only advisable when using
 * Hibernate's JCA Connector, i.e. when the application server cares for
 * initialization. Else, portability is rather limited: Manual JNDI binding
 * isn't supported by some application servers (e.g. Tomcat). Unfortunately,
 * JCA has drawbacks too: Its setup is container-specific and can be tedious.
 *
 * <p>This factory bean will by default expose a transaction-aware SessionFactory
 * proxy, letting data access code work with the plain Hibernate SessionFactory
 * and its <code>getCurrentSession()</code> method, while still being able to
 * participate in current Spring-managed transactions: with any transaction
 * management strategy, either local or JTA / EJB CMT, and any transaction
 * synchronization mechanism, either Spring or JTA. Furthermore,
 * <code>getCurrentSession()</code> will also seamlessly work with
 * a request-scoped Session managed by OpenSessionInViewFilter/Interceptor.
 *
 * <p>Requires Hibernate 3.0.3 or later. Note that this factory will always use
 * "on_close" as default Hibernate connection release mode, for the reason that
 * this is appropriate for most Spring-based applications (in particular when
 * using Spring's HibernateTransactionManager). Hibernate 3.0 used "on_close"
 * as its own default too; however, Hibernate 3.1 changed this to "auto"
 * (i.e. "after_statement" or "after_transaction").
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see HibernateTemplate#setSessionFactory
 * @see HibernateTransactionManager#setSessionFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see #setExposeTransactionAwareSessionFactory
 * @see org.hibernate.SessionFactory#getCurrentSession()
 * @see HibernateTransactionManager
 */
public class LocalSessionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	private static ThreadLocal configTimeDataSourceHolder = new ThreadLocal();

	private static ThreadLocal configTimeTransactionManagerHolder = new ThreadLocal();

	private static ThreadLocal configTimeLobHandlerHolder = new ThreadLocal();

	/**
	 * Return the DataSource for the currently configured Hibernate SessionFactory,
	 * to be used by LocalDataSourceConnectionProvoder.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setDataSource
	 * @see LocalDataSourceConnectionProvider
	 */
	public static DataSource getConfigTimeDataSource() {
		return (DataSource) configTimeDataSourceHolder.get();
	}

	/**
	 * Return the JTA TransactionManager for the currently configured Hibernate
	 * SessionFactory, to be used by LocalTransactionManagerLookup.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setJtaTransactionManager
	 * @see LocalTransactionManagerLookup
	 */
	public static TransactionManager getConfigTimeTransactionManager() {
		return (TransactionManager) configTimeTransactionManagerHolder.get();
	}

	/**
	 * Return the LobHandler for the currently configured Hibernate SessionFactory,
	 * to be used by UserType implementations like ClobStringType.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setLobHandler
	 * @see org.springframework.orm.hibernate3.support.ClobStringType
	 * @see org.springframework.orm.hibernate3.support.BlobByteArrayType
	 * @see org.springframework.orm.hibernate3.support.BlobSerializableType
	 */
	public static LobHandler getConfigTimeLobHandler() {
		return (LobHandler) configTimeLobHandlerHolder.get();
	}


	protected final Log logger = LogFactory.getLog(getClass());

	private Class configurationClass = Configuration.class;

	private Resource[] configLocations;

	private Resource[] mappingLocations;

	private Resource[] cacheableMappingLocations;

	private Resource[] mappingJarLocations;

	private Resource[] mappingDirectoryLocations;

	private Properties hibernateProperties;

	private DataSource dataSource;

	private boolean useTransactionAwareDataSource = false;

	private boolean exposeTransactionAwareSessionFactory = true;

	private TransactionManager jtaTransactionManager;

	private LobHandler lobHandler;

	private Interceptor entityInterceptor;

	private NamingStrategy namingStrategy;

	private Properties entityCacheStrategies;

	private Properties collectionCacheStrategies;

	private TypeDefinitionBean[] typeDefinitions;

	private FilterDefinition[] filterDefinitions;

	private Map eventListeners;

	private boolean schemaUpdate = false;

	private Configuration configuration;

	private SessionFactory sessionFactory;


	/**
	 * Specify the Hibernate Configuration class to use.
	 * Default is "org.hibernate.cfg.Configuration"; any subclass of
	 * this default Hibernate Configuration class can be specified.
	 * <p>Can be set to "org.hibernate.cfg.AnnotationConfiguration" for
	 * using Hibernate3 annotation support (initially only available as
	 * alpha download separate from the main Hibernate3 distribution).
	 * <p>Annotated packages and annotated classes can be specified via the
	 * corresponding tags in "hibernate.cfg.xml" then, so this will usually
	 * be combined with a "configLocation" property that points at such a
	 * standard Hibernate configuration file.
	 * @see #setConfigLocation
	 * @see org.hibernate.cfg.Configuration
	 * @see org.hibernate.cfg.AnnotationConfiguration
	 */
	public void setConfigurationClass(Class configurationClass) {
		if (configurationClass == null || !Configuration.class.isAssignableFrom(configurationClass)) {
			throw new IllegalArgumentException(
					"configurationClass must be assignable to [org.hibernate.cfg.Configuration]");
		}
		this.configurationClass = configurationClass;
	}

	/**
	 * Set the location of a single Hibernate XML config file, for example as
	 * classpath resource "classpath:hibernate.cfg.xml".
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocations = new Resource[] {configLocation};
	}

	/**
	 * Set the locations of multiple Hibernate XML config files, for example as
	 * classpath resources "classpath:hibernate.cfg.xml,classpath:extension.cfg.xml".
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocations(Resource[] configLocations) {
		this.configLocations = configLocations;
	}

	/**
	 * Set Hibernate mapping resources to be found in the class path,
	 * like "example.hbm.xml" or "mypackage/example.hbm.xml".
	 * Analogous to mapping entries in a Hibernate XML config file.
	 * Alternative to the more generic setMappingLocations method.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see #setMappingLocations
	 * @see org.hibernate.cfg.Configuration#addResource
	 */
	public void setMappingResources(String[] mappingResources) {
		this.mappingLocations = new Resource[mappingResources.length];
		for (int i = 0; i < mappingResources.length; i++) {
			this.mappingLocations[i] = new ClassPathResource(mappingResources[i].trim());
		}
	}

	/**
	 * Set locations of Hibernate mapping files, for example as classpath
	 * resource "classpath:example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, for example relative paths like
	 * "WEB-INF/mappings/example.hbm.xml" when running in an application context.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addInputStream
	 */
	public void setMappingLocations(Resource[] mappingLocations) {
		this.mappingLocations = mappingLocations;
	}

	/**
	 * Set locations of cacheable Hibernate mapping files, for example as web app
	 * resource "/WEB-INF/mapping/example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, as long as the resource can be resolved
	 * in the file system.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addCacheableFile(java.io.File)
	 */
	public void setCacheableMappingLocations(Resource[] cacheableMappingLocations) {
		this.cacheableMappingLocations = cacheableMappingLocations;
	}

	/**
	 * Set locations of jar files that contain Hibernate mapping resources,
	 * like "WEB-INF/lib/example.hbm.jar".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addJar(java.io.File)
	 */
	public void setMappingJarLocations(Resource[] mappingJarLocations) {
		this.mappingJarLocations = mappingJarLocations;
	}

	/**
	 * Set locations of directories that contain Hibernate mapping resources,
	 * like "WEB-INF/mappings".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addDirectory(java.io.File)
	 */
	public void setMappingDirectoryLocations(Resource[] mappingDirectoryLocations) {
		this.mappingDirectoryLocations = mappingDirectoryLocations;
	}

	/**
	 * Set Hibernate properties, like "hibernate.dialect".
	 * <p>Can be used to override values in a Hibernate XML config file,
	 * or to specify all necessary properties locally.
	 * <p>Note: Do not specify a transaction provider here when using
	 * Spring-driven transactions. It is also advisable to omit connection
	 * provider settings and use a Spring-set DataSource instead.
	 * @see #setDataSource
	 */
	public void setHibernateProperties(Properties hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	/**
	 * Return the Hibernate properties, if any. Mainly available for
	 * configuration through property paths that specify individual keys.
	 */
	public Properties getHibernateProperties() {
		if (this.hibernateProperties == null) {
			this.hibernateProperties = new Properties();
		}
		return this.hibernateProperties;
	}

	/**
	 * Set the DataSource to be used by the SessionFactory.
	 * If set, this will override corresponding settings in Hibernate properties.
	 * <p>If this is set, the Hibernate settings should not define
	 * a connection provider to avoid meaningless double configuration.
	 * <p>If using HibernateTransactionManager as transaction strategy, consider
	 * proxying your target DataSource with a LazyConnectionDataSourceProxy.
	 * This defers fetching of an actual JDBC Connection until the first JDBC
	 * Statement gets executed, even within JDBC transactions (as performed by
	 * HibernateTransactionManager). Such lazy fetching is particularly beneficial
	 * for read-only operations, in particular if the chances of resolving the
	 * result in the second-level cache are high.
	 * <p>As JTA and transactional JNDI DataSources already provide lazy enlistment
	 * of JDBC Connections, LazyConnectionDataSourceProxy does not add value with
	 * JTA (i.e. Spring's JtaTransactionManager) as transaction strategy.
	 * @see #setUseTransactionAwareDataSource
	 * @see LocalDataSourceConnectionProvider
	 * @see HibernateTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set whether to use a transaction-aware DataSource for the SessionFactory,
	 * i.e. whether to automatically wrap the passed-in DataSource with Spring's
	 * TransactionAwareDataSourceProxy.
	 * <p>Default is "false": LocalSessionFactoryBean is usually used with Spring's
	 * HibernateTransactionManager or JtaTransactionManager, both of which work nicely
	 * on a plain JDBC DataSource. Hibernate Sessions and their JDBC Connections are
	 * fully managed by the Hibernate/JTA transaction infrastructure in such a scenario.
	 * <p>If you switch this flag to "true", Spring's Hibernate access will be able to
	 * <i>participate in JDBC-based transactions managed outside of Hibernate</i>
	 * (for example, by Spring's DataSourceTransactionManager). This can be convenient
	 * if you need a different local transaction strategy for another O/R mapping tool,
	 * for example, but still want Hibernate access to join into those transactions.
	 * <p>A further benefit of this option is that <i>plain Sessions opened directly
	 * via the SessionFactory</i>, outside of Spring's Hibernate support, will still
	 * participate in active Spring-managed transactions. However, consider using
	 * Hibernate's <code>getCurrentSession()</code> method instead (see javadoc of
	 * "exposeTransactionAwareSessionFactory" property).
	 * <p>As a further effect, using a transaction-aware DataSource will <i>apply
	 * remaining transaction timeouts to all created JDBC Statements</i>. This means
	 * that all operations performed by the SessionFactory will automatically
	 * participate in Spring-managed transaction timeouts, not just queries.
	 * This adds value even for HibernateTransactionManager, but only on Hibernate 3.0,
	 * as there is a direct transaction timeout facility in Hibernate 3.1.
	 * <p><b>WARNING:</b> When using a transaction-aware JDBC DataSource in combination
	 * with OpenSessionInViewFilter/Interceptor, whether participating in JTA or
	 * external JDBC-based transactions, it is strongly recommended to set Hibernate's
	 * Connection release mode to "after_transaction" or "after_statement", which
	 * guarantees proper Connection handling in such a scenario. In contrast to that,
	 * HibernateTransactionManager generally requires release mode "on_close".
	 * <p>Note: If you want to use Hibernate's Connection release mode "after_statement"
	 * with a DataSource specified on this LocalSessionFactoryBean (for example, a
	 * JTA-aware DataSource fetched from JNDI), switch this setting to "true".
	 * Else, the ConnectionProvider used underneath will vote against aggressive
	 * release and thus silently switch to release mode "after_transaction".
	 * @see #setDataSource
	 * @see #setExposeTransactionAwareSessionFactory
	 * @see org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 * @see HibernateTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	public void setUseTransactionAwareDataSource(boolean useTransactionAwareDataSource) {
		this.useTransactionAwareDataSource = useTransactionAwareDataSource;
	}

	/**
	 * Set whether to expose a transaction-aware proxy for the SessionFactory,
	 * returning the Session that's associated with the current Spring-managed
	 * transaction on <code>getCurrentSession()</code>, if any.
	 * <p>Default is "true", letting data access code work with the plain
	 * Hibernate SessionFactory and its <code>getCurrentSession()</code> method,
	 * while still being able to participate in current Spring-managed transactions:
	 * with any transaction management strategy, either local or JTA / EJB CMT,
	 * and any transaction synchronization mechanism, either Spring or JTA.
	 * Furthermore, <code>getCurrentSession()</code> will also seamlessly work with
	 * a request-scoped Session managed by OpenSessionInViewFilter/Interceptor.
	 * <p>Turn this flag off to expose the plain Hibernate SessionFactory with
	 * Hibernate's default <code>getCurrentSession()</code> behavior, where
	 * Hibernate 3.0.x only supports plain JTA synchronization. On Hibernate 3.1+,
	 * such a plain SessionFactory will by default have a SpringSessionContext
	 * registered to nevertheless provide Spring-managed Sessions; this can be
	 * overridden through the corresponding Hibernate property
	 * "hibernate.current_session_context_class".
	 * @see org.hibernate.SessionFactory#getCurrentSession()
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see HibernateTransactionManager
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 * @see SpringSessionContext
	 */
	public void setExposeTransactionAwareSessionFactory(boolean exposeTransactionAwareSessionFactory) {
		this.exposeTransactionAwareSessionFactory = exposeTransactionAwareSessionFactory;
	}

	/**
	 * Set the JTA TransactionManager to be used for Hibernate's
	 * TransactionManagerLookup. If set, this will override corresponding
	 * settings in Hibernate properties. Allows to use a Spring-managed
	 * JTA TransactionManager for Hibernate's cache synchronization.
	 * <p>Note: If this is set, the Hibernate settings should not define a
	 * transaction manager lookup to avoid meaningless double configuration.
	 * @see LocalTransactionManagerLookup
	 */
	public void setJtaTransactionManager(TransactionManager jtaTransactionManager) {
		this.jtaTransactionManager = jtaTransactionManager;
	}

	/**
	 * Set the LobHandler to be used by the SessionFactory.
	 * Will be exposed at config time for UserType implementations.
	 * @see #getConfigTimeLobHandler
	 * @see org.hibernate.usertype.UserType
	 * @see org.springframework.orm.hibernate3.support.ClobStringType
	 * @see org.springframework.orm.hibernate3.support.BlobByteArrayType
	 * @see org.springframework.orm.hibernate3.support.BlobSerializableType
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this factory.
	 * <p>Such an interceptor can either be set at the SessionFactory level, i.e. on
	 * LocalSessionFactoryBean, or at the Session level, i.e. on HibernateTemplate,
	 * HibernateInterceptor, and HibernateTransactionManager. It's preferable to set
	 * it on LocalSessionFactoryBean or HibernateTransactionManager to avoid repeated
	 * configuration and guarantee consistent behavior in transactions.
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 * @see org.hibernate.cfg.Configuration#setInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Set a Hibernate NamingStrategy for the SessionFactory, determining the
	 * physical column and table names given the info in the mapping document.
	 * @see org.hibernate.cfg.Configuration#setNamingStrategy
	 */
	public void setNamingStrategy(NamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Specify the cache strategies for entities (persistent classes or named entities).
	 * This configuration setting corresponds to the &lt;class-cache&gt; entry
	 * in the "hibernate.cfg.xml" configuration format.
	 * <p>For example:
	 * <pre>
	 * &lt;property name="entityCacheStrategies"&gt;
	 *   &lt;props&gt;
	 *     &lt;prop key="com.mycompany.Customer"&gt;read-write&lt;/prop&gt;
	 *     &lt;prop key="com.mycompany.Product"&gt;read-only,myRegion&lt;/prop&gt;
	 *   &lt;/props&gt;
	 * &lt;/property&gt;</pre>
	 * Note that appending a cache region name (with a comma separator) is only
	 * supported on Hibernate 3.1, where this functionality is publically available.
	 * @param entityCacheStrategies properties that define entity cache strategies,
	 * with class names as keys and cache concurrency strategies as values
	 * @see org.hibernate.cfg.Configuration#setCacheConcurrencyStrategy(String, String)
	 */
	public void setEntityCacheStrategies(Properties entityCacheStrategies) {
		this.entityCacheStrategies = entityCacheStrategies;
	}

	/**
	 * Specify the cache strategies for persistent collections (with specific roles).
	 * This configuration setting corresponds to the &lt;collection-cache&gt; entry
	 * in the "hibernate.cfg.xml" configuration format.
	 * <p>For example:
	 * <pre>
	 * &lt;property name="collectionCacheStrategies"&gt;
	 *   &lt;props&gt;
	 *     &lt;prop key="com.mycompany.Order.items">read-write&lt;/prop&gt;
	 *     &lt;prop key="com.mycompany.Product.categories"&gt;read-only,myRegion&lt;/prop&gt;
	 *   &lt;/props&gt;
	 * &lt;/property&gt;</pre>
	 * Note that appending a cache region name (with a comma separator) is only
	 * supported on Hibernate 3.1, where this functionality is publically available.
	 * @param collectionCacheStrategies properties that define collection cache strategies,
	 * with collection roles as keys and cache concurrency strategies as values
	 * @see org.hibernate.cfg.Configuration#setCollectionCacheConcurrencyStrategy(String, String)
	 */
	public void setCollectionCacheStrategies(Properties collectionCacheStrategies) {
		this.collectionCacheStrategies = collectionCacheStrategies;
	}

	/**
	 * Specify the Hibernate type definitions to register with the SessionFactory,
	 * as Spring TypeDefinitionBean instances. This is an alternative to specifying
	 * <&lt;typedef&gt; elements in Hibernate mapping files.
	 * <p>Unfortunately, Hibernate itself does not define a complete object that
	 * represents a type definition, hence the need for Spring's TypeDefinitionBean.
	 * @see TypeDefinitionBean
	 * @see org.hibernate.cfg.Mappings#addTypeDef(String, String, java.util.Properties)
	 */
	public void setTypeDefinitions(TypeDefinitionBean[] typeDefinitions) {
		this.typeDefinitions = typeDefinitions;
	}

	/**
	 * Specify the Hibernate FilterDefinitions to register with the SessionFactory.
	 * This is an alternative to specifying <&lt;filter-def&gt; elements in
	 * Hibernate mapping files.
	 * <p>Typically, the passed-in FilterDefinition objects will have been defined
	 * as Spring FilterDefinitionFactoryBeans, probably as inner beans within the
	 * LocalSessionFactoryBean definition.
	 * @see FilterDefinitionFactoryBean
	 * @see org.hibernate.cfg.Configuration#addFilterDefinition
	 */
	public void setFilterDefinitions(FilterDefinition[] filterDefinitions) {
		this.filterDefinitions = filterDefinitions;
	}

	/**
	 * Specify the Hibernate event listeners to register, with listener types
	 * as keys and listener objects as values.
	 * <p>See the Hibernate documentation for further details on listener types
	 * and associated listener interfaces.
	 * @param eventListeners Map with listener type Strings as keys and
	 * listener objects as values
	 * @see org.hibernate.cfg.Configuration#setListener(String, Object)
	 */
	public void setEventListeners(Map eventListeners) {
		this.eventListeners = eventListeners;
	}

	/**
	 * Set whether to execute a schema update after SessionFactory initialization.
	 * <p>For details on how to make schema update scripts work, see the Hibernate
	 * documentation, as this class leverages the same schema update script support
	 * in org.hibernate.cfg.Configuration as Hibernate's own SchemaUpdate tool.
	 * @see org.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	public void setSchemaUpdate(boolean schemaUpdate) {
		this.schemaUpdate = schemaUpdate;
	}


	/**
	 * Initialize the SessionFactory for the given or the default location.
	 * @throws IllegalArgumentException in case of illegal property values
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, HibernateException, IOException {
		// Create Configuration instance.
		Configuration config = newConfiguration();

		if (this.dataSource != null) {
			// Make given DataSource available for SessionFactory configuration.
			configTimeDataSourceHolder.set(this.dataSource);
		}

		if (this.jtaTransactionManager != null) {
			// Make Spring-provided JTA TransactionManager available.
			configTimeTransactionManagerHolder.set(this.jtaTransactionManager);
		}

		if (this.lobHandler != null) {
			// Make given LobHandler available for SessionFactory configuration.
			// Do early because because mapping resource might refer to custom types.
			configTimeLobHandlerHolder.set(this.lobHandler);
		}

		try {
			// Set connection release mode "on_close" as default.
			// This was the case for Hibernate 3.0; Hibernate 3.1 changed
			// it to "auto" (i.e. "after_statement" or "after_transaction").
			// However, for Spring's resource management (in particular for
			// HibernateTransactionManager), "on_close" is the better default.
			config.setProperty(Environment.RELEASE_CONNECTIONS, ConnectionReleaseMode.ON_CLOSE.toString());

			if (!this.exposeTransactionAwareSessionFactory) {
				// Not exposing a SessionFactory proxy with transaction-aware
				// getCurrentSession() method -> set Hibernate 3.1 CurrentSessionContext
				// implementation instead, providing the Spring-managed Session that way.
				// Can be overridden by a custom value for corresponding Hibernate property.
				config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS,
						"org.springframework.orm.hibernate3.SpringSessionContext");
			}

			if (this.entityInterceptor != null) {
				// Set given entity interceptor at SessionFactory level.
				config.setInterceptor(this.entityInterceptor);
			}

			if (this.namingStrategy != null) {
				// Pass given naming strategy to Hibernate Configuration.
				config.setNamingStrategy(this.namingStrategy);
			}

			if (this.typeDefinitions != null) {
				// Register specified Hibernate type definitions.
				Mappings mappings = config.createMappings();
				for (int i = 0; i < this.typeDefinitions.length; i++) {
					TypeDefinitionBean typeDef = this.typeDefinitions[i];
					mappings.addTypeDef(typeDef.getTypeName(), typeDef.getTypeClass(), typeDef.getParameters());
				}
			}

			if (this.configLocations != null) {
				for (int i = 0; i < this.configLocations.length; i++) {
					// Load Hibernate configuration from given location.
					config.configure(this.configLocations[i].getURL());
				}
			}

			if (this.hibernateProperties != null) {
				// Add given Hibernate properties to Configuration.
				config.addProperties(this.hibernateProperties);
			}

			if (this.dataSource != null) {
				boolean actuallyTransactionAware =
						(this.useTransactionAwareDataSource || this.dataSource instanceof TransactionAwareDataSourceProxy);
				// Set Spring-provided DataSource as Hibernate ConnectionProvider.
				config.setProperty(Environment.CONNECTION_PROVIDER,
						actuallyTransactionAware ?
						TransactionAwareDataSourceConnectionProvider.class.getName() :
						LocalDataSourceConnectionProvider.class.getName());
			}

			if (this.jtaTransactionManager != null) {
				// Set Spring-provided JTA TransactionManager as Hibernate property.
				config.setProperty(
						Environment.TRANSACTION_MANAGER_STRATEGY, LocalTransactionManagerLookup.class.getName());
			}

			if (this.mappingLocations != null) {
				// Register given Hibernate mapping definitions, contained in resource files.
				for (int i = 0; i < this.mappingLocations.length; i++) {
					config.addInputStream(this.mappingLocations[i].getInputStream());
				}
			}

			if (this.cacheableMappingLocations != null) {
				// Register given cacheable Hibernate mapping definitions, read from the file system.
				for (int i = 0; i < this.cacheableMappingLocations.length; i++) {
					config.addCacheableFile(this.cacheableMappingLocations[i].getFile());
				}
			}

			if (this.mappingJarLocations != null) {
				// Register given Hibernate mapping definitions, contained in jar files.
				for (int i = 0; i < this.mappingJarLocations.length; i++) {
					Resource resource = this.mappingJarLocations[i];
					config.addJar(resource.getFile());
				}
			}

			if (this.mappingDirectoryLocations != null) {
				// Register all Hibernate mapping definitions in the given directories.
				for (int i = 0; i < this.mappingDirectoryLocations.length; i++) {
					File file = this.mappingDirectoryLocations[i].getFile();
					if (!file.isDirectory()) {
						throw new IllegalArgumentException(
								"Mapping directory location [" + this.mappingDirectoryLocations[i] +
								"] does not denote a directory");
					}
					config.addDirectory(file);
				}
			}

			if (this.entityCacheStrategies != null) {
				// Register cache strategies for mapped entities.
				for (Enumeration classNames = this.entityCacheStrategies.propertyNames(); classNames.hasMoreElements();) {
					String className = (String) classNames.nextElement();
					String[] strategyAndRegion =
							StringUtils.commaDelimitedListToStringArray(this.entityCacheStrategies.getProperty(className));
					if (strategyAndRegion.length > 1) {
						config.setCacheConcurrencyStrategy(className, strategyAndRegion[0], strategyAndRegion[1]);
					}
					else if (strategyAndRegion.length > 0) {
						config.setCacheConcurrencyStrategy(className, strategyAndRegion[0]);
					}
				}
			}

			if (this.collectionCacheStrategies != null) {
				// Register cache strategies for mapped collections.
				for (Enumeration collRoles = this.collectionCacheStrategies.propertyNames(); collRoles.hasMoreElements();) {
					String collRole = (String) collRoles.nextElement();
					String[] strategyAndRegion =
							StringUtils.commaDelimitedListToStringArray(this.collectionCacheStrategies.getProperty(collRole));
					if (strategyAndRegion.length > 1) {
						config.setCollectionCacheConcurrencyStrategy(collRole, strategyAndRegion[0], strategyAndRegion[1]);
					}
					else if (strategyAndRegion.length > 0) {
						config.setCollectionCacheConcurrencyStrategy(collRole, strategyAndRegion[0]);
					}
				}
			}

			if (this.filterDefinitions != null) {
				// Register specified Hibernate FilterDefinitions.
				for (int i = 0; i < this.filterDefinitions.length; i++) {
					config.addFilterDefinition(this.filterDefinitions[i]);
				}
			}

			if (this.eventListeners != null) {
				// Register specified Hibernate event listeners.
				for (Iterator it = this.eventListeners.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					String listenerType = (String) entry.getKey();
					Object listenerObject = entry.getValue();
					config.setListener(listenerType, listenerObject);
				}
			}

			// Perform custom post-processing in subclasses.
			postProcessConfiguration(config);

			// Build SessionFactory instance.
			logger.info("Building new Hibernate SessionFactory");
			this.configuration = config;
			SessionFactory sf = newSessionFactory(config);

			// Wrap SessionFactory with transaction-aware proxy, if demanded.
			if (this.exposeTransactionAwareSessionFactory) {
			 	this.sessionFactory = getTransactionAwareSessionFactoryProxy(sf);
			}
			else {
				this.sessionFactory = sf;
			}
		}

		finally {
			if (this.dataSource != null) {
				// Reset DataSource holder.
				configTimeDataSourceHolder.set(null);
			}

			if (this.jtaTransactionManager != null) {
				// Reset TransactionManager holder.
				configTimeTransactionManagerHolder.set(null);
			}

			if (this.lobHandler != null) {
				// Reset LobHandler holder.
				configTimeLobHandlerHolder.set(null);
			}
		}

		// Execute schema update if requested.
		if (this.schemaUpdate) {
			updateDatabaseSchema();
		}
	}
	

	private String[] parseCacheStrategy(Properties strategies, String key) {
		String strategy = null;
		String region = null;
		String attribute = strategies.getProperty(key);
		String[] tokens = StringUtils.commaDelimitedListToStringArray(attribute);
		if (tokens.length > 0) {
			strategy = tokens[0];
		}
		if (tokens.length > 1) {
			region = tokens[1];
		}
		return new String[] {strategy, region};
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the Configuration instance used for SessionFactory creation.
	 * The properties of this LocalSessionFactoryBean will be applied to
	 * the Configuration object that gets returned here.
	 * <p>The default implementation creates a new Configuration instance.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom Configuration subclass.
	 * @return the Configuration instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see org.hibernate.cfg.Configuration#Configuration()
	 */
	protected Configuration newConfiguration() throws HibernateException {
		return (Configuration) BeanUtils.instantiateClass(this.configurationClass);
	}

	/**
	 * To be implemented by subclasses that want to to perform custom
	 * post-processing of the Configuration object after this FactoryBean
	 * performed its default initialization.
	 * @param config the current Configuration object
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	protected void postProcessConfiguration(Configuration config) throws HibernateException {
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the SessionFactory instance, creating it via the given Configuration
	 * object that got prepared by this LocalSessionFactoryBean.
	 * <p>The default implementation invokes Configuration's buildSessionFactory.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom SessionFactoryImpl subclass.
	 * @param config Configuration prepared by this LocalSessionFactoryBean
	 * @return the SessionFactory instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see org.hibernate.cfg.Configuration#buildSessionFactory
	 */
	protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
		return config.buildSessionFactory();
	}

	/**
	 * Wrap the given SessionFactory with a proxy that delegates every method call
	 * to it but delegates <code>getCurrentSession</code> calls to SessionFactoryUtils,
	 * for participating in Spring-managed transactions.
	 * @param target the original SessionFactory to wrap
	 * @return the wrapped SessionFactory
	 * @see org.hibernate.SessionFactory#getCurrentSession()
	 * @see SessionFactoryUtils#doGetSession(org.hibernate.SessionFactory, boolean)
	 */
	protected SessionFactory getTransactionAwareSessionFactoryProxy(SessionFactory target) {
		Class sfInterface = SessionFactory.class;
		if (target instanceof SessionFactoryImplementor) {
			sfInterface = SessionFactoryImplementor.class;
		}
		return (SessionFactory) Proxy.newProxyInstance(sfInterface.getClassLoader(),
				new Class[] {sfInterface}, new TransactionAwareInvocationHandler(target));
	}


	/**
	 * Execute schema drop script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaExport class, to be invoked on application setup.
	 * <p>Fetch the LocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>LocalSessionFactoryBean lsfb = (LocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws DataAccessException in case of script execution errors
	 * @see org.hibernate.cfg.Configuration#generateDropSchemaScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaExport#drop
	 */
	public void dropDatabaseSchema() throws DataAccessException {
		logger.info("Dropping database schema for Hibernate SessionFactory");
		HibernateTemplate hibernateTemplate = new HibernateTemplate(this.sessionFactory);
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					Dialect dialect = Dialect.getDialect(configuration.getProperties());
					String[] sql = configuration.generateDropSchemaScript(dialect);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute schema creation script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaExport class, to be invoked on application setup.
	 * <p>Fetch the LocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>LocalSessionFactoryBean lsfb = (LocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws DataAccessException in case of script execution errors
	 * @see org.hibernate.cfg.Configuration#generateSchemaCreationScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaExport#create
	 */
	public void createDatabaseSchema() throws DataAccessException {
		logger.info("Creating database schema for Hibernate SessionFactory");
		HibernateTemplate hibernateTemplate = new HibernateTemplate(this.sessionFactory);
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					final Dialect dialect = Dialect.getDialect(configuration.getProperties());
					String[] sql = configuration.generateSchemaCreationScript(dialect);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute schema update script, determined by the Configuration object
	 * used for creating the SessionFactory. A replacement for Hibernate's
	 * SchemaUpdate class, for automatically executing schema update scripts
	 * on application startup. Can also be invoked manually.
	 * <p>Fetch the LocalSessionFactoryBean itself rather than the exposed
	 * SessionFactory to be able to invoke this method, e.g. via
	 * <code>LocalSessionFactoryBean lsfb = (LocalSessionFactoryBean) ctx.getBean("&mySessionFactory");</code>.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script.
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see #setSchemaUpdate
	 * @see org.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see org.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	public void updateDatabaseSchema() throws HibernateException {
		logger.info("Updating database schema for Hibernate SessionFactory");
		HibernateTemplate hibernateTemplate = new HibernateTemplate(this.sessionFactory);
		hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_NEVER);
		hibernateTemplate.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection con = session.connection();
					final Dialect dialect = Dialect.getDialect(configuration.getProperties());
					DatabaseMetadata metadata = new DatabaseMetadata(con, dialect);
					String[] sql = configuration.generateSchemaUpdateScript(dialect, metadata);
					executeSchemaScript(con, sql);
					return null;
				}
			}
		);
	}

	/**
	 * Execute the given schema script on the given JDBC Connection.
	 * Will log unsuccessful statements and continue to execute.
	 * @param con the JDBC Connection to execute the script on
	 * @param sql the SQL statements to execute
	 * @throws SQLException if thrown by JDBC methods
	 */
	protected void executeSchemaScript(Connection con, String[] sql) throws SQLException {
		if (sql != null && sql.length > 0) {
			boolean oldAutoCommit = con.getAutoCommit();
			if (!oldAutoCommit) {
				con.setAutoCommit(true);
			}
			try {
				Statement stmt = con.createStatement();
				try {
					for (int i = 0; i < sql.length; i++) {
						logger.debug("Executing schema statement: " + sql[i]);
						try {
							stmt.executeUpdate(sql[i]);
						}
						catch (SQLException ex) {
							logger.warn("Unsuccessful schema statement: " + sql[i], ex);
						}
					}
				}
				finally {
					JdbcUtils.closeStatement(stmt);
				}
			}
			finally {
				if (!oldAutoCommit) {
					con.setAutoCommit(false);
				}
			}
		}
	}


	/**
	 * Return the Configuration object used to build the SessionFactory.
	 * Allows access to configuration metadata stored there (rarely needed).
	 */
	public Configuration getConfiguration() {
		return configuration;
	}


	/**
	 * Return the singleton SessionFactory.
	 */
	public Object getObject() {
		return this.sessionFactory;
	}

	public Class getObjectType() {
		return (this.sessionFactory != null) ? this.sessionFactory.getClass() : SessionFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Close the SessionFactory on bean factory shutdown.
	 */
	public void destroy() throws HibernateException {
		logger.info("Closing Hibernate SessionFactory");
		if (this.dataSource != null) {
			// Make given DataSource available for potential SchemaExport,
			// which unfortunately reinstantiates a ConnectionProvider.
			configTimeDataSourceHolder.set(this.dataSource);
		}
		try {
			this.sessionFactory.close();
		}
		finally {
			if (this.dataSource != null) {
				// Reset DataSource holder.
				configTimeDataSourceHolder.set(null);
			}
		}
	}


	/**
	 * Invocation handler that delegates <code>getCurrentSession()</code> calls
	 * to SessionFactoryUtils, for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private final SessionFactory target;

		public TransactionAwareInvocationHandler(SessionFactory target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on SessionFactory/SessionFactoryImplementor interface coming in...

			if (method.getName().equals("getCurrentSession")) {
				// Handle getCurrentSession method: return transactional Session, if any.
				try {
					return SessionFactoryUtils.doGetSession((SessionFactory) proxy, false);
				}
				catch (IllegalStateException ex) {
					throw new HibernateException(ex.getMessage());
				}
			}
			else if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			// Invoke method on target SessionFactory.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
