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
package org.springframework.binding.mapping;

import java.util.Map;

/**
 * A lightweight service interface for mapping between two attribute maps.
 * <p>
 * Implementations of this interface are expected to encapsulate the mapping
 * configuration information as well as the logic to act on it to perform
 * mapping between a given source and target map.
 * @author Keith Donald
 */
public interface AttributeMapper {

	/**
	 * Map data from one map to another map.
	 * @param source The accessor to the source map
	 * @param target The setter to the target map
	 * @param mappingContext the mapping context
	 */
	public void map(Object source, Object target, Map mappingContext);
}