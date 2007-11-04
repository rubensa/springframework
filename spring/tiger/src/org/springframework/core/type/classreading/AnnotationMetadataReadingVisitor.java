/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.core.type.classreading;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import org.springframework.core.type.AnnotationMetadata;

/**
 * ASM class visitor which looks for the class name and implemented types as
 * well as for the annotations defined on the class, exposing them through
 * the {@link org.springframework.core.type.AnnotationMetadata} interface.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 */
class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor implements AnnotationMetadata {

	private final Map<String, Map<String, Object>> attributesMap = new LinkedHashMap<String, Map<String, Object>>();

	private final Set<String> metaAnnotationTypes = new HashSet<String>();


	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String className = Type.getType(desc).getClassName();
		final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		return new EmptyVisitor() {
			public void visit(String name, Object value) {
				attributes.put(name, value);
			}
			public void visitEnd() {
				try {
					Class clazz = getClass().getClassLoader().loadClass(className);
					Annotation[] metaAnnotations = clazz.getAnnotations();
					for (Annotation metaAnnotation : metaAnnotations) {
						metaAnnotationTypes.add(metaAnnotation.annotationType().getName());
					}
				}
				catch (ClassNotFoundException ex) {
					// Class not found - can't determine meta-annotations.
				}
				attributesMap.put(className, attributes);
			}
		};
	}


	public Set<String> getAnnotationTypes() {
		return this.attributesMap.keySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return this.attributesMap.containsKey(annotationType);
	}

	public Set<String> getMetaAnnotationTypes() {
		return this.metaAnnotationTypes;
	}

	public boolean hasMetaAnnotation(String annotationType) {
		return this.metaAnnotationTypes.contains(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return this.attributesMap.get(annotationType);
	}

}
