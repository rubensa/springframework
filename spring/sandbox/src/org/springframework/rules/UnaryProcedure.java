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
package org.springframework.rules;

/**
 * A procedure (action which returns no value) that takes a single argument.
 * 
 * @author Keith Donald
 */
public interface UnaryProcedure {

    /**
     * Run this procedure with the specified input argument.
     * 
     * @param argument1
     *            the input
     */
    public void run(Object argument);
}