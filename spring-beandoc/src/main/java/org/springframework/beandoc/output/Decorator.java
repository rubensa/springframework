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

package org.springframework.beandoc.output;

import org.jdom.Document;


/**
 * Decorator implementations have the opportunity to modify the DOM trees built by a 
 * Processor instance.  They can optionally add attributes (but not elements) to any 
 * part of the DOM for subsequent use by one or more Transformer instances.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface Decorator {

    /**
     * Decorators act incrementally, this method allows the implementor to further modify
     * any previously modified DOM trees by previous <code>Decorator</code>s.  Note that any
     * attempt to add new nodes (Elements) to the DOM will result in a runtime error.  Only
     * addition and modification of attributes is permitted.
     * 
     * @param contextDocuments an array of Document objects representing the in-memory
     *      tree of the input files supplied to the beandoc <code>ContextProcessor</code>. 
     */
    public void decorate(Document[] contextDocuments);
    
}
