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
package org.springframework.functor;

import org.springframework.functor.predicates.ParameterizedBinaryPredicate;
import org.springframework.functor.predicates.UnaryFunctionTester;
import org.springframework.functor.predicates.UnaryNot;

/**
 * A factory for easing the construction of predicates.
 * 
 * @author Keith Donald
 */
public class PredicateFactory {

    /**
     * Bind the specified parameter to the second argument of a BinaryPredicate,
     * returning a UnaryPredicate which will test a single variable argument
     * against the constant parameter.
     * 
     * @param predicate
     *            the binary predicate to bind to
     * @param parameter
     *            the parameter value (constant)
     * @return The unary predicate
     */
    public static UnaryPredicate bind(BinaryPredicate predicate,
            Object parameter) {
        return new ParameterizedBinaryPredicate(predicate, parameter);
    }

    /**
     * Attaches a predicate that tests the result returned by evaluating the
     * specified unary function.
     * 
     * @param tester
     *            the predicate to test the function result
     * @param function
     *            the function
     * @return The testing predicate, which on the call to test(o) first
     *         evaluates 'o' using the function and then tests the result.
     */
    public static UnaryPredicate attachResultTester(UnaryPredicate tester,
            UnaryFunction function) {
        return new UnaryFunctionTester(tester, function);
    }

    /**
     * Negate the specified predicate.
     * 
     * @param predicate
     *            The predicate to negate
     * @return The negated predicate.
     */
    public static UnaryPredicate negate(UnaryPredicate predicate) {
        if (predicate instanceof UnaryNot) {
            throw new IllegalArgumentException("Predicate is already negated");
        }
        return new UnaryNot(predicate);
    }

}