/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.predicates;

import java.util.Comparator;

import org.springframework.functor.BinaryPredicate;

public abstract class OperatorBinaryPredicate implements BinaryPredicate {
    private Comparator comparator;
    
    public OperatorBinaryPredicate() {
        
    }
    
    public OperatorBinaryPredicate(Comparator comparator) {
        this.comparator = comparator;
    }
    
    public boolean evaluate(Object value1, Object value2) {
        if (comparator != null) {
            return evaluateOperatorResult(this.comparator.compare(value1, value2));
        } else {
            Comparable c1 = (Comparable)value1;
            Comparable c2 = (Comparable)value2;
            return evaluateOperatorResult(c1.compareTo(c2));
        }
    }
    
    public abstract boolean evaluateOperatorResult(int result);

}
