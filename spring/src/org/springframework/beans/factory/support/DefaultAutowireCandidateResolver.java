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

package org.springframework.beans.factory.support;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * Resolver to use when Java version is less than 1.5 and therefore no
 * annotation support is available. Simply checks the bean definition.
 * 
 * @author Mark Fisher
 * @since 2.1
 */
public class DefaultAutowireCandidateResolver extends AbstractAutowireCandidateResolver {

	public void addQualifierType(Class qualifierType) {
		throw new UnsupportedOperationException(getClass().getName() + 
				" does not support registration of custom qualifier types.");
	}

	public boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, TypeConverter typeConverter) {

		return mbd.isAutowireCandidate();
	}

}