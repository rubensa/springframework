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
package org.springframework.rules.constraint;

import org.springframework.rules.closure.BinaryConstraint;
import org.springframework.rules.closure.StringLength;
import org.springframework.util.Assert;
import org.springframework.util.closure.Constraint;

/**
 * Constraint to validate an object's string length.
 * 
 * @author Keith Donald
 */
public class StringLengthConstraint extends AbstractComposingConstraint {
    private Constraint lengthConstraint;

    /**
     * Constructs a maxlength constraint of the specified length.
     * 
     * @param length
     *            the max string length
     */
    public StringLengthConstraint(int length) {
        this(RelationalOperator.LESS_THAN_EQUAL_TO, length);
    }

    /**
     * Constructs a string length constraint with the specified operator and
     * length constraint.
     * 
     * @param operator
     *            the operator (one of ==, >, >=, <, <=)
     * @param length
     *            the length constraint
     */
    public StringLengthConstraint(RelationalOperator operator, int length) {
        Assert.notNull(operator, "The relational operator is required");
        Assert.isTrue(length > 0, "length is required");
        BinaryConstraint comparer = operator.getPredicate();
        Constraint lengthConstraint = bind(comparer, length);
        this.lengthConstraint = testResultOf(StringLength.instance(),
                lengthConstraint);
    }

    /**
     * Constructs a string length range constraint.
     * 
     * @param min
     *            The minimum edge of the range
     * @param max
     *            the maximum edge of the range
     */
    public StringLengthConstraint(int min, int max) {
        Constraint rangeConstraint = new Range(min, max);
        this.lengthConstraint = testResultOf(StringLength.instance(),
                rangeConstraint);
    }

    /**
     * Tests that the string form of this argument falls within the length
     * constraint.
     * 
     * @see org.springframework.util.closure.Constraint#test(java.lang.Object)
     */
    public boolean test(Object argument) {
        return this.lengthConstraint.test(argument);
    }

    public Constraint getPredicate() {
        return lengthConstraint;
    }

    public String toString() {
        return lengthConstraint.toString();
    }

}