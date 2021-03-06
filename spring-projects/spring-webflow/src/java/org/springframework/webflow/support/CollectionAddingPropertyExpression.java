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
package org.springframework.webflow.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.expression.SetPropertyAttempt;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A property expression that can add an element to a collection when asked to
 * set its value.
 * @author Keith Donald
 */
public class CollectionAddingPropertyExpression implements PropertyExpression {

	/**
	 * The expression that resolves a mutable collection reference.
	 */
	private Expression collectionExpression;

	/**
	 * Creates a collection adding property expression.
	 * @param collectionExpression the collection expression
	 */
	public CollectionAddingPropertyExpression(Expression collectionExpression) {
		this.collectionExpression = collectionExpression;
	}

	public void setValue(Object target, Object value, Map context) throws EvaluationException {
		Object result = evaluateAgainst(target, context);
		Assert.isInstanceOf(Collection.class, result, "Not a collection: ");
		if (result == null) {
			throw new EvaluationException(new SetPropertyAttempt(collectionExpression, target, value, context),
					new IllegalArgumentException("The collection expression evaluated to a [null] reference"));
		}
		if (value != null) {
			((Collection)result).add(value);
		}
	}

	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		return collectionExpression.evaluateAgainst(target, context);
	}

	public String toString() {
		return new ToStringCreator(this).append("collectionExpression", collectionExpression).toString();
	}
}