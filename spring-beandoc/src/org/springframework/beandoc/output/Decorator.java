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
 * Decorator implementations have the opportunity to modufy the DOM trees built by a 
 * Processor instance.  They can optionally add elements and attributes to elements
 * that may be required by one or more Transformer instances.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface Decorator {

    public void decorate(Document[] contextDocuments);
    
}
