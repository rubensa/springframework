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

package org.springframework.beandoc.transform;

import java.io.File;

import org.w3c.dom.Node;

/**
 * Transformer implementations do the work of generating output based on the 
 * decorated Node's passed to them.  A {@link BeanDocEngine} instance will use a 
 * <code>Transformer</code> to generate both the documentation (via this class' 
 * <code>document()</code> method) and, if graphing output is enabled, the .dot
 * files that GraphViz uses to actually create graphs.
 * <p>
 * Both methods in this interface accept a <code>Node</code> and a <code>File</code>
 * as parameters.  The <code>Node</code> is a decorated version of a <code>Document</code>
 * that was built from the parsed context files and the <code>File</code> represents a
 * filesystem directoy that should be writeable by the current user.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface Transformer {

    /**
     * Process the Configuration to output the bean documentation.
     * 
     * @param node a W3C Node object that is used to write documentation (BeanDoc) files
     * @param outputDirectory the location to write the output to, which may consist of 
     * 		multiple files
     * @throws Exception any exception will be wrapped by the BeanDocEngine and rethrown
     *      as a BeanDocException
     */
    public void document(Node node, File outputDirectory) throws Exception;
    
    /**
     * Process a Node to produce .dot files for GraphViz.  May implement a no-op
     * procedure if this transformer doesn't support graphing output.
     * 
     * @param node a W3C Node object that is used to write the .dot file
     * @param outputFile the location to write the dot file to
     * @throws Exception any exception will be wrapped by the BeanDocEngine and rethrown
     *      as a BeanDocException
     */
    public void dotFile(Node node, File outputFile) throws Exception;
    
}
