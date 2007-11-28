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

package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the
 * {@link org.springframework.beans.factory.ListableBeanFactory} and
 * {@link BeanDefinitionRegistry} interfaces: a full-fledged bean factory
 * based on bean definition objects.
 *
 * <p>Typical usage is registering all bean definitions first (possibly read
 * from a bean definition file), before accessing beans. Bean definition lookup
 * is therefore an inexpensive operation in a local bean definition table,
 * operating on pre-built bean definition metadata objects.
 *
 * <p>Can be used as a standalone bean factory, or as a superclass for custom
 * bean factories. Note that readers for specific bean definition formats are
 * typically implemented separately rather than as bean factory subclasses:
 * see for example {@link PropertiesBeanDefinitionReader} and
 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>For an alternative implementation of the
 * {@link org.springframework.beans.factory.ListableBeanFactory} interface,
 * have a look at {@link StaticListableBeanFactory}, which manages existing
 * bean instances rather than creating new ones based on bean definitions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 16 April 2001
 * @see StaticListableBeanFactory
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	/** Whether to allow re-registration of a different definition with the same name */
	private boolean allowBeanDefinitionOverriding = true;

	/** Whether to allow eager class loading even for lazy-init beans */
	private boolean allowEagerClassLoading = true;

	/** Map of bean definition objects, keyed by bean name */
	private final Map beanDefinitionMap = CollectionFactory.createConcurrentMapIfPossible(16);

	/** List of bean definition names, in registration order */
	private final List beanDefinitionNames = new ArrayList();

	/** Resolver to use for checking if a bean definition is an autowire candidate */
	private AutowireCandidateResolver autowireCandidateResolver = AutowireUtils.createAutowireCandidateResolver();

	/** Map from dependency type to corresponding autowired value */
	private final Map resolvableDependencies = new HashMap();


	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 * @param parentBeanFactory the parent BeanFactory
	 */
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}


	/**
	 * Set a custom autowire candidate resolver for this BeanFactory to use
	 * when deciding whether a bean definition should be considered as a
	 * candidate for autowiring.
	 */
	public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	/**
	 * Return the autowire candidate resolver for this BeanFactory (never <code>null</code>).
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}

	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is "true".
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Set whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * <p>Default is "true". Turn this flag off to suppress class loading
	 * for lazy-init beans unless such a bean is explicitly requested.
	 * In particular, by-type lookups will then simply ignore bean definitions
	 * without resolved class name, instead of loading the bean classes on
	 * demand just to perform a type check.
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}


	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
		}
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		synchronized (this.beanDefinitionMap) {
			return StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}

	public String[] getBeanNamesForType(Class type) {
		return getBeanNamesForType(type, true, true);
	}

	public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean allowEagerInit) {
		List result = new ArrayList();

		// Check all bean definitions.
		String[] beanDefinitionNames = null;
		synchronized (this.beanDefinitionMap) {
			beanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
		}
		for (int i = 0; i < beanDefinitionNames.length; i++) {
			String beanName = beanDefinitionNames[i];
			// Only consider bean as eligible if the bean name
			// is not defined as alias for some other bean.
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// Only check bean definition if it is complete.
					if (!mbd.isAbstract() &&
							(allowEagerInit || mbd.hasBeanClass() || !mbd.isLazyInit() || this.allowEagerClassLoading)) {
						// In case of FactoryBean, match object created by FactoryBean.
						Class beanClass = predictBeanType(beanName, mbd, true);
						boolean isFactoryBean = (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass));
						boolean matchFound =
								(allowEagerInit || !requiresEagerInitForType(beanName, isFactoryBean, mbd.getFactoryBeanName())) &&
								(includePrototypes || isSingleton(beanName)) && isTypeMatch(beanName, type);
						if (!matchFound && isFactoryBean) {
							// In case of FactoryBean, try to match FactoryBean instance itself next.
							beanName = FACTORY_BEAN_PREFIX + beanName;
							matchFound = (includePrototypes || mbd.isSingleton()) && isTypeMatch(beanName, type);
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring bean class loading failure for bean '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				}
				catch (BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring unresolvable metadata in bean definition '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				}
			}
		}

		// Check singletons too, to catch manually registered singletons.
		String[] singletonNames = getSingletonNames();
		for (int i = 0; i < singletonNames.length; i++) {
			String beanName = singletonNames[i];
			// Only check if manually registered.
			if (!containsBeanDefinition(beanName)) {
				// In case of FactoryBean, match object created by FactoryBean.
				if (isFactoryBean(beanName)) {
					if ((includePrototypes || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
		}

		return StringUtils.toStringArray(result);
	}

	/**
	 * Check whether the specified bean would need to be eagerly initialized
	 * in order to determine its type.
	 * @param beanName the name of the bean
	 * @param isFactoryBean whether the bean itself is a FactoryBean
	 * @param factoryBeanName a factory-bean reference that the bean definition
	 * defines a factory method for
	 * @return whether eager initialization is necessary
	 */
	private boolean requiresEagerInitForType(String beanName, boolean isFactoryBean, String factoryBeanName) {
		return (isFactoryBean && !containsSingleton(beanName)) ||
				(factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}

	public Map getBeansOfType(Class type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean allowEagerInit)
			throws BeansException {

		String[] beanNames = getBeanNamesForType(type, includePrototypes, allowEagerInit);
		Map result = new LinkedHashMap(beanNames.length);
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			try {
				result.put(beanName, getBean(beanName));
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					if (isCurrentlyInCreation(bce.getBeanName())) {
						if (this.logger.isDebugEnabled()) {
							this.logger.debug("Ignoring match to currently created bean '" + beanName + "': " + ex.getMessage());
						}
						onSuppressedException(ex);
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory interface
	//---------------------------------------------------------------------

	public void registerResolvableDependency(Class dependencyType, Object autowiredValue) {
		Assert.notNull(dependencyType, "Type must not be null");
		Assert.isTrue((autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue)),
				"Value [" + autowiredValue + "] does not implement specified type [" + dependencyType.getName() + "]");
		this.resolvableDependencies.put(dependencyType, autowiredValue);
	}

	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {

		// Consider FactoryBeans as autowiring candidates.
		boolean isFactoryBean = (descriptor != null && descriptor.getDependencyType() != null &&
				FactoryBean.class.isAssignableFrom(descriptor.getDependencyType()));
		if (isFactoryBean) {
			beanName = BeanFactoryUtils.transformedBeanName(beanName);
		}

		if (!containsBeanDefinition(beanName)) {
			if (containsSingleton(beanName)) {
				return true;
			}
			else if (getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
				// No bean definition found in this factory -> delegate to parent.
				return ((ConfigurableListableBeanFactory) getParentBeanFactory()).isAutowireCandidate(beanName, descriptor);
			}
		}

		return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanName), descriptor);
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * @param beanName the name of the bean definition to check
	 * @param mbd the merged bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor) {
		return getAutowireCandidateResolver().isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName, getAliases(beanName)), descriptor);
	}

	protected String determinePrimaryCandidate(Map candidateBeans, Class type) {
		String primaryBeanName = null;
		for (Iterator it = candidateBeans.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String candidateBeanName = (String) entry.getKey();
			if (isPrimary(candidateBeanName, entry.getValue())) {
				if (primaryBeanName != null) {
					throw new NoSuchBeanDefinitionException(type,
							"more than one 'primary' bean found among candidates: " + candidateBeans.keySet());
				}
				primaryBeanName = candidateBeanName;
			}
		}
		return primaryBeanName;
	}

	/**
	 * Return whether the bean definition for the given bean name has been
	 * marked as a primary bean.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance
	 * @return whether the given bean qualifies as primary
	 */
	protected boolean isPrimary(String beanName, Object beanInstance) {
		return ((containsBeanDefinition(beanName) && getMergedLocalBeanDefinition(beanName).isPrimary()) ||
				this.resolvableDependencies.values().contains(beanInstance));
	}

	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	public void preInstantiateSingletons() throws BeansException {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Pre-instantiating singletons in " + this);
		}

		synchronized (this.beanDefinitionMap) {
			for (Iterator it = this.beanDefinitionNames.iterator(); it.hasNext();) {
				String beanName = (String) it.next();
				RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
				if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
					if (isFactoryBean(beanName)) {
						FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
						if (factory instanceof SmartFactoryBean && ((SmartFactoryBean) factory).isEagerInit()) {
							getBean(beanName);
						}
					}
					else {
						getBean(beanName);
					}
				}
			}
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry interface
	//---------------------------------------------------------------------

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "'beanName' must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		synchronized (this.beanDefinitionMap) {
			Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
			if (oldBeanDefinition != null) {
				if (!this.allowBeanDefinitionOverriding) {
					throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
							"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
							"': There is already [" + oldBeanDefinition + "] bound.");
				}
				else {
					if (this.logger.isInfoEnabled()) {
						this.logger.info("Overriding bean definition for bean '" + beanName +
								"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
					}
				}
			}
			else {
				this.beanDefinitionNames.add(beanName);
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);

			resetBeanDefinition(beanName);
		}
	}

	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		synchronized (this.beanDefinitionMap) {
			BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.remove(beanName);
			if (bd == null) {
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No bean named '" + beanName + "' found in " + this);
				}
				throw new NoSuchBeanDefinitionException(beanName);
			}
			this.beanDefinitionNames.remove(beanName);

			resetBeanDefinition(beanName);
		}
	}

	/**
	 * Reset all bean definition caches for the given bean,
	 * including the caches of beans that are derived from it.
	 * @param beanName the name of the bean to reset
	 */
	protected void resetBeanDefinition(String beanName) {
		// Remove the merged bean definition for the given bean, if already created.
		clearMergedBeanDefinition(beanName);

		// Remove corresponding bean from singleton cache, if any. Shouldn't usually
		// be necessary, rather just meant for overriding a context's default beans
		// (e.g. the default StaticMessageSource in a StaticApplicationContext).
		synchronized (getSingletonMutex()) {
			destroySingleton(beanName);
		}

		// Reset all bean definitions that have the given bean as parent
		// (recursively).
		for (Iterator it = this.beanDefinitionNames.iterator(); it.hasNext();) {
			String bdName = (String) it.next();
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.get(bdName);
				if (beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	protected Map findAutowireCandidates(String beanName, Class requiredType, DependencyDescriptor descriptor) {
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType);
		Map result = new LinkedHashMap(candidateNames.length);
		for (Iterator it = this.resolvableDependencies.keySet().iterator(); it.hasNext();) {
			Class autowiringType = (Class) it.next();
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = this.resolvableDependencies.get(autowiringType);
				if (autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue)) {
					autowiringValue = ((ObjectFactory) autowiringValue).getObject();
				}
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		for (int i = 0; i < candidateNames.length; i++) {
			String candidateName = candidateNames[i];
			if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, descriptor)) {
				result.put(candidateName, getBean(candidateName));
			}
		}
		return result;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.arrayToCommaDelimitedString(getBeanDefinitionNames()));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		}
		else {
			sb.append("parent: " + ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}

}
