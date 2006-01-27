/**
 * Created on Jan 24, 2006
 *
 * $Id$
 * $Revision$
 */
package org.springframework.workflow.jbpm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextBeanFactoryReference;

/**
 * BeanFactoryLocator used for injecting Spring application context into JBPM. The difference/advantage over the traditional
 * SingletonBeanFactoryLocator is that it doesn not parse the bean factory; it is used internally by the jbpmSessionFactoryBean 
 * and it will register the bean factory/application context automatically under the name and 
 * and aliases of the bean. If there is only one BeanFactory registered then a null value can
 * be used with setBeanName method.
 * <p/> 
 * Note that in most cases, you don't have to use this class since 
 * it is used internally by LocalJbpmSessionFactoryBean.
 * 
 * @author Costin Leau
 *
 */
public class JbpmFactoryLocator implements BeanFactoryLocator, BeanFactoryAware, BeanNameAware {

	private static final Log logger = LogFactory.getLog(JbpmFactoryLocator.class);

	private String factoryName;

	protected static final Map beanFactories = new HashMap();
	protected static boolean canUseDefaultBeanFactory = true;
	protected static BeanFactoryReference defaultReference = null;

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
		// find the approapriate beanFactory reference

		BeanFactoryReference reference;
		if (beanFactory instanceof ApplicationContext)
			reference = new ContextBeanFactoryReference((ApplicationContext) beanFactory);
		else
			// simple implementation
			reference = new BeanFactoryReference() {
				private BeanFactory factory = beanFactory;

				public BeanFactory getFactory() {
					if (this.factory == null)
						throw new IllegalArgumentException("beanFactory already released");
					return this.factory;
				}

				public void release() throws FatalBeanException {
					this.factory = null;
				}
			};

		// add the factory as default if possible (if it's the only one)
		synchronized (JbpmFactoryLocator.class) {
			if (canUseDefaultBeanFactory) {
				if (defaultReference == null) {
					defaultReference = reference;
					if (logger.isDebugEnabled())
						logger.debug("default beanFactoryReference=" + defaultReference);
				}
				else {
					if (logger.isDebugEnabled())
						logger.debug("more then one beanFactory - default not possible to determine");
					canUseDefaultBeanFactory = false;
					defaultReference = null;
				}
			}
		}

		// add name
		addToMap(factoryName, reference);
		// add aliases
		String[] aliases = beanFactory.getAliases(factoryName);
		for (int i = 0; i < aliases.length; i++) {
			addToMap(aliases[i], reference);
		}
	}

	protected void addToMap(String fName, BeanFactoryReference reference) {
		if (logger.isDebugEnabled())
			logger.debug("adding key=" + fName + " w/ reference=" + reference);

		synchronized (beanFactories) {
			// override check
			if (beanFactories.containsKey(fName))
				throw new IllegalArgumentException("a beanFactoryReference already exists for key "
						+ factoryName);
			beanFactories.put(fName, reference);
		}
	}

	/**
	 * @see org.springframework.beans.factory.access.BeanFactoryLocator#useBeanFactory(java.lang.String)
	 */
	public BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException {
		// see if there is a default FactoryBean
		if (factoryKey == null) {
			if (!canUseDefaultBeanFactory)
				throw new IllegalArgumentException(
						"a non-null factoryKey needs to be specified as there are more then one factoryKeys available");
			return defaultReference;
		}

		return (BeanFactoryReference) beanFactories.get(factoryKey);
	}

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		factoryName = name;
	}

}
