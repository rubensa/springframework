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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;

/**
 * A {@link ScopeMetadataResolver} implementation that (by default) checks for
 * the presence of the {@link Scope} annotation on the bean class.
 *
 * <p>The exact type of annotation that is checked for is configurable via the
 * {@link #setScopeAnnotationType(Class)} property.
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.1
 * @see Scope
 */
public class AnnotationScopeMetadataResolver implements ScopeMetadataResolver {

	private Class<? extends Annotation> scopeAnnotationType = Scope.class;
	
	private ScopedProxyMode scopedProxyMode;


	/**
	 * Create a new instance of the <code>AnnotationScopeMetadataResolver</code> class.
	 * @see #AnnotationScopeMetadataResolver(ScopedProxyMode)
	 * @see ScopedProxyMode#NO
	 */
	public AnnotationScopeMetadataResolver() {
		this(ScopedProxyMode.NO);
	}

	/**
	 * Create a new instance of the <code>AnnotationScopeMetadataResolver</code> class.
	 * @param scopedProxyMode the desired scoped-proxy-mode; must not be <code>null</code>
	 * @throws IllegalArgumentException if the supplied <code>scopedProxyMode</code> is <code>null</code>.
	 */
	public AnnotationScopeMetadataResolver(ScopedProxyMode scopedProxyMode) {
		Assert.notNull(scopedProxyMode, "'scopedProxyMode' cannot be null.");
		this.scopedProxyMode = scopedProxyMode;
	}


	/**
	 * Set the type of annotation that is checked for by this
	 * {@link AnnotationScopeMetadataResolver}.
	 * @param scopeAnnotationType the target annotation type; must not be <code>null</code>
	 * @throws IllegalArgumentException if the supplied <code>scopeAnnotationType</code> is <code>null</code>.
	 */
	public void setScopeAnnotationType(Class<? extends Annotation> scopeAnnotationType) {
		Assert.notNull(scopeAnnotationType, "'scopeAnnotationType' cannot be null.");
		this.scopeAnnotationType = scopeAnnotationType;
	}
	

	public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
		ScopeMetadata metadata = new ScopeMetadata();
		if (definition instanceof AnnotatedBeanDefinition) {
			AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
			Map<String, Object> attributes =
					annDef.getMetadata().getAnnotationAttributes(this.scopeAnnotationType.getName());
			if (attributes != null) {
				metadata.setScopeName((String) attributes.get("value"));
			}
			if (!metadata.getScopeName().equals(BeanDefinition.SCOPE_SINGLETON)) {
				metadata.setScopedProxyMode(this.scopedProxyMode);
			}
		}
		return metadata;
	}

}
