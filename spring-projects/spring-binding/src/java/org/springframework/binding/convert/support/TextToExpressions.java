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
package org.springframework.binding.convert.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.expression.support.CompositeStringExpression;
import org.springframework.binding.expression.support.ExpressionParserUtils;

/**
 * Converter that converts a String into an array of evaluatable Expression
 * object.
 * 
 * @see org.springframework.binding.expression.Expression
 * @see org.springframework.binding.expression.PropertyExpression
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class TextToExpressions extends AbstractConverter {

	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();

	/**
	 * Returns the expression parser used by this converter.
	 */
	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	/**
	 * Set the expression parser used by this converter.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { CompositeStringExpression.class, Expression[].class, PropertyExpression[].class };
	}

	protected Object doConvert(Object source, Class targetClass, Map context) throws Exception {
		if (targetClass.equals(CompositeStringExpression.class)) {
			return new CompositeStringExpression(getExpressionParser().parseExpressions((String)source,
					Collections.EMPTY_MAP));
		}
		else {
			return getExpressionParser().parseExpressions((String)source, context);
		}
	}
}