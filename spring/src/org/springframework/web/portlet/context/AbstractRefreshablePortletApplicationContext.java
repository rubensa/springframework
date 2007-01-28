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

package org.springframework.web.portlet.context;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.context.request.SessionScope;
import org.springframework.web.context.support.ServletContextAwareProcessor;

/**
 * {@link org.springframework.context.support.AbstractRefreshableApplicationContext}
 * subclass which implements the {@link ConfigurablePortletApplicationContext}
 * interface for portlet environments. Provides a "configLocations" property,
 * to be populated through the ConfigurablePortletApplicationContext interface
 * on portlet application startup.
 *
 * <p>This class is as easy to subclass as AbstractRefreshableApplicationContext:
 * All you need to implements is the {@link #loadBeanDefinitions} method;
 * see the superclass javadoc for details. Note that implementations are supposed
 * to load bean definitions from the files specified by the locations returned
 * by the {@link #getConfigLocations} method.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs, as implemented by
 * {@link org.springframework.core.io.DefaultResourceLoader}.
 *
 * <p><b>This is the portlet context to be subclassed for a different bean definition format.</b>
 * Such a context implementation can be specified as "contextClass" init-param
 * for FrameworkPortlet, replacing the default {@link XmlPortletApplicationContext}.
 * It will then automatically receive the "contextConfigLocation" init-param.
 *
 * <p>Note that Portlet-based context implementations are generally supposed
 * to configure themselves based on the configuration received through the
 * {@link ConfigurablePortletApplicationContext} interface. In contrast, a standalone
 * application context might allow for configuration in custom startup code
 * (for example, {@link org.springframework.context.support.GenericApplicationContext}).
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 * @see #loadBeanDefinitions
 * @see org.springframework.web.portlet.context.ConfigurablePortletApplicationContext#setConfigLocations
 * @see XmlPortletApplicationContext
 */
public abstract class AbstractRefreshablePortletApplicationContext extends AbstractRefreshableApplicationContext
		implements WebApplicationContext, ConfigurablePortletApplicationContext {

	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Portlet context that this context runs in */
	private PortletContext portletContext;

	/** Portlet config that this context runs in */
	private PortletConfig portletConfig;

	/** Namespace of this context, or null if root */
	private String namespace;

	/** Paths to XML configuration files */
	private String[] configLocations;


	public AbstractRefreshablePortletApplicationContext() {
		setDisplayName("Root PortletApplicationContext");
	}

	public void setParent(ApplicationContext parent) {
		super.setParent(parent);
		if (parent instanceof WebApplicationContext) {
			this.servletContext = ((WebApplicationContext) parent).getServletContext();
		}
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public void setPortletContext(PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public PortletContext getPortletContext() {
		return this.portletContext;
	}

	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
		if (portletConfig != null && this.portletContext == null) {
			this.portletContext = portletConfig.getPortletContext();
		}
	}

	public PortletConfig getPortletConfig() {
		return this.portletConfig;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		if (namespace != null) {
			setDisplayName("PortletApplicationContext for namespace '" + namespace + "'");
		}
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void setConfigLocations(String[] configLocations) {
		this.configLocations = configLocations;
	}

	public String[] getConfigLocations() {
		return (!ObjectUtils.isEmpty(this.configLocations) ? this.configLocations : getDefaultConfigLocations());
	}

	/**
	 * Return the default config locations to use, for the case where no explicit
	 * config locations have been specified.
	 * <p>Default implementation returns null, requiring explicit config locations.
	 * @see #setConfigLocations
	 */
	protected String[] getDefaultConfigLocations() {
		return null;
	}


	/**
	 * Register request/session scopes, a {@link PortletContextAwareProcessor}, etc.
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.registerScope(SCOPE_REQUEST, new RequestScope());
		beanFactory.registerScope(SCOPE_SESSION, new SessionScope(false));
		beanFactory.registerScope(SCOPE_GLOBAL_SESSION, new SessionScope(true));

		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
		beanFactory.addBeanPostProcessor(new PortletContextAwareProcessor(this.portletContext, this.portletConfig));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		beanFactory.ignoreDependencyInterface(PortletContextAware.class);
		beanFactory.ignoreDependencyInterface(PortletConfigAware.class);
	}

	/**
	 * This implementation supports file paths beneath the root of the PortletContext.
	 * @see PortletContextResource
	 */
	protected Resource getResourceByPath(String path) {
		return new PortletContextResource(this.portletContext, path);
	}

	/**
	 * This implementation supports pattern matching in unexpanded WARs too.
	 * @see PortletContextResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PortletContextResourcePatternResolver(this);
	}

}
