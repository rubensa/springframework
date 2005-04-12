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

package org.springframework.web.bind;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Special binder to perform data binding from servlet request parameters
 * to JavaBeans, including support for multipart files.
 *
 * <p>See the DataBinder superclass for customization options, which include
 * specifying allowed/required fields, and registering custom property editors.
 *
 * <p>Used by Spring web MVC's BaseCommandController and MultiActionController.
 * Note that BaseCommandController and its subclasses allow for easy customization
 * of the binder instances that they use through overriding <code>initBinder</code>.
 *
 * <p>Can also be used for manual data binding in custom web controllers.
 * Simply instantiate a ServletRequestDataBinder for each binding process,
 * and invoke <code>bind</code> with the current ServletRequest as argument.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #bind(javax.servlet.ServletRequest)
 * @see #setAllowedFields
 * @see #setRequiredFields
 * @see #registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController
 */
public class ServletRequestDataBinder extends DataBinder {

	/**
	 * Default prefix that field marker parameters start with, followed by the field
	 * name: e.g. "_subscribeToNewsletter" for a field "subscribeToNewsletter".
	 * <p>Such a marker parameter indicates that the field was visible respectively
	 * existed in the form that caused the submission. If no corresponding field
	 * value parameter was found, the field will be reset. This is particularly
	 * useful for HTML checkboxes and select options.
	 * @see #setFieldMarkerPrefix
	 */
	public static final String DEFAULT_FIELD_MARKER_PREFIX = "_";

	private String fieldMarkerPrefix = DEFAULT_FIELD_MARKER_PREFIX;

	private boolean bindEmptyMultipartFiles = true;


	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName objectName of the target object
	 */
	public ServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}

	/**
	 * Specify a prefix that can be used for parameters that mark potentially
	 * empty fields, having "prefix + field" as name. Such a marker parameter is
	 * checked by existence: You can send any value for it, for example "visible".
	 * This is particularly useful for HTML checkboxes and select options.
	 * <p>Default is "_", for "_FIELD" parameters (e.g. "_subscribeToNewsletter").
	 * Set this to null if you want to turn off the empty field check completely.
	 * <p>HTML checkboxes only send a value when they're checked, so it is not
	 * possible to detect that a formerly checked box has just been unchecked,
	 * at least not with standard HTML means.
	 * <p>One way to address this is to look for a checkbox parameter value if
	 * you know that the checkbox has been visible in the form, resetting the
	 * checkbox if no value found. In Spring web MVC, this typically happens
	 * in a custom <code>onBind</code> implementation.
	 * <p>This auto-reset mechanism addresses this deficiency, provided
	 * that a marker parameter is sent for each checkbox field, like
	 * "_subscribeToNewsletter" for a "subscribeToNewsletter" field.
	 * As the marker parameter is sent in any case, the data binder can
	 * detect an empty field and automatically reset its value.
	 * @see #DEFAULT_FIELD_MARKER_PREFIX
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind
	 */
	public void setFieldMarkerPrefix(String fieldMarkerPrefix) {
		this.fieldMarkerPrefix = fieldMarkerPrefix;
	}

	/**
	 * Return the prefix for parameters that mark potentially empty fields.
	 */
	public String getFieldMarkerPrefix() {
		return fieldMarkerPrefix;
	}

	/**
	 * Set whether to bind empty MultipartFile parameters. Default is true.
	 * <p>Turn this off if you want to keep an already bound MultipartFile
	 * when the user resubmits the form without choosing a different file.
	 * Else, the already bound MultipartFile will be replaced by an empty
	 * MultipartFile holder.
	 */
	public void setBindEmptyMultipartFiles(boolean bindEmptyMultipartFiles) {
		this.bindEmptyMultipartFiles = bindEmptyMultipartFiles;
	}

	/**
	 * Return whether to bind empty MultipartFile parameters.
	 */
	public boolean isBindEmptyMultipartFiles() {
		return bindEmptyMultipartFiles;
	}


	/**
	 * Bind the parameters of the given request to this binder's target,
	 * also binding multipart files in case of a multipart request.
	 * <p>This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * <p>Multipart files are bound via their parameter name, just like normal
	 * HTTP parameters: i.e. "uploadedFile" to an "uploadedFile" bean property,
	 * invoking a "setUploadedFile" setter method.
	 * <p>The type of the target property for a multipart file can be MultipartFile,
	 * byte[], or String. The latter two receive the contents of the uploaded file;
	 * all metadata like original file name, content type, etc are lost in those cases.
	 * @param request request with parameters to bind (can be multipart)
	 * @see org.springframework.web.multipart.MultipartHttpServletRequest
	 * @see org.springframework.web.multipart.MultipartFile
	 * @see #checkFieldMarkers
	 * @see #checkMultipartFiles
	 * @see #bind(org.springframework.beans.PropertyValues)
	 */
	public void bind(ServletRequest request) {
		MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
		checkFieldMarkers(mpvs, request);
		checkMultipartFiles(mpvs, request);
		bind(mpvs);
	}

	/**
	 * Check the given property values for field markers,
	 * i.e. for fields that start with the field marker prefix.
	 * <p>The existence of a field marker indicates that the specified
	 * field existed in the form. If the property values do not contain
	 * a corresponding field value, the field will be considered as empty
	 * and will be reset appropriately.
	 * @param mpvs the property values to be bound (can be modified)
	 * @param request current request (can be multipart)
	 * @see #getFieldMarkerPrefix
	 * @see #getEmptyValue(String, Class)
	 */
	protected void checkFieldMarkers(MutablePropertyValues mpvs, ServletRequest request) {
		if (getFieldMarkerPrefix() != null) {
			String fieldMarkerPrefix = getFieldMarkerPrefix();
			PropertyValue[] pvArray = mpvs.getPropertyValues();
			for (int i = 0; i < pvArray.length; i++) {
				PropertyValue pv = pvArray[i];
				if (pv.getName().startsWith(fieldMarkerPrefix)) {
					String field = pv.getName().substring(fieldMarkerPrefix.length());
					if (getBeanWrapper().isWritableProperty(field) && !mpvs.contains(field)) {
						Class fieldType = getBeanWrapper().getPropertyType(field);
						mpvs.addPropertyValue(field, getEmptyValue(field, fieldType));
					}
				}
			}
		}
	}

	/**
	 * Determine an empty value for the specified field.
	 * <p>Default implementation returns <code>Boolean.FALSE</code>
	 * for boolean fields and an empty array of array types.
	 * Else, <code>null</code> is used as default.
	 * @param field the name of the field
	 * @param fieldType the type of the field
	 * @return the empty value (for most fields: null)
	 */
	protected Object getEmptyValue(String field, Class fieldType) {
		if (fieldType != null && boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
			// Special handling of boolean property.
			return Boolean.FALSE;
		}
		else if (fieldType != null && fieldType.isArray()) {
			// Special handling of array property.
			return Array.newInstance(fieldType.getComponentType(), 0);
		}
		else {
			// Default value: try null.
			return null;
		}
	}

	/**
	 * Check the multipart files contained in the given request, if any
	 * (in case of a multipart request).
	 * <p>Multipart files will only be added to the property values if they
	 * are not empty or if we're configured to bind empty multipart files too.
	 * @param mpvs the property values to be bound (can be modified)
	 * @param request current request (can be multipart)
	 * @see org.springframework.web.multipart.MultipartHttpServletRequest
	 * @see org.springframework.web.multipart.MultipartFile
	 * @see #isBindEmptyMultipartFiles
	 */
	protected void checkMultipartFiles(MutablePropertyValues mpvs, ServletRequest request) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map fileMap = multipartRequest.getFileMap();
			for (Iterator it = fileMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				MultipartFile value = (MultipartFile) entry.getValue();
				if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
					mpvs.addPropertyValue(key, value);
				}
			}
		}
	}

	/**
	 * Treats errors as fatal. Use this method only if
	 * it's an error if the input isn't valid.
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws ServletRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws ServletRequestBindingException {
		if (getErrors().hasErrors()) {
			throw new ServletRequestBindingException(
					"Errors binding onto object '" + getErrors().getObjectName() + "'", getErrors());
		}
	}

}
