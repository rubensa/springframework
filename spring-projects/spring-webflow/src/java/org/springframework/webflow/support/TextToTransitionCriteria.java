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
package org.springframework.webflow.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.WildcardTransitionCriteria;

/**
 * Converter that takes an encoded string representation and produces
 * a corresponding <code>TransitionCriteria</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in a TransitionCriteria object that matches on everything
 * ({@link org.springframework.webflow.WildcardTransitionCriteria})
 * </li>
 * <li>"eventId" - will result in a TransitionCriteria object that matches given
 * event id ({@link org.springframework.webflow.support.EventIdTransitionCriteria})
 * </li>
 * <li>"${...}" - will result in a TransitionCriteria object that evaluates given
 * condition, expressed as an expression
 * ({@link org.springframework.webflow.support.BooleanExpressionTransitionCriteria})
 * </li>
 * <li>"class:&lt;classname&gt;" - will result in instantiation and usage of a custom 
 * TransitionCriteria implementation. The implementation must have a public no-arg constructor.
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends ConversionServiceAwareConverter {
	
	/**
	 * Create a new converter that converts strings to transition
	 * criteria objects.
	 */
	public TextToTransitionCriteria() {
	}

	/**
	 * Create a new converter that converts strings to transition
	 * criteria objects. The given conversion service will be used to do
	 * all necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTransitionCriteria(ConversionService conversionService) {
		super(conversionService);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class } ;
	}
	
	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria) ||
				WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return WildcardTransitionCriteria.INSTANCE;
		}
		else if (encodedCriteria.startsWith(CLASS_PREFIX)) {
			Object o = newInstance(encodedCriteria);
			Assert.isInstanceOf(TransitionCriteria.class, o, "Encoded criteria class is of wrong type: ");
			return (TransitionCriteria)o;
		}
		else {
			Expression expression = (Expression)fromStringTo(Expression.class).execute(encodedCriteria);
			if (expression instanceof StaticExpression) {
				return createEventIdTransitionCriteria(encodedCriteria);
			}
			else {
				return createBooleanExpressionTransitionCriteria(expression);
			}
		}
	}
	
	/**
	 * Hook method subclasses can override to return a specialized eventId matching
	 * transition criteria implementation.
	 * @param eventId the event id to match
	 * @return the transition criteria object
	 * @throws ConversionException when something goes wrong
	 */
	protected TransitionCriteria createEventIdTransitionCriteria(String eventId) throws ConversionException {
		return new EventIdTransitionCriteria(eventId);
	}
	
	/**
	 * Hook method subclasses can override to return a specialized expression evaluating
	 * transition criteria implementation.
	 * @param expression the expression to evaluate
	 * @return the transition criteria object
	 * @throws ConversionException when something goes wrong
	 */
	protected TransitionCriteria createBooleanExpressionTransitionCriteria(Expression expression) throws ConversionException {
		return new BooleanExpressionTransitionCriteria(expression);
	}
}