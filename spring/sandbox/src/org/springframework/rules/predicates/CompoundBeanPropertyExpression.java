/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.rules.predicates;

import java.util.Iterator;

import org.springframework.rules.BeanPropertyExpression;
import org.springframework.rules.UnaryPredicate;

/**
 * Abstract base class for unary predicates which compose other predicates.
 * 
 * @author Keith Donald
 */
public class CompoundBeanPropertyExpression implements BeanPropertyExpression {
    private CompoundUnaryPredicate compoundPredicate;

    public String getPropertyName() {
        BeanPropertyExpression e =
            (BeanPropertyExpression)compoundPredicate.iterator().next();
        return e.getPropertyName();
    }

    public UnaryPredicate getPredicate() {
        return compoundPredicate;
    }
    
    /**
     * Constructs a compound predicate with no initial members. It is expected
     * the client will call "add" to add individual predicates.
     */
    public CompoundBeanPropertyExpression(CompoundUnaryPredicate compoundPredicate) {
        this.compoundPredicate = compoundPredicate;
        this.compoundPredicate.validateTypeSafety(BeanPropertyExpression.class);
    }

    /**
     * Add the specified predicate to the set of predicates aggregated by this
     * compound predicate.
     * 
     * @param predicate
     *            the predicate to add
     * @return A reference to this, to support chaining.
     */
    public CompoundBeanPropertyExpression add(BeanPropertyExpression expression) {
        this.compoundPredicate.add(expression);
        return this;
    }

    /**
     * Return an iterator over the aggregated predicates.
     * 
     * @return An iterator
     */
    public Iterator iterator() {
        return compoundPredicate.iterator();
    }

    public boolean test(Object bean) {
        return compoundPredicate.test(bean);
    }
    
    public String toString() {
        return compoundPredicate.toString();
    }

}