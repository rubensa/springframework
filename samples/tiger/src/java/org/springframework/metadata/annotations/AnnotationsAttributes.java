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

package org.springframework.metadata.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.metadata.Attributes;

/**
 * Implementation of the Spring Attributes facade for standard JDK 1.5+) Annotations.
 *
 * @author Colin Sampaleanu
 */
public class AnnotationsAttributes implements Attributes {

	/*
	 * Commons Attributes caches attributes, so we don't need to cache here
	 * as well.
	 */

    /**
     * The getAnnotations method internally initializes only once, so we should not
     * need to cache these values
     */
	public Collection getAttributes(Class targetClass) {
		return Arrays.asList(targetClass.getAnnotations());
	}

	public Collection getAttributes(Class targetClass, Class filter) {
		ArrayList list = new ArrayList();
		Annotation clazz = targetClass.getAnnotation(filter);
		if (clazz != null)
			list.add(clazz);
		return list;
	}

	public Collection getAttributes(Method targetMethod) {
		return Arrays.asList(targetMethod.getAnnotations());
	}

	public Collection getAttributes(Method targetMethod, Class filter) {
		ArrayList list = new ArrayList();
		Annotation clazz = targetMethod.getAnnotation(filter);
		if (clazz != null)
			list.add(clazz);
		return list;
	}

	public Collection getAttributes(Field targetField) {
		return Arrays.asList(targetField.getAnnotations());
	}

	public Collection getAttributes(Field targetField, Class filter) {
		ArrayList list = new ArrayList();
		Annotation clazz = targetField.getAnnotation(filter);
		if (clazz != null)
			list.add(clazz);
		return list;
	}
}
