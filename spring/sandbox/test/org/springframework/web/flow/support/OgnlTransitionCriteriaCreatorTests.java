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
package org.springframework.web.flow.support;

import junit.framework.TestCase;

import ognl.ExpressionSyntaxException;

import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.SimpleEvent;

/**
 * Test case for OgnlTransitionCriteriaCreator.
 * 
 * @author Rob Harrop
 */
public class OgnlTransitionCriteriaCreatorTests extends TestCase {

	public void testTrueEvaluation() throws Exception {
		String expression = "flowScope.foo == 'bar'";
		OgnlTransitionCriteriaCreator.OgnlTransitionCriteria criterion =
			new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria(expression);
		RequestContext ctx = getRequestContext();
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));

	}

	public void testFalseEvaluation() throws Exception {
		String expression = "flowScope.foo != 'bar'";
		OgnlTransitionCriteriaCreator.OgnlTransitionCriteria criterion =
			new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria(expression);
		RequestContext ctx = getRequestContext();
		assertFalse("Criterion should evaluate to false", criterion.test(ctx));
	}

	public void testNonBooleanEvaluation() throws Exception {
		String expression = "flowScope.foo";
		OgnlTransitionCriteriaCreator.OgnlTransitionCriteria criterion =
			new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria(expression);
		RequestContext ctx = getRequestContext();

		try {
			criterion.test(ctx);
			fail("Non-boolean evaluations are not allowed");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	public void testInvalidSyntax() throws Exception {
		try {
			new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria("&foo<<m");
			fail("Syntax error should throw ExpressionSyntaxException");
		}
		catch (ExpressionSyntaxException ex) {
			// success
		}
	}
	
	public void testEventId() throws Exception {
		OgnlTransitionCriteriaCreator.OgnlTransitionCriteria criterion =
			new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria("lastEvent.id == 'sample'");
		RequestContext ctx = getRequestContext();
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));
		
		criterion = new OgnlTransitionCriteriaCreator.OgnlTransitionCriteria("#result == 'sample'");
		assertTrue("Criterion should evaluate to true", criterion.test(ctx));
	}

	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.setLastEvent(new SimpleEvent(this, "sample"));
		ctx.getFlowScope().setAttribute("foo", "bar");
		return ctx;
	}
}