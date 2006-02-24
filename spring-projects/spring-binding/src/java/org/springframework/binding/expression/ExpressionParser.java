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
package org.springframework.binding.expression;

/**
 * Parses expression strings, returing a configured evaluator instance capable
 * of performing parsed expression evaluation in a thread safe way.
 * 
 * @author Keith Donald
 */
public interface ExpressionParser {

	/**
	 * Is this expression string actually a parseable expression?
	 * @param expressionString the proposed expression string
	 * @return true if yes, false if not
	 */
	public boolean isExpression(String expressionString);

	/**
	 * Parse the provided expression string, returning an evaluator capable of
	 * evaluating it against input.
	 * @param expressionString the parseable expression
	 * @return the evaluator for the parsed expression
	 * @throws ParserException an exception occured during parsing
	 */
	public Expression parseExpression(String expressionString) throws ParserException;

	/**
	 * Parse the provided string containing multiple expressions, returning an
	 * array of evaluatable expressions.
	 * @param expressionString the parseable expression
	 * @return the evaluator for the parsed expression
	 * @throws ParserException an exception occured during parsing
	 */
	public Expression[] parseExpressions(String expressionString) throws ParserException;

	/**
	 * Parse the provided property expression string, returning an evaluator
	 * capable of evaluating its value as well as setting its value.
	 * @param expressionString the parseable property path expression
	 * @return the evaluator for the parsed expression
	 * @throws ParserException an exception occured during parsing
	 * @throws UnsupportedOperationException this parser does not support
	 * property expressions
	 */
	public PropertyExpression parsePropertyExpression(String expressionString) throws ParserException,
			UnsupportedOperationException;

}