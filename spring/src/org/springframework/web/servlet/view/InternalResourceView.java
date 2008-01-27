/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.web.servlet.view;

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ContextExposingHttpServletRequest;
import org.springframework.web.util.WebUtils;

/**
 * Wrapper for a JSP or other resource within the same web application.
 * Exposes model objects as request attributes and forwards the request to
 * the specified resource URL using a RequestDispatcher.
 *
 * <p>A URL for this view is supposed to specify a resource within the web
 * application, suitable for RequestDispatcher's <code>forward</code> or
 * <code>include</code> method.
 *
 * <p>If operating within an already included request or within a response that
 * has already been committed, this view will fall back to an include instead of
 * a forward. This can be enforced by calling <code>response.flushBuffer()</code>
 * (which will commit the response) before rendering the view.
 *
 * <p>Typical usage with InternalResourceViewResolver would look as follows,
 * from the perspective of the DispatcherServlet context definition:
 *
 * <pre class="code">&lt;bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver"&gt;
 *   &lt;property name="prefix" value="/WEB-INF/jsp/"/&gt;
 *   &lt;property name="suffix" value=".jsp"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Every view name returned from a handler will be translated to a JSP
 * resource (for example: "myView" -> "/WEB-INF/jsp/myView.jsp"), using
 * this view class by default.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see javax.servlet.RequestDispatcher#forward
 * @see javax.servlet.RequestDispatcher#include
 * @see javax.servlet.ServletResponse#flushBuffer
 * @see InternalResourceViewResolver
 */
public class InternalResourceView extends AbstractUrlBasedView {

	private static final String DISPATCHED_PATH_ATTRIBUTE =
			InternalResourceView.class.getName() + ".DISPATCHED_PATH";


	private boolean alwaysInclude = false;

	private boolean exposeContextBeansAsAttributes = false;

	private boolean preventDispatchLoop = false;


	/**
	 * Constructor for use as a bean.
	 * @see #setUrl
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView() {
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView(String url) {
		super(url);
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 * @param alwaysInclude whether to always include the view rather than forward to it
	 */
	public InternalResourceView(String url, boolean alwaysInclude) {
		super(url);
		this.alwaysInclude = alwaysInclude;
	}


	/**
	 * Specify whether to always include the view rather than forward to it.
	 * <p>Default is "false". Switch this flag on to enforce the use of a
	 * Servlet include, even if a forward would be possible.
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see #useInclude(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	/**
	 * Set whether to make all Spring beans in the application context accessible
	 * as request attributes, through lazy checking once an attribute gets accessed.
	 * <p>This will make all such beans accessible in plain <code>${...}</code>
	 * expressions in a JSP 2.0 page, as well as in JSTL's <code>c:out</code>
	 * value expressions.
	 * <p>Default is "false". Switch this flag on to transparently expose all
	 * Spring beans in the request attribute namespace.
	 * <p><b>NOTE:</b> Context beans will override any custom request or session
	 * attributes of the same name that have been manually added. However, model
	 * attributes (as explicitly exposed to this view) of the same name will
	 * always override context beans.
	 * @see #getRequestToExpose
	 */
	public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
	}

	/**
	 * Set whether to explicitly prevent dispatching back to the
	 * current handler path.
	 * <p>Default is "false". Switch this to "true" for convention-based
	 * views where a dispatch back to the current handler path is a
	 * definitive error.
	 */
	public void setPreventDispatchLoop(boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}


	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 */
	protected void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// Determine which request handle to expose to the RequestDispatcher.
		HttpServletRequest requestToExpose = getRequestToExpose(request);

		// Expose the model object as request attributes.
		exposeModelAsRequestAttributes(model, requestToExpose);

		// Expose helpers as request attributes, if any.
		exposeHelpers(requestToExpose);

		// Determine the path for the request dispatcher.
		String dispatcherPath = prepareForRendering(requestToExpose, response);

		// Obtain a RequestDispatcher for the target resource (typically a JSP).
		RequestDispatcher rd = requestToExpose.getRequestDispatcher(dispatcherPath);
		if (rd == null) {
			throw new ServletException(
					"Could not get RequestDispatcher for [" + getUrl() + "]: check that this file exists within your WAR");
		}

		// If already included or response already committed, perform include, else forward.
		if (useInclude(requestToExpose, response)) {
			response.setContentType(getContentType());
			if (logger.isDebugEnabled()) {
				logger.debug("Including resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
			rd.include(requestToExpose, response);
		}

		else {
			// Note: The forwarded resource is supposed to determine the content type itself.
			exposeForwardRequestAttributes(requestToExpose);
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarding to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
			}
			rd.forward(requestToExpose, response);
		}
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's contexts etc.
	 * <p>Called by {@link #renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)}.
	 * The default implementation is empty. This method can be overridden to add
	 * custom helpers as request attributes.
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding attributes
	 * @see #renderMergedOutputModel
	 * @see JstlView#exposeHelpers
	 * @see org.springframework.web.servlet.view.tiles.TilesJstlView#exposeHelpers
	 */
	protected void exposeHelpers(HttpServletRequest request) throws Exception {
	}

	/**
	 * Prepare for rendering, and determine the request dispatcher path
	 * to forward to (or to include).
	 * <p>This implementation simply returns the configured URL.
	 * Subclasses can override this to determine a resource to render,
	 * typically interpreting the URL in a different manner.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the request dispatcher path to use
	 * @throws Exception if preparations failed
	 * @see #getUrl()
	 * @see org.springframework.web.servlet.view.tiles.TilesView#prepareForRendering
	 */
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String path = getUrl();
		if (this.preventDispatchLoop) {
			String previousPath = (String) request.getAttribute(DISPATCHED_PATH_ATTRIBUTE);
			String uri = request.getRequestURI();
			if (path.equals(previousPath)) {
				throw new ServletException("Circular view path [" + path + "]: already dispatched to this " +
						"view path within the same request [" + uri + "]. Check your ViewResolver setup! " +
						"(Hint: This may be the result of an unspecified view, due to default view name generation.)");
			}
			if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {
				throw new ServletException("Circular view path [" + path + "]: would dispatch back " +
						"to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " +
						"(Hint: This may be the result of an unspecified view, due to default view name generation.)");
			}
			request.setAttribute(DISPATCHED_PATH_ATTRIBUTE, path);
		}
		return path;
	}

	/**
	 * Get the request handle to expose to the RequestDispatcher, i.e. to the view.
	 * <p>The default implementation wraps the original request for exposure of
	 * Spring beans as request attributes (if demanded).
	 * @param originalRequest the original servlet request as provided by the engine
	 * @return the wrapped request, or the original request if no wrapping is necessary
	 * @see #setExposeContextBeansAsAttributes
	 * @see org.springframework.web.context.support.ContextExposingHttpServletRequest
	 */
	protected HttpServletRequest getRequestToExpose(HttpServletRequest originalRequest) {
		if (this.exposeContextBeansAsAttributes &&
				!(originalRequest instanceof ContextExposingHttpServletRequest)) {
			return new ContextExposingHttpServletRequest(originalRequest, getWebApplicationContext());
		}
		return originalRequest;
	}

	/**
	 * Determine whether to use RequestDispatcher's <code>include</code> or
	 * <code>forward</code> method.
	 * <p>Performs a check whether an include URI attribute is found in the request,
	 * indicating an include request, and whether the response has already been committed.
	 * In both cases, an include will be performed, as a forward is not possible anymore.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return <code>true</code> for include, <code>false</code> for forward
	 * @see javax.servlet.RequestDispatcher#forward
	 * @see javax.servlet.RequestDispatcher#include
	 * @see javax.servlet.ServletResponse#isCommitted
	 * @see org.springframework.web.util.WebUtils#isIncludeRequest
	 */
	protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
		return (this.alwaysInclude || WebUtils.isIncludeRequest(request) || response.isCommitted());
	}

	/**
	 * Expose the current request URI and paths as {@link HttpServletRequest}
	 * attributes under the keys defined in the Servlet 2.4 specification,
	 * for Servlet 2.3 containers.
	 * <p>Does not override values if already present, to not conflict
	 * with Servlet 2.4+ containers.
	 * @see org.springframework.web.util.WebUtils#exposeForwardRequestAttributes
	 */
	protected void exposeForwardRequestAttributes(HttpServletRequest request) {
		WebUtils.exposeForwardRequestAttributes(request);
	}

}
