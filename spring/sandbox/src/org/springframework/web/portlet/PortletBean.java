/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.portlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.context.support.PortletContextResourceLoader;

/**
 * Simple extension of <code>javax.portlet.GenericPortlet</code> that treats
 * its config parameters as bean properties.
 *
 * <p>A very handy superclass for any type of portlet. Type conversion is automatic.
 * It is also possible for subclasses to specify required properties.
 *
 * <p>This portlet leaves request handling to subclasses, inheriting the default
 * behaviour of GenericPortlet (<code>doDispatch</code>, <code>processAction</code>, etc).
 *
 * <p>This portlet superclass has no dependency on a Spring application context,
 * in contrast to the FrameworkPortlet class which loads its own context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 * @see #addRequiredProperty
 * @see #initPortletBean
 * @see #doDispatch
 * @see #processAction
 * @see FrameworkPortlet
 */
public abstract class PortletBean extends GenericPortlet {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 
	 * Set of required properties (Strings) that must be supplied as
	 * config parameters to this portlet.
	 */
	private final Set requiredProperties = new HashSet();

	
	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter. This method would
	 * normally be called from a subclass constructor.
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}

	/**
	 * Map config parameters onto bean properties of this portlet, and
	 * invoke subclass initialization.
	 * @throws PortletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 */
	public final void init() throws PortletException {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing portlet '" + getPortletName() + "'");
		}
		
		// Set bean properties from init parameters.
		try {
			PropertyValues pvs = new PortletConfigPropertyValues(getPortletConfig(), this.requiredProperties);
			BeanWrapper bw = new BeanWrapperImpl(this);
			ResourceLoader resourceLoader = new PortletContextResourceLoader(getPortletContext());
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
			initBeanWrapper(bw);
			bw.setPropertyValues(pvs);
		}
		catch (BeansException ex) {
			logger.error("Failed to set bean properties on portlet '" + getPortletName() + "'", ex);
			throw ex;
		}

		// let subclasses do whatever initialization they like
		initPortletBean();

		if (logger.isInfoEnabled()) {
			logger.info("Portlet '" + getPortletName() + "' configured successfully");
		}
	}
	
	/**
	 * Initialize the BeanWrapper for this PortletBean,
	 * possibly with custom editors.
	 * @param bw the BeanWrapper to initialize
	 * @throws BeansException if thrown by BeanWrapper methods
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}

	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this portlet will have been set before this
	 * method is invoked. This default implementation does nothing.
	 * @throws PortletException if subclass initialization fails
	 */
	protected void initPortletBean() throws PortletException {
	}


	/**
	 * PropertyValues implementation created from PortletConfig init parameters.
	 */
	private static class PortletConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new PortletConfigPropertyValues.
		 * @param config PortletConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * @throws PortletException if any required properties are missing
		 */
		private PortletConfigPropertyValues(PortletConfig config, Set requiredProperties)
			throws PortletException {
				
			Set missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet(requiredProperties) : null;

			Enumeration en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String property = (String) en.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// fail if we are still missing properties
			if (missingProps != null && missingProps.size() > 0) {
				throw new PortletException(
					"Initialization from PortletConfig for portlet '" + config.getPortletName() +
					"' failed; the following required properties were missing: " +
					StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
