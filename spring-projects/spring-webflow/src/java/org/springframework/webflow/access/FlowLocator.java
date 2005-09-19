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
package org.springframework.webflow.access;

import org.springframework.webflow.Flow;

/**
 * Service locator interface for retrieving a flow by id. Needed at runtime
 * time to load a configured flow instance from a registry, to support launching
 * new flow executions. The default registry is typically the Spring application context.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowLocator {

	/**
	 * Lookup a flow definition with specified id.
	 * @param id the flow definition id
	 * @return the flow
	 * @throws ServiceLookupException when the flow cannot be found
	 */
	public Flow getFlow(String id) throws ServiceLookupException;
}