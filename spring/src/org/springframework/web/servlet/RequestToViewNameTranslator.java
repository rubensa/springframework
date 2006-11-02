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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Strategy interface for translating an incoming
 * {@link javax.servlet.http.HttpServletRequest} into a
 * logical view name when no view name is explicitly supplied.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public interface RequestToViewNameTranslator {

	/**
	 * Translate the given {@link HttpServletRequest} into a view name.
	 * @param request the incoming {@link HttpServletRequest} providing
	 * the context from which a view name is to be resolved
	 * @return the view name (never <code>null</code>)
	 * @throws Exception if view name translation fails
	 */
	String getViewName(HttpServletRequest request) throws Exception;

}
