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
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

/**
 * Transformer that simply echoes the XML representation of the decorated DOM.  Useful for
 * testing or debugging.
 * 
 * @author Darren Davison
 */
public class EchoTransformer implements Transformer {

	/* (non-Javadoc)
	 * @see org.springframework.beandoc.transform.Transformer#document(org.w3c.dom.Node, java.io.File)
	 */
	public void document(Node node, File outputFile) throws Exception {
		Document doc = new DOMBuilder().build((org.w3c.dom.Document) node);
		try {
			new XMLOutputter().output(doc, new FileWriter(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.beandoc.transform.Transformer#dotFile(org.w3c.dom.Node, java.io.File)
	 */
	public void dotFile(Node node, File outputFile) throws Exception {
        Document doc = new DOMBuilder().build((org.w3c.dom.Document) node);
        try {
            new XMLOutputter().output(doc, new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
