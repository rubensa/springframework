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

package org.springframework.web.servlet.view;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.support.JstlUtils;

/**
 * Specialization of InternalResourceView for JSTL pages,
 * i.e. JSP pages that use the JSP Standard Tag Library.
 *
 * <p>Exposes JSTL-specific request attributes specifying locale
 * and resource bundle for JSTL's formatting and message tags,
 * using Spring's locale and message source.
 *
 * <p>This is a separate class mainly to avoid JSTL dependencies
 * in InternalResourceView itself.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 */
public class JstlView extends InternalResourceView {

	protected void exposeModelAsRequestAttributes(Map model, HttpServletRequest request) throws ServletException {
		super.exposeModelAsRequestAttributes(model, request);
		JstlUtils.exposeLocalizationContext(request, getApplicationContext());
	}
}
