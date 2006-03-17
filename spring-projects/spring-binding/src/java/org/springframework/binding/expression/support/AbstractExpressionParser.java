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

import java.util.LinkedList;
import java.util.List;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.util.StringUtils;

/**
 * An expression parser that parses Ognl expressions.
 * @author Keith
 */
public abstract class AbstractExpressionParser implements ExpressionParser {

	/**
	 * The expression prefix.
	 */
	private static final String DEFAULT_EXPRESSION_PREFIX = "${";

	/**
	 * The expression suffix.
	 */
	private static final String DEFAULT_EXPRESSION_SUFFIX = "}";

	private String expressionPrefix = DEFAULT_EXPRESSION_PREFIX;
	
	private String expressionSuffix = DEFAULT_EXPRESSION_SUFFIX;

	public String getExpressionPrefix() {
		return expressionPrefix;
	}

	public void setExpressionPrefix(String expressionPrefix) {
		this.expressionPrefix = expressionPrefix;
	}

	public String getExpressionSuffix() {
		return expressionSuffix;
	}

	public void setExpressionSuffix(String expressionSuffix) {
		this.expressionSuffix = expressionSuffix;
	}

	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	public boolean isDelimitedExpression(String expressionString) {
		return (expressionString.startsWith(getExpressionPrefix()) && expressionString.endsWith(getExpressionSuffix()));
	}

	public final Expression parseExpression(String expressionString) throws ParserException {
		Expression[] expressions = parseExpressions(expressionString);
		if (expressions.length == 1) {
			return expressions[0];
		}
		else {
			return new CompositeStringExpression(expressions);
		}
	}

	/*
	 * Helper that parses given expression string using the configured parser.
	 * The expression string can contain any number of expressions, all
	 * contained in "${...}" markers. For instance: "foo${expr0}bar${expr1}".
	 * The static pieces of text will also be returned as Expressions that just
	 * return that static piece of text. As a result, evaluating all returned
	 * expressions and concating the results produces the complete evaluated
	 * string. @param expressionString the expression string @return the parsed
	 * expressions @throws ParserException when the expressions cannot be parsed
	 */
	private Expression[] parseExpressions(String expressionString) throws ParserException {
		List expressions = new LinkedList();
		if (StringUtils.hasText(expressionString)) {
			expressionString = cut(expressionString);
			int startIdx = 0;
			while (startIdx < expressionString.length()) {
				int exprStartIdx = expressionString.indexOf(getExpressionPrefix(), startIdx);
				if (exprStartIdx >= startIdx) {
					// an expression was found
					if (exprStartIdx > startIdx) {
						expressions.add(new StaticExpression(expressionString.substring(startIdx, exprStartIdx)));
						startIdx = exprStartIdx;
					}
					int exprEndIdx = expressionString.indexOf(getExpressionSuffix(), exprStartIdx);
					if (exprEndIdx >= exprStartIdx) {
						expressions.add(doParseExpression(cut(expressionString.substring(exprStartIdx, exprEndIdx + 1))));
						startIdx = exprEndIdx + 1;
					}
					else {
						expressions.add(new StaticExpression(expressionString.substring(startIdx)));
						startIdx = expressionString.length();
					}
				}
				else {
					if (startIdx == 0) {
						// treat entire string as one expression
						expressions.add(doParseExpression(expressionString));
					} else {
						// no more ${expressions} found in string
						expressions.add(new StaticExpression(expressionString.substring(startIdx)));
					}
					startIdx = expressionString.length();
				}
			}
		}
		else {
			expressions.add(new StaticExpression(expressionString));
		}
		return (Expression[])expressions.toArray(new Expression[expressions.size()]);
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 */
	protected String cut(String expressionString) {
		if (isDelimitedExpression(expressionString)) {
			return expressionString.substring(DEFAULT_EXPRESSION_PREFIX.length(), expressionString.length()
					- DEFAULT_EXPRESSION_SUFFIX.length());
		}
		else {
			return expressionString;
		}
	}

	protected abstract Expression doParseExpression(String expressionString);

	public abstract PropertyExpression parsePropertyExpression(String expressionString) throws ParserException,
			UnsupportedOperationException;

}