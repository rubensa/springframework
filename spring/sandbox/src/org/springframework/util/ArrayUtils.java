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
package org.springframework.util;

import java.lang.reflect.Array;

/**
 * Static utility functions to assist in working with arrays. Aim is to reduce
 * repeat code.
 * 
 * @author Keith Donald, adapted from jakarta-commons-lang's ArrayUtils
 */
public class ArrayUtils {

    /** Immutable empty object array */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private ArrayUtils() {
    }

    /**
     * Checks if an array of Objects has elements.
     * 
     * @param array
     *            the array to test
     * @return <code>true</code> if the array has elements, false is the
     *         array is empty or <code>null</code>
     */
    public static boolean hasElements(final Object[] array) {
        if (array == null || array.length == 0) {
            return false;
        }
        return true;
    }

    /**
     * Convert an array into string form.
     * 
     * This implementation delegates to ToStringBuilder using the default
     * SpringToStringStyler.
     * 
     * @param array
     *            The array to convert.
     * @return The array in string form.
     */
    public static String toString(final Object[] array) {
        return new ToStringBuilder(array).toString();
    }

    /**
     * Convert a primitive array to an object array of wrapper objects.
     * 
     * @param primitiveArray
     *            The primitive array
     * @return The object array.
     * @throws IllegalArgumentException
     *             if the parameter is not a primitive array.
     */
    public static Object[] toObjectArrayFromPrimitive(Object primitiveArray) {
        // if null, return
        if (primitiveArray == null) {
            return EMPTY_OBJECT_ARRAY;
        }
        // if not an array or elements not primitive, illegal argument...
        Class clazz = primitiveArray.getClass();
        Assert.isTrue(
            clazz.isArray(),
            "The specified parameter is not an array.");
        Assert.isTrue(
            clazz.getComponentType().isPrimitive(),
            "The specified parameter is not a primitive array.");

        // get array length and create Object output array
        int length = Array.getLength(primitiveArray);
        Object[] newArray = new Object[length];
        // wrap and copy elements
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(primitiveArray, i);
        }
        return newArray;
    }

}
