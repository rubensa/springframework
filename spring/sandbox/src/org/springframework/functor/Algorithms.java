/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

import java.util.Collection;
import java.util.Iterator;

public class Algorithms {
    public static Object findFirst(Collection collection, UnaryPredicate predicate) {
        for (Iterator i = collection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (predicate.evaluate(o)) {
                return o;
            }
        }
        return null;
    }
}
