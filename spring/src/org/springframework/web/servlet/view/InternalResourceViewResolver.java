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

/**
 * Convenience subclass of UrlBasedViewResolver that supports InternalResourceView
 * (i.e. Servlets and JSPs), and subclasses like JstlView and TilesView.
 *
 * <p>The view class for all views generated by this resolver can be specified
 * via setViewClass. See UrlBasedViewResolver's javadocs for details.
 *
 * <p>BTW, it's good practice to put JSP files that just serve as views under
 * WEB-INF, to hide them from direct access (e.g. via a manually entered URL).
 * Only controllers will be able to access them then.
 *
 * <p>Note: When chaining ViewResolvers, a InternalResourceViewResolver always
 * needs to be last, as it will attempt to resolve any view name, no matter
 * whether the underlying resource actually exists.
 *
 * @author Juergen Hoeller
 * @since 17.02.2003
 * @see #setViewClass
 * @see #setPrefix
 * @see #setSuffix
 * @see #setRequestContextAttribute
 * @see InternalResourceView
 * @see JstlView
 * @see org.springframework.web.servlet.view.tiles.TilesView
 */
public class InternalResourceViewResolver extends UrlBasedViewResolver {

	/**
	 * Sets default viewClass to InternalResourceView.
	 * @see #setViewClass
	 */
	public InternalResourceViewResolver() {
		setViewClass(InternalResourceView.class);
	}

	/**
	 * Requires InternalResourceView.
	 * @see InternalResourceView
	 */
	protected Class requiredViewClass() {
		return InternalResourceView.class;
	}

}
