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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.Filter;

/**
 * Abstract implementation of the <code>Decorator</code> interface which 
 * simply iterates each <code>Element</code> in each <code>Document</code>
 * and calls the protected <code>decorateElement</code> method that
 * subclasses will implement.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public abstract class SimpleDecorator implements Decorator {
    
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Permits decoration of the document by iterating all the 
     * child elements in the Documents supplied and calls a decorateElement() for 
     * each one.  decorateElement() must be implemented by subclasses.
     * 
     * @see org.springframework.beandoc.output.Decorator#decorate
     */
    public void decorate(Document[] contextDocuments) {
        for (int i = 0; i < contextDocuments.length; i++) {
            Filter filter = new ContentFilter(ContentFilter.ELEMENT);
            for (Iterator iter = contextDocuments[i].getDescendants(filter); iter.hasNext();) {
                Element element = (Element) iter.next();
                decorateElement(element);
            }
        }
    }
    
    /**
     * Each <code>Element</code> in each input <code>Document</code> is iteratively
     * passed to this method allowing subclasses to add or amend any attributes they
     * desire.
     * 
     * @param element
     */
    protected abstract void decorateElement(Element element);

}
