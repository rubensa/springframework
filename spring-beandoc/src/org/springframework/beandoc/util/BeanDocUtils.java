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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;



/**
 * Collection of static utility methods used by BeanDoc.
 * 
 * @author Darren Davison
 * @author Michael Schuerig, <michael@schuerig.de>
 * @since 1.0
 */
public class BeanDocUtils {
    
    private static final Log logger = LogFactory.getLog(BeanDocUtils.class);
    
    private BeanDocUtils() {
        // no instances required
    }

    /**
     * Returns a new <code>Map</code> containing only those entries
     * from <code>map</code> whose key starts with <code>prefix</code>.
     * Prefixes are removed from the keys in the returned <code>Map</code>.
     * 
     * @param map the original <code>Map</code> to be filtered; must not be <code>null</code>
     * @param prefix prefix by which entries are filtered; may be empty of <code>null</code>
     * @return a new <code>Map</code> with filtered entries
     * @see #filterByPrefix(Map<String,?>, String, boolean)
     */
    public static Map filterByPrefix(Map map, String prefix) {
        return filterByPrefix(map, prefix, true);        
    }
        
    /**
     * Returns a new <code>Map</code> containing only those entries
     * from <code>map</code> whose key starts with <code>prefix</code>.
     *
     * @param map the original <code>Map</code> to be filtered; must not be <code>null</code>
     * @param prefix prefix by which entries are filtered; may be empty of <code>null</code>
     * @param removePrefix should prefixes be removed from entries in the returned <code>Map</code>?
     * @return a new <code>Map</code> with filtered entries
     */
    public static Map filterByPrefix(
            final Map map,
            final String prefix, 
            final boolean removePrefix) {
        
        final HashMap filteredMap = new HashMap();    

        if (!StringUtils.hasText(prefix)) {
            filteredMap.putAll(map);
            return filteredMap;
        }

        for ( Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            try {
                Map.Entry entry = (Entry)it.next();
                String name = (String)entry.getKey();
                if ( name.startsWith(prefix) ) {
                    filteredMap.put(
                            removePrefix ? name.substring(prefix.length()) : name,
                            entry.getValue());
                }
            } catch (Exception e) {
                logger.warn("Unable to filter Map.Entry; [" + e.getMessage() + "]");
            }
        }
        
        return filteredMap;
    }

}
