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

package org.springframework.web.struts;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.config.ModuleConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;

/**
 * Subclass of Struts' default RequestProcessor that looks up Spring-managed
 * Struts 1.1 Actions defined in ContextLoaderPlugIn's WebApplicationContext.
 *
 * <p>In the Struts config file, you can either specify the original Action class
 * (as when generated by XDoclet), or no Action class at all. In any case, Struts
 * will delegate to an Action bean in the ContextLoaderPlugIn context.
 *
 * <pre>
 * &lt;action path="/login" type="myapp.MyAction"/&gt;</pre>
 *
 * or
 *
 * <pre>
 * &lt;action path="/login"/&gt;</pre>
 *
 * The name of the Action bean in the WebApplicationContext will be
 * determined from the mapping path and module prefix. This can be
 * customized by overriding the <code>determineActionBeanName</code> method.
 *
 * <p>Example:
 * <ul>
 * <li>mapping path "/login" -> bean name "/login"<br>
 * <li>mapping path "/login", module prefix "/mymodule" ->
 * bean name "/mymodule/login"
 * </ul>
 *
 * <p>A corresponding bean definition in the ContextLoaderPlugin
 * context looks as follows, being able to fully leverage
 * Spring's configuration facilities:
 *
 * <pre>
 * &lt;bean name="/login" class="myapp.MyAction"&gt;
 *   &lt;property name="..."&gt;...&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Note that you can use a single ContextLoaderPlugIn for all Struts modules.
 * That context can in turn be loaded from multiple XML files, for example split
 * according to Struts modules. Alternatively, define one ContextLoaderPlugIn per
 * Struts module, specifying appropriate "contextConfigLocation" parameters.
 * In both cases, the Spring bean name has to include the module prefix.
 *
 * <p>If you also need the Tiles setup functionality of the original
 * TilesRequestProcessor, use DelegatingTilesRequestProcessor. As there's just
 * a single central class to customize in Struts, we have to provide another
 * subclass here, covering both the Tiles and the Spring delegation aspect.
 *
 * <p>If this RequestProcessor conflicts with the need for a different
 * RequestProcessor subclass (other than TilesRequestProcessor), consider
 * using {@link DelegatingActionProxy DelegatingActionProxy} as Struts
 * Action type in your struts-config.
 *
 * <p>The default implementation delegates to the DelegatingActionUtils
 * class as fas as possible, to reuse as much code as possible despite
 * the need to provide two RequestProcessor subclasses. If you need to
 * subclass yet another RequestProcessor, take this class as a template,
 * delegating to DelegatingActionUtils just like it.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #determineActionBeanName
 * @see DelegatingTilesRequestProcessor
 * @see DelegatingActionProxy
 * @see DelegatingActionUtils
 * @see ContextLoaderPlugIn
 */
public class DelegatingRequestProcessor extends RequestProcessor {

	private WebApplicationContext webApplicationContext;

	public void init(ActionServlet actionServlet, ModuleConfig moduleConfig) throws ServletException {
		super.init(actionServlet, moduleConfig);
		if (actionServlet != null) {
			this.webApplicationContext = initWebApplicationContext(actionServlet, moduleConfig);
		}
	}

	/**
	 * Fetch ContextLoaderPlugIn's WebApplicationContext from the
	 * ServletContext, containing the Struts Action beans to delegate to.
	 * @param actionServlet the associated ActionServlet
	 * @param moduleConfig the associated ModuleConfig
	 * @return the WebApplicationContext
	 * @throws IllegalStateException if no WebApplicationContext could be found
	 * @see DelegatingActionUtils#getRequiredWebApplicationContext
	 * @see ContextLoaderPlugIn#SERVLET_CONTEXT_PREFIX
	 */
	protected WebApplicationContext initWebApplicationContext(
			ActionServlet actionServlet, ModuleConfig moduleConfig) throws IllegalStateException {

		return DelegatingActionUtils.getRequiredWebApplicationContext(actionServlet, moduleConfig);
	}

	/**
	 * Return the WebApplicationContext that this processor delegates to.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	/**
	 * Override the base class method to return the delegate action.
	 * @see #getDelegateAction
	 */
	protected Action processActionCreate(
			HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) throws IOException {
		try {
			return getDelegateAction(mapping);
		}
		catch (NoSuchBeanDefinitionException ex) {
			return super.processActionCreate(request, response, mapping);
		}
	}

	/**
	 * Return the delegate Action for the given mapping.
	 * <p>The default implementation determines a bean name from the
	 * given ActionMapping and looks up the corresponding bean in the
	 * WebApplicationContext.
	 * @param mapping the Struts ActionMapping
	 * @return the delegate Action
	 * @throws BeansException if thrown by WebApplicationContext methods
	 * @see #determineActionBeanName
	 */
	protected Action getDelegateAction(ActionMapping mapping) throws BeansException {
		String beanName = determineActionBeanName(mapping);
		return (Action) this.webApplicationContext.getBean(beanName, Action.class);
	}

	/**
	 * Determine the name of the Action bean, to be looked up in
	 * the WebApplicationContext.
	 * <p>The default implementation takes the mapping path and
	 * prepends the module prefix, if any.
	 * @param mapping the Struts ActionMapping
	 * @return the name of the Action bean
	 * @see DelegatingActionUtils#determineActionBeanName
	 * @see org.apache.struts.action.ActionMapping#getPath
	 * @see org.apache.struts.config.ModuleConfig#getPrefix
	 */
	protected String determineActionBeanName(ActionMapping mapping) {
		return DelegatingActionUtils.determineActionBeanName(mapping);
	}

}
