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
package org.springframework.rules.functions;

import org.springframework.rules.BinaryFunction;
import org.springframework.util.comparators.NullSafeComparator;

/**
 * Returns the maximum of two <code>Comparable</code> objects
 * 
 * @author Keith Donald
 */
public class Maximum implements BinaryFunction {
    private static final Maximum INSTANCE = new Maximum();

    public Maximum() {
        super();
    }

    /**
     * Return the maximum of the two Comparable objects.
     * 
     * @param comparable1
     *            the first comparable
     * @param comparable2
     *            the second comparable
     * @return the maximum
     */
    public Object evaluate(Object comparable1, Object comparable2) {
        int result = NullSafeComparator.instance().compare(comparable1,
                comparable2);
        if (result > 0) {
            return comparable1;
        } else if (result < 0) {
            return comparable2;
        }
        return comparable1;
    }

    /**
     * Returns the shared instance--this is possible as the default functor for
     * this class is immutable and stateless.
     * 
     * @return the shared instance
     */
    public static final Maximum instance() {
        return INSTANCE;
    }

    public String toString() {
        return "max(arg1, arg2)";
    }

}