/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cache with weak keys and soft values. This class is abstract template; cache
 * implementations should subclass and override the create(key) method which
 * encapsulates new expensive object creation.
 * 
 * @author Bob Lee
 * @see http://www.crazybob.org
 * @version $Id$
 */
public abstract class Cache {
    private static final Log logger = LogFactory.getLog(Cache.class);

    static Object NULL_VALUE = new Object();
    Map map;
    ReferenceQueue queue = new ReferenceQueue();

    /**
     * Creates cache; defaults to weak keys.
     */
    public Cache() {
        this(true);
    }
    
    /**
     * Creates cache.
     * 
     * @param weakKeys
     *            Use weak references for keys.
     */
    public Cache(boolean weakKeys) {
        this.map = weakKeys ? (Map)new WeakHashMap() : new HashMap();
        this.map = Collections.synchronizedMap(this.map);
    }

    /**
     * Creates cache with initial size.
     * 
     * @param weakKeys
     *            Use weak references for keys.
     * @param size
     *            The initial cache size.
     */
    public Cache(boolean weakKeys, int size) {
        this.map = weakKeys ? (Map)new WeakHashMap(size) : new HashMap(size);
        this.map = Collections.synchronizedMap(this.map);
    }

    /**
     * Creates value for key. Called by get() if value isn't cached.
     */
    protected abstract Object create(Object key);

    /**
     * Gets value for key. Creates and caches value if it doesn't already exist
     * in the cache.
     */
    public Object get(Object key) {
        Object value = internalGet(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Creating new expensive value with key '" + key + "'");
            }
            value = create(key);
            if (value == null) {
                value = NULL_VALUE;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caching value '" + value + "'");
            }
            put(key, value);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Found expensive value with key '" + key + "' in cache.");
            }
        }
        return (value == NULL_VALUE) ? null : value;
    }

    private void put(Object key, Object value) {
        this.map.put(key, new ValueReference(key, value));
    }

    /**
     * Returns the size of the cache.
     * 
     * @return The cache size.
     */
    public int size() {
        return map.size();
    }

    /**
     * Empties the cache, removing all entries.
     */
    public void clear() {
        map.clear();
    }
    
    public Iterator keys() {
        return map.keySet().iterator();
    }
    
    public Iterator values() {
        return new ValuesIterator(map.values().iterator());
    }
    
    /**
     * Delegates to the underlying values iterator, retrieving
     * the object stored at each value reference.
     */
    static class ValuesIterator implements Iterator {
        private Iterator it;
        
        public ValuesIterator(Iterator it) {
            this.it = it;
        }
        
        public boolean hasNext() {
            return it.hasNext();
        }

        public Object next() {
            return ((ValueReference)it.next()).get();
        }

        public void remove() {
            it.remove();
        }
    }
    
    public Iterator entries() {
        return map.entrySet().iterator();
    }

    private Object internalGet(Object key) {
        cleanUp();
        Reference reference = (Reference)map.get(key);
        return (reference == null) ? null : reference.get();
    }

    private void cleanUp() {
        Reference reference;
        while ((reference = this.queue.poll()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Removing claimed soft reference '" + reference + "'");
            }
            map.remove(((ValueReference)reference).getKey());
        }
    }

    class ValueReference extends SoftReference {
        WeakReference keyReference;

        ValueReference(Object key, Object value) {
            super(value, queue);
            this.keyReference = new WeakReference(key);
        }

        Object getKey() {
            return this.keyReference.get();
        }

        public String toString() {
            return String.valueOf(super.get());
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("weakKeys", (map instanceof WeakHashMap))
            .append("size", map.size())
            .append("contents", map)
            .toString();
    }
}