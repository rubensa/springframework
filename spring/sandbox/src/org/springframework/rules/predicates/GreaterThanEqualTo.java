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
package org.springframework.rules.predicates;

import java.util.Comparator;

import org.springframework.rules.BinaryPredicate;
import org.springframework.rules.RelationalOperator;

/**
 * Predicate that tests if one comparable object is greater than or equal to
 * another.
 * 
 * @author Keith Donald
 */
public class GreaterThanEqualTo extends ComparisonBinaryPredicate implements
        BinaryPredicate {
    private static final GreaterThanEqualTo INSTANCE = new GreaterThanEqualTo();

    public GreaterThanEqualTo() {
        super();
    }

    public GreaterThanEqualTo(Comparator comparator) {
        super(comparator);
    }

    protected boolean testCompareResult(int result) {
        return result >= 0;
    }

    public static BinaryPredicate instance() {
        return INSTANCE;
    }

    public static BinaryPredicate instance(Comparator c) {
        return new GreaterThanEqualTo(c);
    }

    public String toString() {
        return RelationalOperator.GREATER_THAN_EQUAL_TO.toString();
    }
}