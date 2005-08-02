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

package org.springframework.web.portlet.bind;

import javax.portlet.PortletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;

/**
 * Special DataBinder to perform data binding from PortletRequest parameters
 * to JavaBeans.
 *
 * <p>See the DataBinder/WebDataBinder superclasses for customization options,
 * which include specifying allowed/required fields, and registering custom
 * property editors.
 *
 * <p>Used by Spring Portlet MVC's BaseCommandController.
 * Note that BaseCommandController and its subclasses allow for easy customization
 * of the binder instances that they use through overriding <code>initBinder</code>.
 *
 * <p>Can also be used for manual data binding in custom portlet controllers.
 * Simply instantiate a PortletRequestDataBinder for each binding process,
 * and invoke <code>bind</code> with the current PortletRequest as argument.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @see #bind(javax.portlet.PortletRequest)
 * @see #registerCustomEditor
 * @see #setAllowedFields
 * @see #setRequiredFields
 * @see #setFieldMarkerPrefix
 * @see org.springframework.web.portlet.mvc.BaseCommandController#initBinder
 */
public class PortletRequestDataBinder extends WebDataBinder {

	/**
	 * Create a new PortletRequestDataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName objectName of the target object
	 */
	public PortletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}

	/**
	 * Bind the parameters of the given request to this binder's target.
	 * <p>This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * @param request request with parameters to bind
	 * @see #bind(org.springframework.beans.PropertyValues)
	 */
	public void bind(PortletRequest request) {
		MutablePropertyValues mpvs = new PortletRequestParameterPropertyValues(request);
		doBind(mpvs);
	}

	/**
	 * Treats errors as fatal. Use this method only if
	 * it's an error if the input isn't valid.
	 * This might be appropriate if all input is from dropdowns, for example.
	 * @throws PortletRequestBindingException subclass of PortletException on any binding problem
	 */
	public void closeNoCatch() throws PortletRequestBindingException {
		if (getErrors().hasErrors()) {
			throw new PortletRequestBindingException(
					"Errors binding onto object '" + getErrors().getObjectName() + "'", getErrors());
		}
	}

}
