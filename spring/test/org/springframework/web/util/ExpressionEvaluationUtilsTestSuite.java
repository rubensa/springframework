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

package org.springframework.web.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import com.mockobjects.servlet.MockPageContext;
import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ExpressionEvaluationUtilsTestSuite extends TestCase {

	public void testIsExpressionLanguage() {
		// should be true
		String expr = "${bla}";		
		assertTrue(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be true
		expr = "bla${bla}";
		assertTrue(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be false
		expr = "bla{bla";
		assertFalse(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// should be false
		expr = "bla$b{";
		assertFalse(ExpressionEvaluationUtils.isExpressionLanguage(expr));
		
		// ok, tested enough ;-)
	}

	public void testEvaluate() throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";
		
		Object o = ExpressionEvaluationUtils.evaluate("test", expr, String.class, ctx);
		assertEquals(o, "blie");
		
		assertEquals(new String("test"),
		             ExpressionEvaluationUtils.evaluate("test", "test", Float.class, ctx));
	}

	public void testEvaluateString() throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", "blie");
		String expr = "${bla}";
		Object o = ExpressionEvaluationUtils.evaluateString("test", expr, ctx);
		assertEquals(o, "blie");
		
		assertEquals("blie", ExpressionEvaluationUtils.evaluateString("test", "blie", ctx));
	}

	public void testEvaluateInteger() throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Integer(1));
		String expr = "${bla}";
		
		int i = ExpressionEvaluationUtils.evaluateInteger("test", expr, ctx);
		assertEquals(i, 1);
		
		assertEquals(21, ExpressionEvaluationUtils.evaluateInteger("test", "21", ctx));
	}

	public void testEvaluateBoolean() throws Exception {
		PageContext ctx = getMockPageContext();
		
		ctx.setAttribute("bla", new Boolean(true));
		String expr = "${bla}";
		
		boolean b = ExpressionEvaluationUtils.evaluateBoolean("test", expr, ctx);
		assertEquals(b, true);
		
		assertEquals(true, ExpressionEvaluationUtils.evaluateBoolean("test", "true", ctx));
	}
	
	private PageContext getMockPageContext() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(null,
			"POST", "http://www.springframework.org");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext() {
			private Map attributes = new HashMap();
			public void setAttribute(String s, Object o) {
				attributes.put(s, o);
			}
			public Object findAttribute(String s) {
				return this.attributes.get(s);
			}
		};
		pageContext.initialize(null, request, response, "nope", false, 4096, true);
		return pageContext;		
	}

}
