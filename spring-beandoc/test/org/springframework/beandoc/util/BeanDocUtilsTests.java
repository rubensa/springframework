/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.beandoc.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocUtilsTests extends TestCase {    

    Map m;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m = new HashMap();
        m.put("key1", new Object());
        m.put("key2", new Object());
        m.put("prefix.pkey1", new Object());
        m.put("prefix.pkey2", new Object());
        m.put("key3", new Object());
    }
    
    public void testFilterMapByPrefixAndStripPrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "prefix.");
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("pkey1"));
        assertTrue(filtered.containsKey("pkey2"));
        assertFalse(filtered.containsKey("key1"));
    }
    
    public void testFilterMapByPrefixAndLeavePrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "prefix.", false);
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("prefix.pkey1"));
        assertTrue(filtered.containsKey("prefix.pkey2"));
        assertFalse(filtered.containsKey("pkey1"));
        assertFalse(filtered.containsKey("key1"));
    }
    
    public void testFilterMapByPrefixWithEmptyPrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "");
        assertEquals(m, filtered);
    }
    
    public void testNonStringKeyInMap() {
        m.put(new Object(), new Object());
        try {
            Map filtered = BeanDocUtils.filterByPrefix(m, "prefix.", true);
        } catch (Exception e) {
            fail(); // s/be swallowed
        }
    }

}
