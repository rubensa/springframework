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
package org.springframework.web.flow.action;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.ScopeType;

/**
 * Convenience wrapper that encapsulates logic on how to retrieve and expose
 * form objects and associated errors to and from a flow execution context.
 * @author Keith Donald
 */
public class FormObjectAccessor {

	/**
	 * The form object instance is aliased under this attribute name in the flow
	 * context by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ATTRIBUTE_NAME = "formObject";

	/**
	 * The form object errors instance is aliased under this attribute name in
	 * the flow context by the default form setup and bind and validate actions.
	 */
	public static final String FORM_OBJECT_ERRORS_ATTRIBUTE_NAME = "formObjectErrors";

	private FlowExecutionContext context;

	/**
	 * Creates a form object accessor that wraps the given context.
	 * @param context the flow execution context.
	 */
	public FormObjectAccessor(FlowExecutionContext context) {
		this.context = context;
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ATTRIBUTE_NAME}.
	 * @param context the flow context
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         context
	 */
	public Object getFormObject() throws IllegalStateException {
		return getFormObject(FORM_OBJECT_ATTRIBUTE_NAME);
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ATTRIBUTE_NAME}.
	 * @param context the flow context
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         context or is not of the required type
	 */
	public Object getFormObject(Class formObjectClass) throws IllegalStateException {
		return getFormObject(FORM_OBJECT_ATTRIBUTE_NAME, formObjectClass);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the name {@link #FORM_OBJECT_ERRORS_ATTRIBUTE_NAME}.
	 * @param context the flow context
	 * @return the form object Errors tracker
	 * @throws IllegalStateException if the Errors instance is not found in the
	 *         context
	 */
	public Errors getFormErrors() throws IllegalStateException {
		return (Errors)this.context.getRequestScope()
				.getRequiredAttribute(FORM_OBJECT_ERRORS_ATTRIBUTE_NAME, Errors.class);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param context the flow context
	 * @param formObjectName the name of the form object in the context
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         context
	 */
	public Object getFormObject(String formObjectName) throws IllegalStateException {
		return this.context.getRequestScope().getRequiredAttribute(formObjectName);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param context the flow context
	 * @param formObjectName the name of the form in the context
	 * @param formObjectClass the class of the form object, which will be
	 *        verified
	 * @return the form object
	 * @throws IllegalStateException if the form object is not found in the
	 *         context or is not of the required type
	 */
	public Object getFormObject(String formObjectName, Class formObjectClass) throws IllegalStateException {
		return this.context.getRequestScope().getRequiredAttribute(formObjectName, formObjectClass);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the specified name.
	 * @param context The flow context
	 * @param formObjectErrorsName The name of the Errors object, which will be
	 *        prefixed with {@link BindException#ERROR_KEY_PREFIX}
	 * @return The form object errors instance
	 * @throws IllegalStateException if the Errors instance is not found in the
	 *         context
	 */
	public Errors getFormErrors(String formObjectErrorsName) throws IllegalStateException {
		return (Errors)this.context.getRequestScope().getRequiredAttribute(
				BindException.ERROR_KEY_PREFIX + formObjectErrorsName, Errors.class);
	}

	/**
	 * Expose a <i>new </i> errors instance in the flow context for the form
	 * object using name {@link #FORM_OBJECT_ATTRIBUTE_NAME}.
	 * 
	 * @param context The flow context
	 * @param formObject The form object to expose an errors instance for
	 */
	public void exposeErrors(Object formObject, ScopeType scope) {
		exposeErrors(formObject, FORM_OBJECT_ATTRIBUTE_NAME, scope);
	}

	/**
	 * Expose a <i>new </i> errors instance in the flow context for the form
	 * object with the specified form object name.
	 * @param context The flow context
	 * @param formObject The form object
	 * @param formObjectName The name of the form object
	 */
	public void exposeErrors(Object formObject, String formObjectName, ScopeType scope) {
		exposeBindExceptionErrors(new BindException(formObject, formObjectName), scope);
	}

	/**
	 * Internal helper to expose form object error information.
	 * @param errors the errors instance
	 */
	public void exposeBindExceptionErrors(BindException errors, ScopeType scope) {
		if (scope == ScopeType.FLOW) {
			this.context.getFlowScope().setAttribute(FORM_OBJECT_ATTRIBUTE_NAME, errors.getTarget());
			this.context.getFlowScope().setAttribute(errors.getObjectName(), errors.getTarget());
		}
		else {
			this.context.getRequestScope().setAttribute(FORM_OBJECT_ATTRIBUTE_NAME, errors.getTarget());
		}
		this.context.getRequestScope().setAttribute(FORM_OBJECT_ERRORS_ATTRIBUTE_NAME, errors);
		this.context.getRequestScope().setAttributes(errors.getModel());
	}
}