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
package org.springframework.webflow.action;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Convenience helper that encapsulates logic on how to retrieve and expose form
 * objects and associated errors to and from a flow execution context.
 * <p>
 * <b>Note</b>: The form object available under the well known attribute name
 * {@link #CURRENT_FORM_OBJECT_ATTRIBUTE} will be the last ("current") form
 * object set in the request context. The same is true for the associated errors
 * object. This implies that special care should be taken when accessing the
 * form object using this alias if there are multiple form objects available in
 * the flow execution context!
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.validation.Errors
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FormObjectAccessor {

	/**
	 * The form object instance is aliased under this attribute name in the flow
	 * context by the default form setup and bind and validate actions.
	 * <p>
	 * Note that if you would have multiple form objects in the request context,
	 * the last one that was used would be available using this alias!
	 */
	private static final String CURRENT_FORM_OBJECT_ATTRIBUTE = "currentFormObject";

	/**
	 * The errors prefix.
	 */
	private static final String ERRORS_PREFIX = BindException.ERROR_KEY_PREFIX;
	
	/**
	 * The current request context.
	 */
	private RequestContext context;

	/**
	 * Creates a form object accessor that wraps the given context.
	 * @param context the flow execution request context
	 */
	public FormObjectAccessor(RequestContext context) {
		this.context = context;
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #CURRENT_FORM_OBJECT_ATTRIBUTE}. Will try all scopes.
	 * @return the form object, or null if not found
	 */
	public Object getCurrentFormObject() {
		Object formObject = getCurrentFormObject(ScopeType.REQUEST);
		if (formObject != null) {
			return formObject;
		}
		formObject = getCurrentFormObject(ScopeType.FLOW);
		if (formObject != null) {
			return formObject;
		}
		return getCurrentFormObject(ScopeType.CONVERSATION);
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #CURRENT_FORM_OBJECT_ATTRIBUTE}.
	 * @param scope the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getCurrentFormObject(ScopeType scope) {
		return getFormObject(getCurrentFormObjectName(), scope);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form object in the context
	 * @param scopeType the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getFormObject(String formObjectName, ScopeType scopeType) {
		return scopeType.getScope(context).get(formObjectName);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form in the context
	 * @param formObjectClass the class of the form object, which will be
	 * verified
	 * @param scopeType the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getFormObject(String formObjectName, Class formObjectClass, ScopeType scopeType) {
		return scopeType.getScope(context).get(formObjectName, formObjectClass);
	}

	/**
	 * Expose given form object using given name in specified scope.
	 * @param formObject the form object
	 * @param formObjectName the name of the form object
	 * @param scopeType the scope in which to expose the form object
	 */
	public void setFormObject(Object formObject, String formObjectName, ScopeType scopeType) {
		scopeType.getScope(context).put(formObjectName, formObject);
		setCurrentFormObject(formObject, scopeType);
	}

	/**
	 * Expose given form object using the well known alias
	 * {@link #CURRENT_FORM_OBJECT_ATTRIBUTE} in the specified scope.
	 * @param formObject the form object
	 * @param scopeType the scope in which to expose the form object
	 */
	public void setCurrentFormObject(Object formObject, ScopeType scopeType) {
		scopeType.getScope(context).put(getCurrentFormObjectName(), formObject);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #CURRENT_FORM_OBJECT_ATTRIBUTE}. This
	 * method will search all scopes.
	 * @return the form object Errors tracker, or null if not found
	 */
	public Errors getCurrentFormErrors() {
		Errors errors = getCurrentFormErrors(ScopeType.REQUEST);
		if (errors != null) {
			return errors;
		}
		errors = getCurrentFormErrors(ScopeType.FLOW);
		if (errors != null) {
			return errors;
		}
		return getCurrentFormErrors(ScopeType.CONVERSATION);

	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #CURRENT_FORM_OBJECT_ATTRIBUTE}.
	 * @param scopeType the scope to obtain the errors from
	 * @return the form object Errors tracker, or null if not found
	 */
	public Errors getCurrentFormErrors(ScopeType scopeType) {
		return getFormErrors(getCurrentFormObjectName(), scopeType);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the specified form object name.
	 * @param formObjectName the name of the Errors object, which will be
	 * prefixed with {@link BindException#ERROR_KEY_PREFIX}
	 * @param scopeType the scope to obtain the errors from
	 * @return the form object errors instance, or null if not found
	 */
	public Errors getFormErrors(String formObjectName, ScopeType scopeType) {
		return (Errors)scopeType.getScope(context).get(ERRORS_PREFIX + formObjectName,
				Errors.class);
	}

	/**
	 * Expose given errors instance in the specified scope.
	 * @param errors the errors object
	 * @param scopeType the scope to expose the errors in
	 */
	public void setFormErrors(Errors errors, ScopeType scopeType) {
		scopeType.getScope(context).put(ERRORS_PREFIX + errors.getObjectName(), errors);
		setCurrentFormErrors(errors, scopeType);
	}

	/**
	 * Expose given errors instance using the well known alias
	 * {@link #CURRENT_FORM_OBJECT_ATTRIBUTE} in the specified scope.
	 * @param errors the errors instance
	 * @param scopeType the scope in which to expose the errors instance
	 */
	public void setCurrentFormErrors(Errors errors, ScopeType scopeType) {
		scopeType.getScope(context).put(getCurrentFormErrorsName(), errors);
	}

	/**
	 * Returns the current form object name.
	 * @return the current form object name
	 */
	public static String getCurrentFormObjectName() {
		return CURRENT_FORM_OBJECT_ATTRIBUTE;
	}
	
	/**
	 * Returns the current form object errors attribute name.
	 * @return the current form object errors attribute name
	 */
	public static String getCurrentFormErrorsName() {
		return ERRORS_PREFIX + getCurrentFormObjectName();
	}
}