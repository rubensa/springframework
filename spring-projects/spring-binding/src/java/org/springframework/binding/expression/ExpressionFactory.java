package org.springframework.binding.expression;

import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.binding.support.Assert;

/**
 * A static factory for producing configured expression evaluators.
 * @author Keith
 */
public class ExpressionFactory {
	
	/**
	 * Is the provided string a parseable expression?
	 * 
	 * @param expressionString the potentially parseable expression string
	 * @return yes if parseable, false if not
	 */
	public static boolean isParseableExpression(String expressionString) {
		return ExpressionParserUtils.getDefaultExpressionParser().isExpression(expressionString);
	}
	
	/**
	 * Return the evaluator for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator
	 */
	public static Expression parseExpression(String expressionString) {
		return ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
	}
	
	/**
	 * Retrun the evaluator/setter for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator setter
	 */
	public static PropertyExpression parsePropertyExpression(String expressionString) {
		Expression evaluator = ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
		Assert.isInstanceOf(PropertyExpression.class, evaluator, "The expression evaluator is not a PropertyExpressionEvaluator");
		return (PropertyExpression)evaluator;
	}
}