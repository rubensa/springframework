/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.rules.factory;

import java.util.Set;

import org.springframework.rules.closure.BinaryConstraint;
import org.springframework.rules.constraint.And;
import org.springframework.rules.constraint.ClosureResultConstraint;
import org.springframework.rules.constraint.EqualTo;
import org.springframework.rules.constraint.GreaterThan;
import org.springframework.rules.constraint.GreaterThanEqualTo;
import org.springframework.rules.constraint.InGroup;
import org.springframework.rules.constraint.LessThan;
import org.springframework.rules.constraint.LessThanEqualTo;
import org.springframework.rules.constraint.Like;
import org.springframework.rules.constraint.MethodInvokingConstraint;
import org.springframework.rules.constraint.Not;
import org.springframework.rules.constraint.Or;
import org.springframework.rules.constraint.ParameterizedBinaryConstraint;
import org.springframework.rules.constraint.Range;
import org.springframework.rules.constraint.RegexpConstraint;
import org.springframework.rules.constraint.RelationalOperator;
import org.springframework.rules.constraint.Required;
import org.springframework.rules.constraint.StringLengthConstraint;
import org.springframework.rules.constraint.Like.LikeType;
import org.springframework.rules.constraint.property.CompoundPropertyConstraint;
import org.springframework.rules.constraint.property.NegatedPropertyConstraint;
import org.springframework.rules.constraint.property.ParameterizedPropertyConstraint;
import org.springframework.rules.constraint.property.PropertiesConstraint;
import org.springframework.rules.constraint.property.PropertyConstraint;
import org.springframework.rules.constraint.property.PropertyValueConstraint;
import org.springframework.rules.constraint.property.UniquePropertyValueConstraint;
import org.springframework.util.Assert;
import org.springframework.util.closure.Closure;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.AlgorithmsAccessor;

/**
 * A factory for easing the construction and composition of predicates.
 *
 * @author Keith Donald
 */
public class Constraints extends AlgorithmsAccessor {

	private static Constraints INSTANCE = new Constraints();

	public Constraints() {

	}

	public static Constraints instance() {
		return INSTANCE;
	}

	public static void load(Constraints sharedInstance) {
		Assert.notNull(sharedInstance, "The global constraints factory cannot be null");
		INSTANCE = sharedInstance;
	}

	/**
	 * Bind the specified parameter to the second argument of the
	 * <code>BinaryConstraint</code>. The result is a
	 * <code>Constraint</code> which will test a single variable argument
	 * against the constant parameter.
	 *
	 * @param constraint
	 *            the binary constraint to bind to
	 * @param parameter
	 *            the parameter value (constant)
	 * @return The constraint
	 */
	public Constraint bind(BinaryConstraint constraint, Object parameter) {
		return new ParameterizedBinaryConstraint(constraint, parameter);
	}

	/**
	 * Bind the specified <code>int</code> parameter to the second argument of
	 * the <code>BinaryConstraint</code>. The result is a
	 * <code>Constraint</code> which will test a single variable argument
	 * against the constant <code>int</code> parameter.
	 *
	 * @param constraint
	 *            the binary constraint to bind to
	 * @param parameter
	 *            the <code>int</code> parameter value (constant)
	 * @return The constraint
	 */
	public Constraint bind(BinaryConstraint constraint, int parameter) {
		return new ParameterizedBinaryConstraint(constraint, parameter);
	}

	/**
	 * Bind the specified <code>float</code> parameter to the second argument
	 * of the <code>BinaryConstraint</code>. The result is a
	 * <code>Constraint</code> which will test a single variable argument
	 * against the constant <code>float</code> parameter.
	 *
	 * @param constraint
	 *            the binary constraint to bind to
	 * @param parameter
	 *            the <code>float</code> parameter value (constant)
	 * @return The constraint
	 */
	public Constraint bind(BinaryConstraint constraint, float parameter) {
		return new ParameterizedBinaryConstraint(constraint, parameter);
	}

	/**
	 * Bind the specified <code>double</code> parameter to the second argument
	 * of the <code>BinaryConstraint</code>. The result is a
	 * <code>Constraint</code> which will test a single variable argument
	 * against the constant <code>double</code> parameter.
	 *
	 * @param constraint
	 *            the binary constraint to bind to
	 * @param parameter
	 *            the <code>double</code> parameter value (constant)
	 * @return The constraint
	 */
	public Constraint bind(BinaryConstraint constraint, double parameter) {
		return new ParameterizedBinaryConstraint(constraint, parameter);
	}

	/**
	 * Bind the specified <code>boolean</code> parameter to the second
	 * argument of the <code>BinaryConstraint</code>. The result is a
	 * <code>Constraint</code> which will test a single variable argument
	 * against the constant <code>boolean</code> parameter.
	 *
	 * @param constraint
	 *            the binary constraint to bind to
	 * @param parameter
	 *            the <code>boolean</code> parameter value (constant)
	 * @return The  constraint
	 */
	public Constraint bind(BinaryConstraint constraint, boolean parameter) {
		return new ParameterizedBinaryConstraint(constraint, parameter);
	}

	/**
	 * Attaches a constraint that tests the result returned by evaluating the
	 * specified closure. This effectively attaches a constraint on the
	 * closure return value.
	 *
	 * @param closure
	 *            the closure
	 * @param constraint
	 *            the constraint to test the closure result
	 * @return The testing constraint, which on the call to test(o) first
	 *         evaluates 'o' using the closure and then tests the result.
	 */
	public Constraint testResultOf(Closure closure, Constraint constraint) {
		return new ClosureResultConstraint(closure, constraint);
	}

	/**
	 * AND two predicates.
	 *
	 * @param predicate1
	 *            the first constraint
	 * @param predicate2
	 *            the second constraint
	 * @return The compound AND constraint
	 */
	public Constraint and(Constraint predicate1, Constraint predicate2) {
		return new And(predicate1, predicate2);
	}

	/**
	 * Return the conjunction (all constraint) for all predicates.
	 *
	 * @param predicates
	 *            the predicates
	 * @return The compound AND constraint
	 */
	public Constraint all(Constraint[] predicates) {
		return new And(predicates);
	}

	/**
	 * Returns a new, empty conjunction prototype, capable of composing
	 * individual predicates where 'ALL' must test true.
	 *
	 * @return the UnaryAnd
	 */
	public And conjunction() {
		return new And();
	}

	/**
	 * OR two predicates.
	 *
	 * @param predicate1
	 *            the first constraint
	 * @param predicate2
	 *            the second constraint
	 * @return The compound OR constraint
	 */
	public Constraint or(Constraint predicate1, Constraint predicate2) {
		return new Or(predicate1, predicate2);
	}

	/**
	 * Return the disjunction (any constraint) for all predicates.
	 *
	 * @param predicates
	 *            the predicates
	 * @return The compound AND constraint
	 */
	public Constraint any(Constraint[] predicates) {
		return new Or(predicates);
	}

	/**
	 * Negate the specified constraint.
	 *
	 * @param constraint
	 *            The constraint to negate
	 * @return The negated constraint.
	 */
	public Constraint not(Constraint constraint) {
		if (!(constraint instanceof Not)) {
			return new Not(constraint);
		}
		else {
			return ((Not)constraint).getConstraint();
		}
	}

	/**
	 * Returns a new, empty disjunction prototype, capable of composing
	 * individual predicates where 'ANY' must test true.
	 *
	 * @return the UnaryAnd
	 */
	public Or disjunction() {
		return new Or();
	}

	/**
	 * Returns a 'in' group (or set) constraint.
	 *
	 * @param group
	 *            the group items
	 * @return The InGroup constraint
	 */
	public Constraint inGroup(Set group) {
		return new InGroup(group);
	}

	/**
	 * Returns a 'in' group (or set) constraint.
	 *
	 * @param group
	 *            the group items
	 * @return The InGroup constraint.
	 */
	public Constraint inGroup(Object[] group) {
		return new InGroup(group);
	}

	/**
	 * Returns a 'in' group (or set) constraint appled to the provided property.
	 *
	 * @param propertyName
	 *            the property
	 * @param group
	 *            the group items
	 * @return The InGroup constraint.
	 */
	public PropertyConstraint inGroup(String propertyName, Object[] group) {
		return value(propertyName, new InGroup(group));
	}

	/**
	 * Returns a 'like' constraint.
	 *
	 * @param encodedLikeString
	 *            the likeString
	 * @return The Like constraint.
	 */
	public Constraint like(String encodedLikeString) {
		return new Like(encodedLikeString);
	}

	/**
	 * Return a 'like' constraint applied as a value constraint to the provided
	 * property.
	 *
	 * @param property
	 *            The property to constrain
	 * @param likeType
	 *            The like type
	 * @param value
	 *            The like string value to match
	 * @return The Like constraint
	 */
	public PropertyConstraint like(String property, LikeType likeType, String value) {
		return value(property, new Like(likeType, value));
	}

	/**
	 * Returns a required constraint.
	 *
	 * @return The required constraint instance.
	 */
	public Constraint required() {
		return Required.instance();
	}

	/**
	 * Returns a required bean property expression.
	 *
	 * @return The required constraint instance.
	 */
	public PropertyConstraint required(String property) {
		return value(property, required());
	}

	/**
	 * Returns a maxlength constraint.
	 *
	 * @param maxLength
	 *            The maximum length in characters.
	 * @return The configured maxlength constraint.
	 */
	public Constraint maxLength(int maxLength) {
		return new StringLengthConstraint(maxLength);
	}

	/**
	 * Returns a minlength constraint.
	 *
	 * @param minLength
	 *            The minimum length in characters.
	 * @return The configured minlength constraint.
	 */
	public Constraint minLength(int minLength) {
		return new StringLengthConstraint(RelationalOperator.GREATER_THAN_EQUAL_TO, minLength);
	}

	/**
	 * Creates a constraint backed by a regular expression.
	 *
	 * @param regexp
	 *            The regular expression string.
	 * @return The constraint.
	 */
	public Constraint regexp(String regexp) {
		return new RegexpConstraint(regexp);
	}

	/**
	 * Creates a constraint backed by a regular expression, with a type for
	 * reporting.
	 *
	 * @param regexp
	 *            The regular expression string.
	 * @return The constraint.
	 */
	public Constraint regexp(String regexp, String type) {
		RegexpConstraint c = new RegexpConstraint(regexp);
		c.setType(type);
		return c;
	}

	/**
	 * Returns a constraint whose test is determined by a boolean method on a
	 * target object.
	 *
	 * @param targetObject
	 *            The targetObject
	 * @param methodName
	 *            The method name
	 * @return The constraint.
	 */
	public Constraint method(Object target, String methodName, String constraintType) {
		return new MethodInvokingConstraint(target, methodName, constraintType);
	}

	/**
	 * Attach a value constraint for the provided bean property.
	 *
	 * @param propertyName
	 *            the bean property name
	 * @param valueConstraint
	 *            the value constraint
	 * @return The bean property expression that tests the constraint
	 */
	public PropertyConstraint value(String propertyName, Constraint valueConstraint) {
		return new PropertyValueConstraint(propertyName, valueConstraint);
	}

	/**
	 * Apply an "all" value constraint to the provided bean property.
	 *
	 * @param propertyName
	 *            The bean property name
	 * @param constraints
	 *            The constraints that form a all conjunction
	 * @return
	 */
	public PropertyConstraint all(String propertyName, Constraint[] constraints) {
		return value(propertyName, all(constraints));
	}

	/**
	 * Apply an "any" value constraint to the provided bean property.
	 *
	 * @param propertyName
	 *            The bean property name
	 * @param constraints
	 *            The constraints that form a all disjunction
	 * @return
	 */
	public PropertyConstraint any(String propertyName, Constraint[] constraints) {
		return value(propertyName, any(constraints));
	}

	/**
	 * Negate a bean property expression.
	 *
	 * @param e
	 *            the expression to negate
	 * @return The negated expression
	 */
	public PropertyConstraint not(PropertyConstraint e) {
		return new NegatedPropertyConstraint(e);
	}

	/**
	 * Apply a "equal to" constraint to a bean property.
	 *
	 * @param propertyName
	 *            The first property
	 * @param propertyValue
	 *            The constraint value
	 * @return The constraint
	 */
	public PropertyConstraint eq(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, EqualTo.instance(), propertyValue);
	}

	/**
	 * Apply a "greater than" constraint to a bean property.
	 *
	 * @param propertyName
	 *            The first property
	 * @param propertyValue
	 *            The constraint value
	 * @return The constraint
	 */
	public PropertyConstraint gt(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, GreaterThan.instance(), propertyValue);
	}

	/**
	 * Apply a "greater than equal to" constraint to a bean property.
	 *
	 * @param propertyName
	 *            The first property
	 * @param propertyValue
	 *            The constraint value
	 * @return The constraint
	 */
	public PropertyConstraint gte(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, GreaterThanEqualTo.instance(), propertyValue);
	}

	/**
	 * Apply a "less than" constraint to a bean property.
	 *
	 * @param propertyName
	 *            The first property
	 * @param propertyValue
	 *            The constraint value
	 * @return The constraint
	 */
	public PropertyConstraint lt(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, LessThan.instance(), propertyValue);
	}

	/**
	 * Apply a "less than equal to" constraint to a bean property.
	 *
	 * @param propertyName
	 *            The first property
	 * @param propertyValue
	 *            The constraint value
	 * @return The constraint
	 */
	public PropertyConstraint lte(String propertyName, Object propertyValue) {
		return new ParameterizedPropertyConstraint(propertyName, LessThanEqualTo.instance(), propertyValue);
	}

	/**
	 * Apply a "greater than" constraint to two properties
	 *
	 * @param propertyName
	 *            The first property
	 * @param otherPropertyName
	 *            The other property
	 * @return The constraint
	 */
	public PropertyConstraint gtProperty(String propertyName, String otherPropertyName) {
		return new PropertiesConstraint(propertyName, GreaterThan.instance(), otherPropertyName);
	}

	/**
	 * Apply a "equal to" constraint to two bean properties.
	 *
	 * @param propertyName
	 *            The first property
	 * @param otherPropertyName
	 *            The other property
	 * @return The constraint
	 */
	public PropertyConstraint eqProperty(String propertyName, String otherPropertyName) {
		return new PropertiesConstraint(propertyName, EqualTo.instance(), otherPropertyName);
	}

	/**
	 * Apply a "greater than or equal to" constraint to two properties.
	 *
	 * @param propertyName
	 *            The first property
	 * @param otherPropertyName
	 *            The other property
	 * @return The constraint
	 */
	public PropertyConstraint gteProperty(String propertyName, String otherPropertyName) {
		return new PropertiesConstraint(propertyName, GreaterThanEqualTo.instance(), otherPropertyName);
	}

	/**
	 * Apply a "less than" constraint to two properties.
	 *
	 * @param propertyName
	 *            The first property
	 * @param otherPropertyName
	 *            The other property
	 * @return The constraint
	 */
	public PropertyConstraint ltProperty(String propertyName, String otherPropertyName) {
		return new PropertiesConstraint(propertyName, LessThan.instance(), otherPropertyName);
	}

	/**
	 * Apply a "less than or equal to" constraint to two properties.
	 *
	 * @param propertyName
	 *            The first property
	 * @param otherPropertyName
	 *            The other property
	 * @return The constraint
	 */
	public PropertyConstraint lteProperty(String propertyName, String otherPropertyName) {
		return new PropertiesConstraint(propertyName, LessThanEqualTo.instance(), otherPropertyName);
	}

	/**
	 * Apply a inclusive "range" constraint to a bean property.
	 *
	 * @param propertyName
	 *            the property with the range constraint.
	 * @param min
	 *            the low edge of the range
	 * @param max
	 *            the high edge of the range
	 * @return The range constraint constraint
	 */
	public PropertyConstraint inRange(String propertyName, Comparable min, Comparable max) {
		Range range = new Range(min, max);
		return value(propertyName, range);
	}

	/**
	 * Apply a inclusive "range" constraint between two other properties to a
	 * bean property.
	 *
	 * @param propertyName
	 *            the property with the range constraint.
	 * @param minPropertyName
	 *            the low edge of the range
	 * @param maxPropertyName
	 *            the high edge of the range
	 * @return The range constraint constraint
	 */
	public PropertyConstraint inRangeProperties(String propertyName, String minPropertyName, String maxPropertyName) {
		PropertiesConstraint min = new PropertiesConstraint(propertyName, GreaterThanEqualTo.instance(), minPropertyName);
		PropertiesConstraint max = new PropertiesConstraint(propertyName, LessThanEqualTo.instance(), maxPropertyName);
		return new CompoundPropertyConstraint(new And(min, max));
	}

	public PropertyConstraint unique(String propertyName) {
		return new UniquePropertyValueConstraint(propertyName);
	}

}