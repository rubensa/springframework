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

package org.springframework.web.servlet.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.validation.Errors;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * <p>Nested path tag, to support and assist with nested beans or bean properties.
 * Exports a "nestedPath" variable of type String.
 *
 * <p>The BindTag will auto-detect the nested path and automatically prepend it
 * to its own path to form a complete path to the bean or bean property.
 *
 * <p>This tag will also prepend any existing nested path that is currently set.
 * Thus, you can nested multiple nested path tags.
 *
 * @author Seth Ladd
 * @author Juergen Hoeller
 * @since 28.07.2004
 */
public class NestedPathTag extends TagSupport {

	/**
	 * Name of the exposed variable within the scope of this tag: "nestedPath".
	 */
	public static final String NESTED_PATH_VARIABLE_NAME = "nestedPath";


	private String path;

	/** To cache any previous nested path, so that it may be reset */
	private String previousNestedPath = "";


	/**
	 * Set the path that this tag should apply.
	 * <p>E.g. "customer" to allow bind paths like "address.street"
	 * rather than "customer.address.street".
	 * @see BindTag#setPath
	 */
	public void setPath(String path) {
		if (path == null) {
			path = "";
		}
		if (path.length() > 0 && !path.endsWith(Errors.NESTED_PATH_SEPARATOR)) {
			path += Errors.NESTED_PATH_SEPARATOR;
		}
		this.path = path;
	}

	/**
	 * Return the path that this tag applies to.
	 */
	public String getPath() {
		return path;
	}


	public int doStartTag() throws JspException {
		String resolvedPath = ExpressionEvaluationUtils.evaluateString("path", getPath(), pageContext);
		String nestedPath = (String) pageContext.getAttribute(NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
		if (nestedPath != null) {
			this.previousNestedPath = nestedPath;
			nestedPath = nestedPath + resolvedPath;
		}
		else {
			nestedPath = resolvedPath;
		}
		this.pageContext.setAttribute(NESTED_PATH_VARIABLE_NAME, nestedPath, PageContext.REQUEST_SCOPE);
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Reset any previous nestedPath value.
	 */
	public int doEndTag() {
		if (this.previousNestedPath != null) {
			pageContext.setAttribute(NESTED_PATH_VARIABLE_NAME, this.previousNestedPath);
		}
		else {
			pageContext.removeAttribute(NESTED_PATH_VARIABLE_NAME);
		}
		return EVAL_PAGE;
	}

}
