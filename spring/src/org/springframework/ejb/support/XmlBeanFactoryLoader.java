/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.support;

import java.io.InputStream;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanFactoryLoader;
import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.ClassLoaderUtils;

/**
 * Implementation of the BeanFactoryLoader interface useful in EJBs
 * (although not tied to the EJB API).
 *
 * <p>This class will look for the JNDI environment key
 * "java:comp/env/ejb/BeanFactoryPath" for the classpath location
 * of an XML bean factory definition.
 *
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @since 20-Jul-2003
 * @version $Id$
 */
public class XmlBeanFactoryLoader implements BeanFactoryLoader {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Load the bean factory.
	 * @throws BootstrapException if the JNDI key is missing or if
	 * the factory cannot be loaded from this location
	 */
	public BeanFactory loadBeanFactory() throws BootstrapException {		
		String beanFactoryPath = null;
		try {
			beanFactoryPath = (String) (new JndiTemplate()).lookup(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			logger.info("BeanFactoryPath from JNDI is [" + beanFactoryPath + "]");
			InputStream is = ClassLoaderUtils.getResourceAsStream(beanFactoryPath);
			if (is == null) {
				throw new BootstrapException("Cannot load bean factory path '" + beanFactoryPath + "'", null);
			}
			ListableBeanFactory beanFactory = new XmlBeanFactory(is);
			logger.info("Loaded BeanFactory [" + beanFactory + "]");
			return beanFactory;
		}
		catch (NamingException ex) {
			throw new BootstrapException("Define an environment variable 'ejb/BeanFactoryPath' containing the location on the class path of an XmlBeanFactory"
						+ ex.getMessage(), ex);
		}		
		catch (BeanDefinitionStoreException ex) {
			throw new BootstrapException("Found resource at '" + beanFactoryPath + "' but it's not a valid Spring bean definition XML file: " + ex.getMessage(), null);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.support.BeanFactoryLoader#unloadBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void unloadBeanFactory(BeanFactory bf) throws FatalBeanException {
		// nothing to do in default implementation
	}
}
