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

package org.springframework.aop.support.aspectj;

/**
 * Implementation of AspectInstanceFactory that wraps a singleton instance
 * @author Rod Johnson
 * @since 1.3
 */
public class SingletonMetadataAwareAspectInstanceFactory extends SingletonAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {
	
	private final AspectMetadata metadata;
	
	public SingletonMetadataAwareAspectInstanceFactory(Object aspectInstance) {
		super(aspectInstance);
		this.metadata = new AspectMetadata(aspectInstance.getClass());
	}
	
	public AspectMetadata getAspectMetadata() {
		return this.metadata;
	}
}