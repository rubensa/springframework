/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision$ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor.predicates;

import java.util.Comparator;

import org.springframework.functor.BinaryPredicate;

/**
 * @author Keith Donald
 */
public class GreaterThanEqualTo extends ComparingBinaryPredicate implements
        BinaryPredicate {

    public GreaterThanEqualTo() {
        super();
    }

    public GreaterThanEqualTo(Comparator comparator) {
        super(comparator);
    }

    public boolean evaluateCompareResult(int result) {
        return result >= 0;
    }

    public static BinaryPredicate instance() {
        return INSTANCE;
    }

    private static final GreaterThanEqualTo INSTANCE = new GreaterThanEqualTo();

}