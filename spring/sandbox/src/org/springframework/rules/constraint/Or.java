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
package org.springframework.rules.constraint;

import java.util.Iterator;

import org.springframework.util.closure.Constraint;

/**
 * A "or" compound predicate (aka disjunction).
 * 
 * @author Keith Donald
 */
public class Or extends CompoundConstraint {

    /**
     * Creates a empty UnaryOr disjunction.
     */
    public Or() {
        super();
    }

    /**
     * "Ors" two predicates.
     * 
     * @param predicate1
     *            The first predicate.
     * @param predicate2
     *            The second predicate.
     */
    public Or(Constraint predicate1, Constraint predicate2) {
        super(predicate1, predicate2);
    }

    /**
     * "Ors" the specified predicates.
     * 
     * @param predicates
     *            The predicates
     */
    public Or(Constraint[] predicates) {
        super(predicates);
    }

    /**
     * Tests if any of the predicates aggregated by this compound predicate
     * test <code>true</code>.
     * 
     * @see org.springframework.util.closure.Constraint#test(java.lang.Object)
     */
    public boolean test(Object value) {
        for (Iterator i = iterator(); i.hasNext();) {
            if (((Constraint)i.next()).test(value))  {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("or(");
        for (Iterator i = iterator(); i.hasNext();) {
            buf.append(i.next());
            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }
    
}