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
import org.jdom.filter.AbstractFilter;
import org.jdom.filter.ElementFilter;
import org.springframework.util.Assert;

/**
 * Abstract implementation of the <code>Decorator</code> interface which 
 * simply iterates each <code>Element</code> in each <code>Document</code>
 * and calls the protected <code>decorateElement</code> method that
 * subclasses will implement.
 * <p>
 * The <code>Element</code>s selected for iteration can be modified from 
 * the default (all Elements in the DOM) by specifying a <code>Filter</code>
 * based on <code>Element</code> names.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public abstract class SimpleDecorator implements Decorator {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    private AbstractFilter elementFilter = new ElementFilter();
    
    /**
     * Permits decoration of the document by iterating all the filtered
     * child elements in the Documents supplied and calls a decorateElement() for 
     * each one.  decorateElement() must be implemented by subclasses.
     * <p>
     * By default, all Elements in the DOM are iterated.  To filter the list to
     * specific named Elements, use setFilterNames() or setFilter()
     * 
     * @see org.springframework.beandoc.output.Decorator#decorate
     * @see #setFilterNames
     * @see #setFilter
     */
    public void decorate(Document[] contextDocuments) {
        for (int i = 0; i < contextDocuments.length; i++)
            for (Iterator iter = contextDocuments[i].getDescendants(elementFilter); iter.hasNext();) {
                Element element = (Element) iter.next();
                decorateElement(element);
            }
        
    }
    
    /**
     * Each <code>Element</code> in each input <code>Document</code> is iteratively
     * passed to this method allowing subclasses to add or amend any attributes they
     * desire.  It's possible to filter the view of the DOM, and thereby the list of
     * <code>Element</code>s that will be passed to this method by setting a 
     * <code>Filter</code> on this instance.
     * 
     * @param element
     * @see #setFilterNames
     * @see #setFilter
     */
    protected abstract void decorateElement(Element element);
    
    /**
     * Directly specify a Filter to limit the Elements from the DOM that this instance
     * will iterate over when permitting decoration.
     * 
     * @param elementFilter
     */
    public void setFilter(ElementFilter elementFilter) {
        Assert.notNull(elementFilter, "Null elementFilter is not permitted");
        this.elementFilter = elementFilter;
    }
    
    /**
     * Specify an array of tag names that define Elements this instance will iterate
     * over.  If your subclass need only be concerned with <code>&lt;bean&gt;</code>
     * and <code>&lt;property&gt;</code> tags for example, you can set those two names
     * as elements of the array of names and only these <code>Element</code> types will
     * be passed into the <code>decorateElement</code> method. 
     * 
     * @param tagNames
     */
    public void setFilterNames(String[] tagNames) {
        Assert.notEmpty(tagNames, "Cannot have null or empty array of names for Element filter");
        
        this.elementFilter = new ElementFilter(tagNames[0]);
        if (tagNames.length > 1)
            for (int i = 1; i < tagNames.length; i++)
                elementFilter = (AbstractFilter) elementFilter.or(new ElementFilter(tagNames[i]));
             
        
    }

}
