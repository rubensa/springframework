package org.springframework.web.bind;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.util.HtmlUtils;

/**
 * Errors wrapper that adds automatic HTML escaping to the wrapped instance,
 * for convenient usage in HTML views. Can be retrieved easily via
 * RequestContext's getErrors method.
 *
 * <p>Note that BindTag does not use this class to avoid unnecessary creation
 * of ObjectError instances. It just escapes the messages and values that get
 * copied into the respective BindStatus instance.
 *
 * @author Juergen Hoeller
 * @since 01.03.2003
 * @see org.springframework.web.servlet.support.RequestContext#getErrors
 * @see org.springframework.web.servlet.tags.BindTag
 */
public class EscapedErrors implements Errors {

	private Errors source = null;

	/**
	 * Create a new EscapedErrors instance for the given source instance.
	 */
	public EscapedErrors(Errors source) {
		if (source == null) {
			throw new IllegalArgumentException("Cannot wrap a null instance");
		}
		this.source = source;
	}

	public Errors getSource() {
		return this.source;
	}

	public String getObjectName() {
		return this.source.getObjectName();
	}

	public void reject(String errorCode, String defaultMessage) {
		this.source.reject(errorCode, defaultMessage);
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		this.source.reject(errorCode, errorArgs, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, String defaultMessage) {
		this.source.rejectValue(field, errorCode, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		this.source.rejectValue(field, errorCode, errorArgs, defaultMessage);
	}

	public boolean hasErrors() {
		return this.source.hasErrors();
	}

	public int getErrorCount() {
		return this.source.getErrorCount();
	}

	public List getAllErrors() {
		return escapeObjectErrors(this.source.getAllErrors());
	}

	public boolean hasGlobalErrors() {
		return this.source.hasGlobalErrors();
	}

	public int getGlobalErrorCount() {
		return this.source.getGlobalErrorCount();
	}

	public List getGlobalErrors() {
		return escapeObjectErrors(this.source.getGlobalErrors());
	}

	public ObjectError getGlobalError() {
		return escapeObjectError(this.source.getGlobalError());
	}

	public boolean hasFieldErrors(String field) {
		return this.source.hasFieldErrors(field);
	}

	public int getFieldErrorCount(String field) {
		return this.source.getFieldErrorCount(field);
	}

	public List getFieldErrors(String field) {
		return escapeObjectErrors(this.source.getFieldErrors(field));
	}

	public FieldError getFieldError(String field) {
		return (FieldError) escapeObjectError(this.source.getFieldError(field));
	}

	public Object getFieldValue(String field) {
		Object value = this.source.getFieldValue(field);
		return (value instanceof String ? HtmlUtils.htmlEscape((String) value) : value);
	}

	public void setNestedPath(String nestedPath) {
		this.source.setNestedPath(nestedPath);
	}

	public String getNestedPath() {
		return this.source.getNestedPath();
	}

	private ObjectError escapeObjectError(ObjectError source) {
		if (source == null) {
			return null;
		}
		if (source instanceof FieldError) {
			FieldError fieldError = (FieldError) source;
			Object value = fieldError.getRejectedValue();
			if (value instanceof String) {
				value = HtmlUtils.htmlEscape((String) fieldError.getRejectedValue());
			}
			return new FieldError(fieldError.getObjectName(), fieldError.getField(), value, fieldError.isBindingFailure(),
														fieldError.getCode(), fieldError.getArguments(), HtmlUtils.htmlEscape(fieldError.getDefaultMessage()));
		}
		return new ObjectError(source.getObjectName(), source.getCode(), source.getArguments(),
													 HtmlUtils.htmlEscape(source.getDefaultMessage()));
	}

	private List escapeObjectErrors(List source) {
		List escaped = new ArrayList();
		for (Iterator it = source.iterator(); it.hasNext();) {
			ObjectError objectError = (ObjectError)it.next();
			escaped.add(escapeObjectError(objectError));
		}
		return escaped;
	}

}
