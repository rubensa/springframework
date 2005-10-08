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

import java.io.*;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beandoc.BeanDocException;
import org.springframework.util.Assert;

/**
 * Transformer that simply echoes the XML representation of the decorated DOM.  Useful for
 * testing or debugging.  Not included in the default context file for the beandoc tool.
 * <p>
 * This transformer can be configured to write the XML to any <code>Writer</code> object
 * you like by setting the 'writer' bean property.  Defaults to <code>System.out</code>
 * (wrapped by an <code>OutputStreamWriter</code>).
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class EchoTransformer implements Transformer {
    
    private Writer writer = new OutputStreamWriter(System.out);
    
    private Format format = Format.getRawFormat();

    /**
     * Simply echoes a textual representation of the decorated JDOM documents to the configured
     * Writer (or System.out if no other was specified).
     * 
     * @see org.springframework.beandoc.output.Transformer#transform(org.jdom.Document[], File)
     */
    public void transform(Document[] contextDocuments, File outputDir) {
        for (int i = 0; i < contextDocuments.length; i++)
            try {
                new XMLOutputter(format).output(contextDocuments[i], writer);
            } catch (IOException e) {
                throw new BeanDocException(e);
            }        
    }

    /**
     * Set the Writer that you wish output to be directed to.  Defaults to an OutputStreamWriter
     * around System.out
     * 
     * @param writer which must not be null (throws IllegalArgumentException)
     */
    public void setWriter(Writer writer) {
        Assert.notNull(writer);
        this.writer = writer;
    }

    /**
     * The default processor removes all comments and whitespcace by default for efficiency.  If
     * you want the debug output to be a bit more readable, set this to true.  False by default
     * which echoes the raw format of the DOMified XML.
     * 
     * @param prettyPrint
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.format = prettyPrint ? Format.getPrettyFormat() : Format.getRawFormat();
    }

}
