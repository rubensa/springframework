/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation.rules;

import org.springframework.functor.BinaryOperator;
import org.springframework.functor.BinaryPredicate;
import org.springframework.functor.PredicateFactory;
import org.springframework.functor.UnaryPredicate;
import org.springframework.functor.functions.StringLengthFunction;
import org.springframework.functor.predicates.NumberRange;

public class StringLengthConstraint implements UnaryPredicate {
    private UnaryPredicate predicate;

    public StringLengthConstraint(BinaryOperator operator, int length) {
        BinaryPredicate comparer = operator.getPredicate();
        UnaryPredicate lengthConstraint = PredicateFactory.bindConstant(
                comparer, new Integer(length));
        this.predicate = PredicateFactory.attachResultEvaluator(
                lengthConstraint, StringLengthFunction.instance());
    }

    public StringLengthConstraint(int min, int max) {
        UnaryPredicate rangeConstraint = new NumberRange(new Integer(min),
                new Integer(max));
        this.predicate = PredicateFactory.attachResultEvaluator(
                rangeConstraint, StringLengthFunction.instance());
    }

    public boolean evaluate(Object value) {
        return this.predicate.evaluate(value);
    }

}