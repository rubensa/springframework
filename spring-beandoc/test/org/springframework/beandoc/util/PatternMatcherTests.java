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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class PatternMatcherTests extends TestCase {
    
    public void testNullOrEmptyCollectionOfPatterns() {
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(null);
        assertEquals(0, patterns.length);

        patterns = PatternMatcher.convertStringsToPatterns(new ArrayList());
        assertEquals(0, patterns.length);
    }

    public void testConvertValidStringsToPatterns() {
        List strings = new ArrayList(3);
        strings.add("^hello$");
        strings.add(".*world");
        strings.add(".*\\.[AaBbCc]Map");
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(3, patterns.length);
        assertEquals("^hello$", patterns[0].pattern());        
    }
    
    public void testConvertMixedObjectsToPatterns() {
        List strings = new ArrayList(3);
        strings.add("^hello$");
        strings.add("[[.*world"); // oops - not valid
        strings.add(".*\\.[AaBbCc]Map");
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(2, patterns.length);
        assertEquals(".*\\.[AaBbCc]Map", patterns[1].pattern());            
    }
    
    public void testConvertInvalidObjectsToPatterns() {
        List strings = new ArrayList(1);
        strings.add(new Date()); // oops - not a string
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(0, patterns.length);  
    }
    
    public void testConvertNullsToPatterns() {
        List strings = new ArrayList(1);
        strings.add(null); // oops - null
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(0, patterns.length);  
    }
    
    public void testPatternMatchCallback() {
        List strings = new ArrayList(3);
        strings.add("^myTestBean$");
        strings.add("com.foo.bar.TestBean");
        strings.add(".*Controller");
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(3, patterns.length);
        
        MatchedPatternCallback testMyBeanMatches = new MatchedPatternCallback() {
            public void patternMatched(String pattern, int index) {
                assertEquals("^myTestBean$", pattern);
                assertEquals(0, index);
            }            
        };
        PatternMatcher.matchPatterns(patterns, new String[] {"myTestBean", "any.old.rubbish.Bean"}, testMyBeanMatches);
        
        
                
        MatchedPatternCallback testNoMatches = new MatchedPatternCallback() {
            public void patternMatched(String pattern, int index) {
                fail("Got a match for pattern [" + pattern + "] - shouldn't have!");
            }            
        };
        PatternMatcher.matchPatterns(patterns, new String[] {"noMatchingBean", "any.old.rubbish.Bean"}, testNoMatches);
        
        
        MatchedPatternCallback testClassMatches = new MatchedPatternCallback() {
            public void patternMatched(String pattern, int index) {
                assertEquals("com.foo.bar.TestBean", pattern);
                assertEquals(1, index);
            }            
        };
        PatternMatcher.matchPatterns(patterns, new String[] {"noMatchingBeanName", "com.foo.bar.TestBean"}, testClassMatches);
    }
    
    public void testPatternMatcherSwallowsNPE() {
        List strings = new ArrayList(1);
        strings.add("^myTestBean$");
        Pattern[] patterns = PatternMatcher.convertStringsToPatterns(strings);
        assertEquals(1, patterns.length);
        
        try {
            MatchedPatternCallback testMyBeanMatches = new MatchedPatternCallback() {        
                public void patternMatched(String pattern, int index) {
                    fail();
                }            
            };
            PatternMatcher.matchPatterns(patterns, new String[] {null, "any.old.rubbish.Bean"}, testMyBeanMatches);
        } catch (Exception e) {
            fail();        
        }
    }
}
