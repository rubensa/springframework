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

package org.springframework.beans.factory.parsing;

/**
 * Simple implementation of the {@link SourceExtractor} that simply passes
 * the candidate source metadata object through for attachment.
 * 
 * <p>Using this implementation means that tools will get raw access to the
 * underlying configuration source metadata provided by the tool.
 * 
 * <p>This implementation <strong>should not</strong> be used in a production
 * application since it is likely to keep too much metadata in memory
 * unnecessarily.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class PassThroughSourceExtractor implements SourceExtractor {

	/**
	 * Simply returns the supplied <code>sourceCandidate</code> as-is.
	 * @param sourceCandidate the source metatada
	 * @return the supplied <code>sourceCandidate</code>
	 */
	public Object extract(Object sourceCandidate) {
		return sourceCandidate;
	}

}
