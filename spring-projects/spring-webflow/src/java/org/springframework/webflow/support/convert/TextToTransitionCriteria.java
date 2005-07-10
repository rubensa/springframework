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

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.expression.Expression;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionCriteriaFactory;

/**
 * Converter that takes an encoded string representation and produces
 * a corresponding <code>TransitionCriteria</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in a TransitionCriteria object that matches on everything
 * ({@link org.springframework.webflow.TransitionCriteriaFactory#alwaysTrue()})
 * </li>
 * <li>"eventId" - will result in a TransitionCriteria object that matches given
 * event id ({@link org.springframework.webflow.TransitionCriteriaFactory#eventId(String)})
 * </li>
 * <li>"${...}" - will result in a TransitionCriteria object that evaluates given
 * condition, expressed as an expression
 * </li>
 * <li>"class:&lt;classname&gt;" - will result in instantiation and usage of a custom 
 * TransitionCriteria implementation. The implementation must have a public no-arg constructor.
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * @see org.springframework.webflow.TransitionCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends BaseConverter {
	
	/**
	 * Create a new converter that converts strings to transition
	 * criteria objects. The default expression parser will
	 * be used.
	 */
	public TextToTransitionCriteria() {
	}
		
	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class } ;
	}
	
	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria) || TransitionCriteriaFactory.WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return TransitionCriteriaFactory.alwaysTrue();
		}
		else if (isExpression(encodedCriteria)) {
			return createExpressionTransitionCriteria(encodedCriteria);
		}
		else if (encodedCriteria.startsWith(CLASS_PREFIX)) {
			Object o = parseAndInstantiateClass(encodedCriteria);
			Assert.isInstanceOf(TransitionCriteria.class, o, "Encoded criteria class is of wrong type: ");
			return (TransitionCriteria)o;
		}
		else {
			return TransitionCriteriaFactory.eventId(encodedCriteria);
		}
	}

	/**
	 * Factory method overridable by subclasses to customize expression-based
	 * transition criteria.
	 * @param expression the expression
	 * @return the criteria
	 * @throws ConversionException when there is a problem parsing the expression
	 */
	protected TransitionCriteria createExpressionTransitionCriteria(String expression) throws ConversionException {
		return new ExpressionTransitionCriteria(parseExpression(expression));
	}
	
	/**
	 * Transtition criteria that tests the value of an expression. The
	 * expression is used to express a condition that guards transition
	 * execution in a web flow.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 * @author Rob Harrop
	 */
	public static class ExpressionTransitionCriteria implements TransitionCriteria {

		private static final String RESULT_ALIAS = "result";
		
		/**
		 * The expression evaluator to use.
		 */
		private Expression evaluator;

		/**
		 * Create a new expression based transition criteria object.
		 * @param evaluator the expression evaluator testing the criteria,
		 *        this expression should be a condition that returns a Boolean value
		 */
		public ExpressionTransitionCriteria(Expression evaluator) {
			this.evaluator = evaluator;
		}

		public boolean test(RequestContext context) {
			Object result = this.evaluator.evaluateAgainst(context, getEvaluationContext(context));
			Assert.isInstanceOf(Boolean.class, result);
			return ((Boolean)result).booleanValue();
		}

		/**
		 * Setup a map with a few aliased values to make writing expression based
		 * transition conditions a bit easier.
		 */
		protected Map getEvaluationContext(RequestContext context) {
			Map evalContext = new HashMap();
			// ${#result == lastEvent.id}
			if (context.getLastEvent() != null) {
				evalContext.put(RESULT_ALIAS, context.getLastEvent().getId());
			}
			return evalContext;
		}

		public String toString() {
			return evaluator.toString();
		}
	}	
}