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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class that matches bean names/id's or class names against an array of
 * regex <code>Pattern</code>s, using a callback interface for each pattern in the
 * array which matched.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public final class PatternMatcher {
    
    private static final Log logger = LogFactory.getLog(PatternMatcher.class);
    
    protected PatternMatcher() {
        // no instances required
    }
    
    /**
     * Convert a collection of String objects to an array of compiled
     * regex Patterns.  Gaps in the collection or objects that aren't 
     * Strings will simply be ignored.
     * 
     * @param strings a List of Strings
     * @return an array of compiled Patterns
     */
    public static Pattern[] convertStringsToPatterns(Collection strings) {
        if (strings == null || strings.size() == 0)
            return new Pattern[0];
        
        List tmp = new LinkedList();
        for (Iterator i = strings.iterator(); i.hasNext();) {
            String enteredPattern = null;
            try {
                enteredPattern = (String) i.next();
                if (enteredPattern != null) {
                    Pattern compiledPattern = Pattern.compile(enteredPattern);
                    tmp.add(compiledPattern);
                }
                
            } catch (ClassCastException cce) {
                logger.warn("Ignoring non String object in Collection [" + cce.getMessage() + "]");
                
            } catch (PatternSyntaxException pse) {
                logger.warn("Ignoring invalid RegEx pattern in String [" + pse.getPattern() + 
                    "]; problem description [" + pse.getMessage() + "]");
            }
        }
        
        return (Pattern[]) tmp.toArray(new Pattern[tmp.size()]);        
    }
    
    /**
     * Takes an array of Patterns, the id/name and f.q.class name of a bean and a callback interface.  For
     * each pattern in the array that matches the bean id/name or class, the callback is executed passing in
     * the String representation of the matching pattern and its index in the original array.
     * 
     * @param patterns an array of compiled <code>Pattern</code>s
     * @param testStrings the array of Strings to test against the patterns.  Usually consists of
     *      the id/name of the bean and its fully qualified classname
     * @param callback an implementation of MatchedPatternCallback
     */
    public static void matchPatterns(Pattern[] patterns, String[] testStrings, MatchedPatternCallback callback) {
        // patterns of beans to be ignored on graphs
        for (int i = 0; i < patterns.length; i++)
            for (int j = 0; j < testStrings.length; j++)
                if (testStrings[j] != null) {
                    Matcher matcher = patterns[i].matcher(testStrings[j]);                
                    try {
                        if (matcher.matches())             
                            callback.patternMatched(patterns[i].pattern(), i);
                        
                    } catch (NullPointerException npe) {                
                        /*
                         * JDK 1.5 seems to have changed the behaviour of Pattern.matches(arg)
                         * in that a null arg now throws a NPE where as JDK 1.4 didn't - it just
                         * resulted in no match.  This has been recorded as a bug with Sun;
                         * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6178785
                         */
                    }
                }        
    }
}
