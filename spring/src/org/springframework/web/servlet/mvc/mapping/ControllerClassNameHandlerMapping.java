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

package org.springframework.web.servlet.mvc.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Implementation of {@link HandlerMapping} that follows a simple convention for generating
 * URL path mappings from the class names of registered {@link Controller} beans.
 * <p/>
 * For simple {@link Controller} implementations (those that handle a single request type),
 * the convention is to take the {@link ClassUtils#getShortName short name} of the <code>Class</code>,
 * remove the 'Controller' suffix if it exists and return the remaining text, lowercased, as
 * the mapping, with a leading <code>/</code>. For example:
 * <ul>
 * 	<li><code>WelcomeController</code> -> <code>/welcome</code></li>
 * 	<li><code>HomeController</code> -> <code>/home</code></li>
 * </ul>
 * <p/>
 * For {@link MultiActionController MultiActionControllers} then a similar mapping is registed
 * except that all sub-paths are registed using the trailing wildcard pattern <code>/*</code>.
 * For example:
 * <ul>
 * 	<li><code>AdminController</code> -> <code>/welcome/*</code></li>
 * 	<li><code>CatalogController</code> -> <code>/catalog/*</code></li>
 * </ul>
 * For {@link MultiActionController} it is often useful to this this mapping strategy in
 * conjunction with the {@link org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class ControllerClassNameHandlerMapping extends AbstractUrlHandlerMapping implements HandlerMapping {

	private static final Log logger = LogFactory.getLog(ControllerClassNameHandlerMapping.class);

	/**
	 * Common suffix at the end of {@link Controller} implementation classes. Removed when
	 * generating the URL path.
	 */
	private static final String CONTROLLER_SUFFIX = "Controller";


	/**
	 * Simply invokes {@link #detectHandlers()} method. Sub-classes may choose to override this
	 * but must remember to invoke the super method.
	 */
	protected void initApplicationContext() throws BeansException {
		detectHandlers();
	}

	/**
	 * Detects all the {@link Controller} beans registered in the
	 * {@link org.springframework.context.ApplicationContext} and registers a
	 * URL path mapping for each one based on rules defined {@link ControllerClassNameHandlerMapping here}.
	 * @see #generatePathMapping(Class)
	 */
	private void detectHandlers() {
		String[] beanNames = getApplicationContext().getBeanNamesForType(Controller.class);
		for (int i = 0; i < beanNames.length; i++) {
			String controllerBeanName = beanNames[i];
			Class controllerClass = getApplicationContext().getType(controllerBeanName);
			String urlPath = generatePathMapping(controllerClass);

			if (logger.isDebugEnabled()) {
				logger.debug("Registering Controller '" + controllerBeanName
								+ "' as handler for URL path '" + urlPath + "'.");
			}

			registerHandler(urlPath, controllerBeanName);
		}
	}

	/**
	 * Generates the actual URL path for the given {@link Controller} class. Sub-classes
	 * may choose to customize the paths that are generated by overriding this method.
	 */
	protected String generatePathMapping(Class controllerClass) {
		StringBuffer pathMapping = new StringBuffer("/");
		String className = ClassUtils.getShortName(controllerClass.getName());
		if (className.endsWith(CONTROLLER_SUFFIX)) {
			pathMapping.append(className.substring(0, className.indexOf(CONTROLLER_SUFFIX)).toLowerCase());
		}
		if (MultiActionController.class.isAssignableFrom(controllerClass)) {
			pathMapping.append("/*");
		}
		return pathMapping.toString();
	}
}
