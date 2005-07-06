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
package org.springframework.webflow.support;

import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.support.Mapping;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Erwin Vervaet
 */
public class ParameterizableFlowAttributeMapperTests extends TestCase {

	public void testSimpleMapping() {
		ParameterizableFlowAttributeMapper mapper = new ParameterizableFlowAttributeMapper();
		// FIXME this is not clear
		Mapping inputMapping = new Mapping("${flowScope.someAttribute}", "someAttribute");
		mapper.setInputMapping(inputMapping);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("someAttribute", "someValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(1, input.size());
		assertEquals("someValue", input.get("someAttribute"));
	}

}
