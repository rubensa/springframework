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
package org.springframework.web.flow;

import java.util.Map;

/**
 * Extension of attributes accessor allowing for mutable operations; if you dont
 * need mutability, pass AttributesAccessor around instead--it's safer.
 * <p>
 * The attributes stored in the flow model are accessed using this interface.
* <p>
 * Implementers of this interface (e.g. the flow model) can have an 
 * <i>active transaction</i> and to start and stop transactions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface MutableAttributesAccessor extends AttributesAccessor {

	/**
	 * Set the attribute with the provided name to the value provided.
	 * @param attributeName The attribute name
	 * @param attributeValue The attribute value
	 */
	public void setAttribute(String attributeName, Object attributeValue);

	/**
	 * Perform a bulk-set operation on a number of attributes.
	 * @param attributes The map of attributes (name=value pairs).
	 */
	public void setAttributes(Map attributes);

	/**
	 * Remove the specified attribute, if it exists.
	 * @param attributeName The attribute name.
	 */
	public void removeAttribute(String attributeName);

	/**
	 * Start a new transaction on this model.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction on this model.
	 */
	public void endTransaction();
}