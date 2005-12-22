/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.Scope;
import org.springframework.webflow.ScopeType;

/**
 * Expression evaluator that evaluates an expression in flow scope.
 * 
 * @author Keith Donald
 */
public class FlowScopeExpression implements Expression {

	/**
	 * The expression to evaluate.
	 */
	private Expression expression;

	/**
	 * Create a new expression evaluator that executes given expression 'in flow
	 * scope'. The expression will be parsed using the default expression
	 * parser.
	 * @param expressionString the expression string
	 */
	public FlowScopeExpression(String expressionString) {
		this(ExpressionFactory.parseExpression(expressionString));
	}

	/**
	 * Create a new expression evaluator that executes given expression 'in flow
	 * scope'.
	 * @param expression the nested evaluator to execute
	 */
	public FlowScopeExpression(Expression expression) {
		this.expression = expression;
	}

	/**
	 * Returns the expression that will be evaluated in 'flow scope'.
	 */
	protected Expression getExpression() {
		return expression;
	}

	public Object evaluateAgainst(Object target, Map context) throws EvaluationException {
		if (target instanceof RequestContext) {
			return expression.evaluateAgainst(((RequestContext)target).getFlowScope(), context);
		}
		else if (target instanceof Scope) {
			Scope scope = (Scope)target;
			if (scope.getScopeType() != ScopeType.FLOW) {
				throw new IllegalArgumentException("The Scope provided as input must be of ScopeType.FLOW");
			}
			return expression.evaluateAgainst(scope, context);
		}
		else {
			throw new IllegalArgumentException(
					"Only supports evaluation against a [RequestScope] or [Scope] instance of [ScopeType.FLOW], but was a [" + target.getClass() + "]");
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("expression", expression).toString();
	}
}