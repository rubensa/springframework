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

/**
 * Callback interface to be implemented by clients of the <code>PatternMatcher</code>
 * class.  The single method in the interface is called by the matcher for each pattern in 
 * an array of <code>Pattern</code>s that matches a bean name/id or class.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface MatchedPatternCallback {
    
    /**
     * Called for each matching pattern in an array
     * 
     * @param pattern the compiled Pattern that matched against the name/id or class
     * @param index the index in the original array of Patterns that the pattern parameter
     *      represents.
     */
    public void patternMatched(String pattern, int index);
    
}
