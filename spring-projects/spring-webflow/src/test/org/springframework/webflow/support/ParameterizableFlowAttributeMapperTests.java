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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.AttributeMapper;
import org.springframework.binding.support.Mapping;
import org.springframework.core.CollectionFactory;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for ParameterizableFlowAttributeMapper.
 * 
 * @author Erwin Vervaet
 */
public class ParameterizableFlowAttributeMapperTests extends TestCase {
	
	private ParameterizableFlowAttributeMapper mapper;
	private MockRequestContext context;
	private MockFlowSession parentSession;
	private MockFlowSession subflowSession;
	
	protected void setUp() throws Exception {
		mapper = new ParameterizableFlowAttributeMapper();
		context = new MockRequestContext();
		parentSession = new MockFlowSession();
		subflowSession = new MockFlowSession();
		subflowSession.setParent(parentSession);
	}
	
	public void testDirectMapping() {
		Mapping testMapping = new Mapping("${flowScope.x}", "${y}");
		mapper.setInputMapping(testMapping);
		mapper.setOutputMapping(testMapping);
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("x", "xValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(1, input.size());
		assertEquals("xValue", input.get("y"));
		
		parentSession.getScope().clear();
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("x", "xValue");
		mapper.mapSubflowOutput(context);
		assertEquals(1, parentSession.getScope().size());
		assertEquals("xValue", parentSession.getScope().get("y"));
	}
	
	public void testDirectAttributeMapper() {
		AttributeMapper testInputMapper = new AttributeMapper() {
			public void map(Object source, Object target, Map context) {
				assertTrue(source instanceof RequestContext);
				assertTrue(target instanceof Map);
				((Map)target).put("y", ((RequestContext)source).getFlowScope().get("x"));
			}
		};
		mapper.setInputMapper(testInputMapper);
		AttributeMapper testOutputMapper = new AttributeMapper() {
			public void map(Object source, Object target, Map context) {
				assertTrue(source instanceof RequestContext);
				assertTrue(target instanceof Scope);
				((Scope)target).setAttribute("y", ((RequestContext)source).getFlowScope().getAttribute("x"));
			}
		};
		mapper.setOutputMapper(testOutputMapper);
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("x", "xValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(1, input.size());
		assertEquals("xValue", input.get("y"));
		
		parentSession.getScope().clear();
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("x", "xValue");
		mapper.mapSubflowOutput(context);
		assertEquals(1, parentSession.getScope().size());
		assertEquals("xValue", parentSession.getScope().get("y"));
	}

	public void testSimpleMapping() {
		List mappings = new ArrayList();
		mappings.add("someAttribute");
		mappings.add("someOtherAttribute");
		mapper.setInputMappings(mappings);
		mapper.setOutputMappings(mappings);
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("someAttribute", "someValue");
		context.getFlowScope().setAttribute("someOtherAttribute", "someOtherValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("someValue", input.get("someAttribute"));
		assertEquals("someOtherValue", input.get("someOtherAttribute"));
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("someAttribute", "someUpdatedValue");
		context.getFlowScope().setAttribute("someOtherAttribute", "someOtherUpdatedValue");
		mapper.mapSubflowOutput(context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("someUpdatedValue", parentSession.getScope().get("someAttribute"));
		assertEquals("someOtherUpdatedValue", parentSession.getScope().get("someOtherAttribute"));
	}
	
	public void testMapMapping() {
		Map mappingsMap = new HashMap();
		mappingsMap.put("a", "b");
		mappingsMap.put("x", "y");
		mapper.setInputMappingsMap(mappingsMap);
		mapper.setOutputMappingsMap(mappingsMap);
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("aValue", input.get("b"));
		assertEquals("xValue", input.get("y"));
		
		parentSession.getScope().clear();
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		mapper.mapSubflowOutput(context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("aValue", parentSession.getScope().get("b"));
		assertEquals("xValue", parentSession.getScope().get("y"));
	}
	
	public void testMixedMapping() {
		List mappings = new ArrayList();
		mappings.add("a");
		Map mappingsMap = new HashMap();
		mappingsMap.put("x", "y");
		mappings.add(mappingsMap);
		mapper.setInputMappings(mappings);
		mapper.setOutputMappings(mappings);
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("aValue", input.get("a"));
		assertEquals("xValue", input.get("y"));
		
		parentSession.getScope().clear();
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		mapper.mapSubflowOutput(context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("aValue", parentSession.getScope().get("a"));
		assertEquals("xValue", parentSession.getScope().get("y"));
	}
	
	public static class TestBean {
		
		private String prop;
		
		public String getProp() {
			return prop;
		}
		
		public void setProp(String prop) {
			this.prop = prop;
		}
	}

	public void testBeanPropertyMapping() {
		Map mappingsMap = CollectionFactory.createLinkedMapIfPossible(3);
		mappingsMap.put("bean.prop", "attr");
		mappingsMap.put("bean", "otherBean");
		mappingsMap.put("otherAttr", "otherBean.prop");
		mapper.setInputMappingsMap(mappingsMap);
		mapper.setOutputMappingsMap(mappingsMap);

		TestBean bean = new TestBean();
		bean.setProp("value");
		
		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("bean", bean);
		context.getFlowScope().setAttribute("otherAttr", "otherValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(2, input.size());
		assertEquals("value", input.get("attr"));
		assertEquals("otherValue", ((TestBean)input.get("otherBean")).getProp());
		
		parentSession.getScope().clear();
		bean.setProp("value");
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("bean", bean);
		context.getFlowScope().setAttribute("otherAttr", "otherValue");
		mapper.mapSubflowOutput(context);
		assertEquals(2, parentSession.getScope().size());
		assertEquals("value", parentSession.getScope().get("attr"));
		assertEquals("otherValue", ((TestBean)parentSession.getScope().get("otherBean")).getProp());
	}
	
	public void testExpressionMapping() {
		Map mappingsMap = new HashMap();
		mappingsMap.put("${flowExecutionContext.key}", "key");
		mappingsMap.put("${requestScope.a}", "b");
		mappingsMap.put("${flowScope.x}", "y");
		mapper.setInputMappingsMap(mappingsMap);
		mapper.setOutputMappingsMap(mappingsMap);
		
		context.setActiveSession(parentSession);
		context.getRequestScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		Map input = mapper.createSubflowInput(context);
		assertEquals(3, input.size());
		assertEquals(context.getFlowExecutionContext().getKey(), input.get("key"));
		assertEquals("aValue", input.get("b"));
		assertEquals("xValue", input.get("y"));
		
		parentSession.getScope().clear();
		
		context.setActiveSession(subflowSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		mapper.mapSubflowOutput(context);
		assertEquals(3, parentSession.getScope().size());
		assertEquals(context.getFlowExecutionContext().getKey(), parentSession.getScope().get("key"));
		assertEquals("aValue", parentSession.getScope().get("b"));
		assertEquals("xValue", parentSession.getScope().get("y"));
	}

}
