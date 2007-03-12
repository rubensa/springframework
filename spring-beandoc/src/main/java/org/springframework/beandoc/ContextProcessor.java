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

package org.springframework.beandoc;

import java.io.IOException;

/**
 * ContextProcessor handles the workflow for the beandoc tool.  It is responsible for managing
 * configuration and transformation of the input files, usually by delegating to a
 * list of configured Transformer implementations to generate all of the required
 * output.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface ContextProcessor {
    
    /**
     * Responsible for taking configured inputs and generating all required
     * beandoc output in whatever format has been requested.  Implementations
     * could read and write file system resources, network streams or handle
     * IO in a completely custom manner.
     * 
     * @throws IOException if the inputs cannot be read, or if the output stream
     *      fails for any reason.
     * @throws BeanDocException when any other processing failure occurs.
     */
    public void process() throws IOException, BeanDocException;

}
