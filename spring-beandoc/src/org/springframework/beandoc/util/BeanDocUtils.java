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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;



/**
 * Collection of static utility methods.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocUtils {

    /**
     * Convert a collection of String objects to an array of compiled
     * regex Patterns.  Gaps in the collection or objects that aren't 
     * Strings will simply be ignored.
     * 
     * @param strings a List of Strings
     * @return an array of compiled Patterns
     */
    public static Pattern[] convertStringsToPatterns(Collection strings) {
        List tmp = new LinkedList();
        for (Iterator i = strings.iterator(); i.hasNext();)
            try {
                String enteredPattern = (String) i.next();
                Pattern compiledPattern = Pattern.compile(enteredPattern);
                tmp.add(compiledPattern);
                
            } catch (Exception e) {
                // ignore it
            }
            
        return
            (Pattern[]) tmp.toArray(new Pattern[tmp.size()]);        
    }
}
