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
package org.springframework.webflow.support.convert;

import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.support.convert.TextToExpression;
import org.springframework.webflow.support.convert.TextToTransitionCriteria;
import org.springframework.webflow.support.convert.TextToViewDescriptorCreator;

/**
 * Conversion service used by the web flow system. This service
 * supports conversion for a number of web flow specific types.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowConversionService extends DefaultConversionService {
	
	/**
	 * Create a new web flow conversion service.
	 */
	public FlowConversionService() {
		// register web flow specific converters
		addConverter(new TextToTransitionCriteria());
		addConverter(new TextToViewDescriptorCreator());
		addConverter(new TextToFlowExecutionListenerCriteria());
		addConverter(new TextToExpression());
		addDefaultAlias(TransitionCriteria.class);
		addDefaultAlias(ViewDescriptorCreator.class);
		addDefaultAlias(FlowExecutionListenerCriteria.class);
		addDefaultAlias(TextToExpression.class);
	}
}