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
 * DocumentCompiler implementations do the work of plugging the various output documents
 * from the transformation stage together.  In some cases, no compilation stage
 * may be necessary.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public interface DocumentCompiler {
    
    /**
     * Perform any required documentation compilation steps.
     */
    public void compile(Document[] contextDocuments, File outputDir);
}
