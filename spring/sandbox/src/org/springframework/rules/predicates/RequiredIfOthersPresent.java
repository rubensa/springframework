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

import java.util.Iterator;
import java.util.Set;

import org.springframework.rules.LogicalOperator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class RequiredIfOthersPresent extends RequiredIfTrue {

    /**
     * Tests that the property is required if all "other properties" are
     * present. Present means they are "non null."
     * 
     * @param otherPropertyNames
     *            one or more other properties, delimited by commas.
     */
    public RequiredIfOthersPresent(String otherPropertyNames) {
        this(otherPropertyNames, LogicalOperator.AND);
    }

    /**
     * Tests that the property is required if all or any of the "other
     * properties" are present.
     * 
     * @param otherPropertyNames
     *            one or more other properties, delimited by commas.
     * @param operator
     *            the logical operator, either AND or OR.
     */
    public RequiredIfOthersPresent(String otherPropertyNames,
            LogicalOperator operator) {
        super();
        Assert.notNull(otherPropertyNames);
        Assert.notNull(operator);
        Set set = StringUtils.commaDelimitedListToSet(otherPropertyNames);
        Assert.hasElements(set);
        CompoundUnaryPredicate compoundPredicate;
        if (operator == LogicalOperator.AND) {
            compoundPredicate = new UnaryAnd();
        } else {
            compoundPredicate = new UnaryOr();
        }
        for (Iterator i = set.iterator(); i.hasNext();) {
            compoundPredicate.add(new PropertyPresent((String)i.next()));
        }
    }

}