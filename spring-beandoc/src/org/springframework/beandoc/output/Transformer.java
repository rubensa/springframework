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

import java.io.File;

import org.jdom.Document;

/**
 * Transformer implementations do the work of generating output based on the 
 * decorated Documents passed to them.  A {@link org.springframework.beandoc.ContextProcessor}
 * instance will use a <code>Transformer</code> to generate output from the 
 * pre-processed DOM trees.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface Transformer {

    /**
     * Process the context document array to output the bean documentation in the required
     * format.
     * 
     * @param contextDocuments an array of JDOM Document objects
     * @param outputDir the directory to place any output in
     */
    public void transform(Document[] contextDocuments, File outputDir);
    
}
