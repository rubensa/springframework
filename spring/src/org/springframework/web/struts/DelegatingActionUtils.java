/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.struts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * Common methods for various ways to make Struts delegate to
 * Spring-managed Actions.
 *
 * <p>As everything in Struts is based on concrete inheritance,
 * we have to provide an Action subclass (DelegatingActionProxy) and
 * two RequestProcessor subclasses (DelegatingRequestProcessor and
 * DelegatingTilesRequestProcessor). The only way to share common
 * functionality is a utility class like this one.
 *
 * @author Juergen Hoeller
 * @since 26.04.2004
 * @see DelegatingActionProxy
 * @see DelegatingRequestProcessor
 * @see DelegatingTilesRequestProcessor
 */
public abstract class DelegatingActionUtils {

	protected static final Log logger = LogFactory.getLog(DelegatingActionUtils.class);

	/**
	 * Fetch ContextLoaderPlugIn's WebApplicationContext from the
	 * ServletContext, containing the Struts Action beans to delegate to.
	 * @param actionServlet the associated ActionServlet
	 * @return the WebApplicationContext
	 * @throws IllegalStateException if no WebApplicationContext could be found
	 * @see ContextLoaderPlugIn#SERVLET_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext initWebApplicationContext(ActionServlet actionServlet)
			throws IllegalStateException {
		WebApplicationContext wac = (WebApplicationContext)
				actionServlet.getServletContext().getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_ATTRIBUTE);
		if (wac == null) {
			throw new IllegalStateException("Could not find ContextLoaderPlugIn's WebApplicationContext as " +
																			"ServletContext attribute [" + ContextLoaderPlugIn.SERVLET_CONTEXT_ATTRIBUTE +
																			"] - did you register " + ContextLoaderPlugIn.class.getName() + "?");
		}
		return wac;
	}

	/**
	 * Default implementation of Action bean determination, taking
	 * the mapping path and prepending the module prefix, if any.
	 * @param mapping the Struts ActionMapping
	 * @return the name of the Action bean
	 * @see org.apache.struts.action.ActionMapping#getPath
	 * @see org.apache.struts.config.ModuleConfig#getPrefix
	 */
	public static String determineActionBeanName(ActionMapping mapping) {
		String prefix = mapping.getModuleConfig().getPrefix();
		String path = mapping.getPath();
		String beanName = StringUtils.hasLength(prefix) ? prefix + path : path;
		if (logger.isDebugEnabled()) {
			logger.debug("DelegatingActionProxy with mapping path '" + path + "' and module prefix '" +
			             prefix + "' delegating to Spring bean with name [" + beanName + "]");
		}
		return beanName;
	}

}
