/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Generic, immutable composite key class. Useful for storing a structure as a
 * key in a map. Backed by a Hashset.
 * 
 * @author Keith Donald
 */
public class CompositeKey {
    private Set keys;

    /**
     * Creates a CompositeKey from the set of individual keys.
     * 
     * @param keys
     *            The set of keys.
     */
    public CompositeKey(Set keys) {
        keys = new HashSet(keys);
    }

    /**
     * Creates a CompositeKey with two keys.
     * 
     * @param key1
     * @param key2
     */
    public CompositeKey(Object key1, Object key2) {
        keys = new HashSet(2);
        keys.add(key1);
        keys.add(key2);
    }

    /**
     * Creates a CompositeKey with three keys.
     * 
     * @param key1
     * @param key2
     * @param key3
     */
    public CompositeKey(Object key1, Object key2, Object key3) {
        keys = new HashSet(3);
        keys.add(key1);
        keys.add(key2);
        keys.add(key3);
    }

    public boolean equals(Object o) {
        if (!(o instanceof CompositeKey)) {
            return false;
        }
        CompositeKey other = (CompositeKey)o;
        return keys.equals(other.keys);
    }

    public int hashCode() {
        return keys.hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this).append("keys", keys).toString();
    }
}
