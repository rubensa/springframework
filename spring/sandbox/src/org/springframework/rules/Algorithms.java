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
package org.springframework.rules;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.factory.Functions;

/**
 * Convenience utility class which provides a number of algorithms involving
 * functor objects such as predicates.
 * 
 * @author Keith Donald
 * @version $Id$
 */
public class Algorithms {
    private Functions functions = Functions.instance();
    private static final Algorithms INSTANCE = new Algorithms();

    public Algorithms() {
    }

    public static Algorithms instance() {
        return INSTANCE;
    }

    /**
     * Find the first element in the collection matching the specified unary
     * predicate.
     * 
     * @param collection
     *            the collection
     * @param predicate
     *            the predicate
     * @return The first object match, or null if no match
     */
    public Object findFirst(Collection collection, UnaryPredicate predicate) {
        for (Iterator i = collection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (predicate.test(o)) {
                return o;
            }
        }
        return null;
    }

    public void forEach(Collection collection, UnaryProcedure callback) {
        forEach(collection.iterator(), callback);
    }

    public void forEach(Iterator it, UnaryProcedure callback) {
        while (it.hasNext()) {
            callback.run(it.next());
        }
    }

    public Generator select(final Generator generator,
            final UnaryPredicate predicate) {
        return new Generator() {
            public void run(UnaryProcedure procedure) {
                UnaryProcedure constrainedProcedure = functions.constrain(
                        procedure, predicate);
                generator.run(constrainedProcedure);
            }
        };
    }

}