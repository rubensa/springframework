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

package org.springframework.validation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Exception to be thrown when abinding errors are considered fatal.
 * Implements the BindingResult interface (and hence its super-interface Errors)
 * to allow for direct analysis of binding errors.
 *
 * <p>As of Spring 2.0, this is a special-purpose class. Normally, application
 * code will work with the BindingResult interface, or a DataBinder that
 * in turn exposes a BindingResult via <code>getBindingResult()</code>.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see BindingResult
 * @see DataBinder#getBindingResult()
 * @see DataBinder#close()
 */
public class BindException extends Exception implements BindingResult {

	/**
	 * Prefix for the name of the BindException instance in a model,
	 * followed by the object name.
	 * @deprecated in favor of <code>BindingResult.MODEL_KEY_PREFIX</code>
	 * @see BindingResult#MODEL_KEY_PREFIX
	 */
	public static final String ERROR_KEY_PREFIX = BindException.class.getName() + ".";


	private final BindingResult bindingResult;


	/**
	 * Create a new BindException instance for a BindingResult.
	 * @param bindingResult the BindingResult instance to wrap
	 */
	public BindException(BindingResult bindingResult) {
		Assert.notNull(bindingResult, "BindingResult must not be null");
		this.bindingResult = bindingResult;
	}

	/**
	 * Create a new BindException instance for a target bean.
	 * @param target target bean to bind onto
	 * @param objectName the name of the target object
	 * @see BeanBindingResult
	 */
	public BindException(Object target, String objectName) {
		Assert.notNull(target, "Target object must not be null");
		this.bindingResult = new BeanBindingResult(target, objectName);
	}

	/**
	 * Return the BindingResult that this BindException wraps.
	 * Will typically be a BeanBindingResult.
	 * @see BeanBindingResult
	 */
	public final BindingResult getBindingResult() {
		return bindingResult;
	}


	/**
	 * Return the wrapped target object.
	 */
	public Object getTarget() {
		return this.bindingResult.getTarget();
	}

	public String getObjectName() {
		return this.bindingResult.getObjectName();
	}


	public void setNestedPath(String nestedPath) {
		this.bindingResult.setNestedPath(nestedPath);
	}

	public String getNestedPath() {
		return this.bindingResult.getNestedPath();
	}

	public void pushNestedPath(String subPath) {
		this.bindingResult.pushNestedPath(subPath);
	}

	public void popNestedPath() throws IllegalStateException {
		this.bindingResult.popNestedPath();
	}


	public void reject(String errorCode) {
		this.bindingResult.reject(errorCode);
	}

	public void reject(String errorCode, String defaultMessage) {
		this.bindingResult.reject(errorCode, defaultMessage);
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		this.bindingResult.reject(errorCode, errorArgs, defaultMessage);
	}

	public void rejectValue(String field, String errorCode) {
		this.bindingResult.rejectValue(field, errorCode);
	}

	public void rejectValue(String field, String errorCode, String defaultMessage) {
		this.bindingResult.rejectValue(field, errorCode, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		this.bindingResult.rejectValue(field, errorCode, errorArgs, defaultMessage);
	}

	public void addAllErrors(Errors errors) {
		this.bindingResult.addAllErrors(errors);
	}


	public boolean hasErrors() {
		return this.bindingResult.hasErrors();
	}

	public int getErrorCount() {
		return this.bindingResult.getErrorCount();
	}

	public List getAllErrors() {
		return this.bindingResult.getAllErrors();
	}

	public boolean hasGlobalErrors() {
		return this.bindingResult.hasGlobalErrors();
	}

	public int getGlobalErrorCount() {
		return this.bindingResult.getGlobalErrorCount();
	}

	public List getGlobalErrors() {
		return this.bindingResult.getGlobalErrors();
	}

	public ObjectError getGlobalError() {
		return this.bindingResult.getGlobalError();
	}

	public boolean hasFieldErrors() {
		return this.bindingResult.hasFieldErrors();
	}

	public int getFieldErrorCount() {
		return this.bindingResult.getFieldErrorCount();
	}

	public List getFieldErrors() {
		return this.bindingResult.getFieldErrors();
	}

	public FieldError getFieldError() {
		return this.bindingResult.getFieldError();
	}

	public boolean hasFieldErrors(String field) {
		return this.bindingResult.hasFieldErrors(field);
	}

	public int getFieldErrorCount(String field) {
		return this.bindingResult.getFieldErrorCount(field);
	}

	public List getFieldErrors(String field) {
		return this.bindingResult.getFieldErrors(field);
	}

	public FieldError getFieldError(String field) {
		return this.bindingResult.getFieldError(field);
	}

	public Object getFieldValue(String field) {
		return this.bindingResult.getFieldValue(field);
	}


	public void recordSuppressedField(String fieldName) {
		this.bindingResult.recordSuppressedField(fieldName);
	}

	public String[] getSuppressedFields() {
		return this.bindingResult.getSuppressedFields();
	}

	public Map getModel() {
		return this.bindingResult.getModel();
	}


	/**
	 * Returns diagnostic information about the errors held in this object.
	 */
	public String getMessage() {
		StringBuffer sb = new StringBuffer("BindException: ");
		sb.append(getErrorCount()).append(" errors");
		Iterator it = this.bindingResult.getAllErrors().iterator();
		while (it.hasNext()) {
			sb.append("; ").append(it.next());
		}
		return sb.toString();
	}

}
