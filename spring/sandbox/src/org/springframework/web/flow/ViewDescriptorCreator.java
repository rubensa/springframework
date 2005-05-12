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
package org.springframework.web.flow;


/**
 * Factory that produces a new, configured <code>ViewDescriptor</code> on each invocation,
 * taking into account the information in the provided flow execution request context.
 * <p>
 * This allows easy insertion of dynamic view descriptor configuration logic, for example to
 * vary the view to render or the available model for rendering based on some context.
 * @author Keith Donald
 */
public interface ViewDescriptorCreator {
	
	/**
	 * Create the view descriptor.
	 * @param context the current request context of the executing flow.
	 * @return the view descriptor.
	 */
	public ViewDescriptor createViewDescriptor(RequestContext context);
}
