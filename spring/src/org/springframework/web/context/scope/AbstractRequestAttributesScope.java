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

package org.springframework.web.context.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Abstract {@link Scope} implementation that reads from a particular scope in
 * the current thread bound {@link RequestAttributes} object. Subclasses simply
 * need to implement {@link #getScope()} to instruct this class which {@link RequestAttributes}
 * scope to read attributes from.
 * <p/>
 * Subclass may wish to override {@link #get} and {@link #remove} to add synchronization around the
 * call back into this super class.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractRequestAttributesScope implements Scope {

	public Object get(String name, ObjectFactory objectFactory) {
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		Object scopedObject = attributes.getAttribute(name, getScope());
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			attributes.setAttribute(name, scopedObject, getScope());
		}
		return scopedObject;
	}

	public Object remove(String name) {
		RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
		Object scopedObject = attributes.getAttribute(name, getScope());
		if (scopedObject != null) {
			attributes.removeAttribute(name, getScope());
			return scopedObject;
		}
		else {
			return null;
		}
	}

	protected abstract int getScope();
}
