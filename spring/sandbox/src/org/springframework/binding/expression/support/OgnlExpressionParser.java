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
package org.springframework.binding.expression.support;


import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParseException;

/**
 * An expression parser that parses Ognl expressions.
 * @author Keith
 */
public class OgnlExpressionParser implements ExpressionParser {

	/**
	 * The expression prefix.
	 */
	private static final String EXPRESSION_PREFIX = "${";

	/**
	 * The expression suffix.
	 */
	private static final String EXPRESSION_SUFFIX = "}";
	
	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	public boolean isExpression(String encodedCriteria) {
		return (encodedCriteria.startsWith(EXPRESSION_PREFIX) && encodedCriteria.endsWith(EXPRESSION_SUFFIX));
	}

	/* (non-Javadoc)
	 * @see org.springframework.binding.expression.ExpressionParser#parseExpression(java.lang.String)
	 */
	public ExpressionEvaluator parseExpression(String expressionString) throws ParseException {
		try {
			return new OgnlExpressionEvaluator(Ognl.parseExpression(cutExpression(expressionString)));
		} catch (OgnlException e) {
			throw new ParseException(expressionString, e);
		}
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 */
	private String cutExpression(String encodedCriteria) {
		if (isExpression(encodedCriteria)) {
			return encodedCriteria.substring(
				EXPRESSION_PREFIX.length(),
				encodedCriteria.length() - EXPRESSION_SUFFIX.length());
		} else {
			return encodedCriteria;
		}
	}
}