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
package org.springframework.functor;

/**
 * Type-safe enums for various logical operators.
 * 
 * @author Keith Donald
 */
public class LogicalOperator {
    
    /**
     * The <code>AND</code> operator
     */
    public static final LogicalOperator AND = new LogicalOperator("&&");
    
    /**
     * The <code>OR</code> operator
     */
    public static final LogicalOperator OR = new LogicalOperator("||");
    
    private String name;

    private LogicalOperator(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}