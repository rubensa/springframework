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

package org.springframework.web.portlet.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.RenderRequest;
import javax.servlet.ServletException;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.ui.context.Theme;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.portlet.context.PortletApplicationContext;
import org.springframework.web.portlet.support.RequestContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Context holder for request-specific state, like current portlet application
 * context, current locale, current theme, and potential binding errors.
 * Provides easy access to localized messages and Errors instances.
 *
 *
 * <p>Suitable for exposition to views, and usage within <jsp:useBean>,
 * JSP scriptlets, JSTL EL, Velocity templates, etc. Necessary for views
 * that do not have access to the servlet request, like Velocity templates.
 *
 * <p>Can be instantiated manually, or automatically exposed to views as
 * model attribute via AbstractView's requestContextAttribute property.
 *
 * @author Juergen Hoeller
 * @since 03.03.2003
 * @see org.springframework.web.portlet.view.AbstractView#setRequestContextAttribute
 */
public class RequestContext {

	private RenderRequest request;

	private Map model;

	private PortletApplicationContext portletApplicationContext;

	private Locale locale;

	private Theme theme;

	private boolean defaultHtmlEscape;

	private Map errorsMap;


	/**
	 * Create a new RequestContext for the given request,
	 * using the request attributes for Errors retrieval.
	 * <p>This only works with InternalResourceViews, as Errors instances
	 * are part of the model and not normally exposed as request attributes.
	 * It will typically be used within JSPs or custom tags.
	 * @param request current portlet request
	 */
	public RequestContext(RenderRequest request) throws ServletException {
		this(request, null);
	}

	/**
	 * Create a new RequestContext for the given request,
	 * using the given model attributes for Errors retrieval.
	 * <p>This works with all View implementations.
	 * It will typically be used by View implementations.
	 * @param request current portlet request
	 * @param model the model attributes for the current view
	 */
	public RequestContext(RenderRequest request, Map model)
		throws ServletException {
		this.request = request;
		this.portletApplicationContext = RequestContextUtils.getPortletApplicationContext(request);
		this.locale = request.getLocale();
		this.theme = RequestContextUtils.getTheme(request);
		this.model = model;
	}

	/**
	 * Return the context path of the current request,
	 * i.e. the path that indicates the current portlet application.
	 * @see javax.portlet.RenderRequest#getContextPath
	 */
	public String getContextPath() {
		return request.getContextPath();
	}

	/**
	 * Return the current PortletApplicationContext.
	 */
	public PortletApplicationContext getPortletApplicationContext() {
		return portletApplicationContext;
	}

	/**
	 * Return the current locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Return the current theme.
	 */
	public Theme getTheme() {
		return theme;
	}

	/**
	 * (De)activate default HTML escaping for messages and errors.
	 */
	public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
		this.defaultHtmlEscape = defaultHtmlEscape;
	}

	/**
	 * Is default HTML escaping active?
	 */
	public boolean isDefaultHtmlEscape() {
		return defaultHtmlEscape;
	}


	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getMessage(String code, String defaultMessage) {
		return getMessage(code, null, defaultMessage, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getMessage(String code, Object[] args, String defaultMessage) {
		return getMessage(code, args, defaultMessage, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 */
	public String getMessage(String code, Object[] args, String defaultMessage, boolean htmlEscape) {
		String msg = this.portletApplicationContext.getMessage(code, args, defaultMessage, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code) throws NoSuchMessageException {
		return getMessage(code, null, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code, Object[] args) throws NoSuchMessageException {
		return getMessage(code, args, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code, Object[] args, boolean htmlEscape) throws NoSuchMessageException {
		String msg = this.portletApplicationContext.getMessage(code, args, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance),
	 * using the defaultHtmlEscape setting.
	 * @param resolvable the MessageSourceResolvable
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return getMessage(resolvable, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance).
	 * @param resolvable the MessageSourceResolvable
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(MessageSourceResolvable resolvable, boolean htmlEscape) throws NoSuchMessageException {
		String msg = this.portletApplicationContext.getMessage(resolvable, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}


	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getThemeMessage(String code, String defaultMessage) {
		return this.theme.getMessageSource().getMessage(code, null, defaultMessage, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getThemeMessage(String code, String[] args, String defaultMessage) {
		return this.theme.getMessageSource().getMessage(code, args, defaultMessage, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(String code) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(code, null, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(String code, String[] args) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(code, args, this.locale);
	}

	/**
	 * Retrieve the given MessageSourceResolvable in the current theme.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param resolvable the MessageSourceResolvable
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(resolvable, this.locale);
	}


	/**
	 * Retrieve the Errors instance for the given bind object,
	 * using the defaultHtmlEscape setting.
	 * @param name name of the bind object
	 * @return the Errors instance, or null if not found
	 */
	public Errors getErrors(String name) {
		return getErrors(name, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the Errors instance for the given bind object.
	 * @param name name of the bind object
	 * @param htmlEscape create an Errors instance with automatic HTML escaping?
	 * @return the Errors instance, or null if not found
	 */
	public Errors getErrors(String name, boolean htmlEscape) {
		if (this.errorsMap == null) {
			this.errorsMap = new HashMap();
		}
		Errors errors = (Errors) this.errorsMap.get(name);
		boolean put = false;
		if (errors == null) {
			errors = retrieveErrors(name);
			if (errors == null) {
				return null;
			}
			put = true;
		}
		if (htmlEscape && !(errors instanceof EscapedErrors)) {
			errors = new EscapedErrors(errors);
			put = true;
		}
		else if (!htmlEscape && errors instanceof EscapedErrors) {
			errors = ((EscapedErrors) errors).getSource();
			put = true;
		}
		if (put) {
			this.errorsMap.put(name, errors);
		}
		return errors;
	}

	/**
	 * Retrieve the Errors instance for the given bind object,
	 * either from the model or from the request attributes.
	 */
	private Errors retrieveErrors(String name) {
		String key = BindException.ERROR_KEY_PREFIX + name;
		if (this.model != null) {
			return (Errors) this.model.get(key);
		}
		else {
			return (Errors) this.request.getAttribute(key);
		}
	}

}
