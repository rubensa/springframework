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

package org.springframework.validation;

/**
 * Interface for strategies that register custom property editors with a
 * data binder. This is particularly useful when you need to use the
 * same set of property editors in several different situations: write
 * a corresponding registrar and reuse that in each case.
 *
 * <p>Note: This interface is currently only intended for use with Web Flow
 * previews. It will be reworked into a more generic mechanism for Spring 1.3
 * and Web Flow 1.0, working on a passed-in PropertyEditorRegistry interface
 * rather than the DataBinder class, with the reworked version likely to
 * reside in the "org.springframework.beans" package.
 * 
 * @author Keith Donald
 * @since 1.2.2
 * @see DataBinder
 * @see java.beans.PropertyEditor
 */
public interface PropertyEditorRegistrar {
	
	/**
	 * Register custom PropertyEditors with the given DataBinder.
	 * @param binder the DataBinder to register the custom PropertyEditors with
	 */
	void registerCustomEditors(DataBinder binder);

}
