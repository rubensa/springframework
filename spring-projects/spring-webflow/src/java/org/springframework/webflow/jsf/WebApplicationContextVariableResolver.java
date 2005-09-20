/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.webflow.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.DelegatingVariableResolver;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Note, this shadows a class that has been added to Spring for v1.2.5. It's
 * here so that there is no need for Web Flow to depend on a CVS version of
 * Spring. As soon as Spring 1.2.5 is available, this class may be removed, and
 * that version may be used instead.
 * 
 * Special <code>VariableResolver</code> that exposes the Spring
 * <code>WebApplicationContext</code> instance under a variable named
 * "webApplicationContext".
 * 
 * <p>
 * In contrast to DelegatingVariableResolver, this VariableResolver does <i>not</i>
 * resolve JSF variable names as Spring bean names. It rather exposes Spring's
 * root WebApplicationContext <i>itself</i> under a special name. JSF-managed
 * beans can then use Spring's WebApplicationContext API to retrieve
 * Spring-managed beans, access resources, etc.
 * 
 * <p>
 * Configure this resolver in your <code>faces-config.xml</code> file as
 * follows:
 * 
 * <pre>
 *  &lt;application&gt;
 *    ...
 *    &lt;variable-resolver&gt;org.springframework.web.jsf.WebApplicationContextVariableResolver&lt;/variable-resolver&gt;
 *  &lt;/application&gt;
 * </pre>
 * 
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2.5
 * @see DelegatingVariableResolver
 * @see FacesContextUtils#getWebApplicationContext
 */
public class WebApplicationContextVariableResolver extends VariableResolver {

	/**
	 * Name of the exposed WebApplicationContext variable:
	 * "webApplicationContext".
	 */
	public static final String WEB_APPLICATION_CONTEXT_VARIABLE_NAME = "webApplicationContext";

	protected final VariableResolver originalVariableResolver;

	/**
	 * Create a new WebApplicationContextVariableResolver, using the given
	 * original VariableResolver.
	 * <p>
	 * A JSF implementation will automatically pass its original resolver into
	 * the constructor of a configured resolver, provided that there is a
	 * corresponding constructor argument.
	 * @param originalVariableResolver the original VariableResolver
	 */
	public WebApplicationContextVariableResolver(VariableResolver originalVariableResolver) {
		this.originalVariableResolver = originalVariableResolver;
	}

	/**
	 * Check for the special "webApplicationContext" variable first, then
	 * delegate to the original VariableResolver.
	 * <p>
	 * If no WebApplicationContext is available, all requests will be delegated
	 * to the original VariableResolver.
	 */
	public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
		Object value = null;
		if (WEB_APPLICATION_CONTEXT_VARIABLE_NAME.equals(name)) {
			value = getWebApplicationContext(context);
		}
		if (value == null) {
			value = this.originalVariableResolver.resolveVariable(context, name);
		}
		return value;
	}

	/**
	 * Retrieve the WebApplicationContext reference to expose.
	 * <p>
	 * Default implementation delegates to FacesContextUtils, returning
	 * <code>null</code> if no WebApplicationContext found.
	 * @param facesContext the current JSF context
	 * @return the Spring web application context
	 * @see FacesContextUtils#getWebApplicationContext
	 */
	protected WebApplicationContext getWebApplicationContext(FacesContext facesContext) {
		return FacesContextUtils.getWebApplicationContext(facesContext);
	}

}
