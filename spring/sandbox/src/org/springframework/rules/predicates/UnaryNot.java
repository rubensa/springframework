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

import org.springframework.rules.UnaryPredicate;

/**
 * "Nots" another unary predicate (the inverse) by using composition.
 * 
 * @author Keith Donald
 */
public class UnaryNot implements UnaryPredicate {
    private UnaryPredicate predicate;

    /**
     * Creates a UnaryNot in temporary invalid state - please use only
     * if you have to, the well-formed constructor is much preferred.
     */
    public UnaryNot() {
        
    }
    
    /**
     * Creates a UnaryNot
     * 
     * @param predicate
     *            The predicate to negate.
     */
    public UnaryNot(UnaryPredicate predicate) {
        this.predicate = predicate;
    }
    
    public UnaryPredicate getPredicate() {
        return predicate;
    }

    public void setPredicate(UnaryPredicate predicate) {
        this.predicate = predicate;
    }
    
    /**
     * Negates the boolean result returned by testing the wrapped predicate.
     * 
     * @see org.springframework.rules.UnaryPredicate#test(java.lang.Object)
     */
    public boolean test(Object value) {
        return !predicate.test(value);
    }
    
    public String toString() {
        return "not(" + getPredicate() + ")";
    }

}