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

import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.springframework.beandoc.output.Transformer;
import org.w3c.dom.Node;

/**
 * Transformer that simply echoes the XML representation of the decorated DOM.  Useful for
 * testing or debugging.
 * 
 * @author Darren Davison
 */
public class EchoTransformer implements Transformer {

    /**
     * @see org.springframework.beandoc.output.Transformer#transform(org.jdom.Document[], File)
     */
    public void transform(Document[] contextDocuments, File outputDir) {
        for (int i = 0; i < contextDocuments.length; i++) {
            Document doc = contextDocuments[i];
            try {
                new XMLOutputter().output(doc, new OutputStreamWriter(System.out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
