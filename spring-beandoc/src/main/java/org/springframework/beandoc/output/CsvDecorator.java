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

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * From the example in the Reference Docs.
 * <p>
 * <b>This class is instructive only and is not designed for normal use</b>
 * <p>
 * See the <a href="http://springframework.sourceforge.net/beandoc/refdoc/extending.html#d0e485">
 * relevent section of the reference documentation</a>
 * for more information.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class CsvDecorator extends SimpleDecorator {

    private static final String ATTRIBUTE_PREFIX = "beandocCSV";

    static final String ATTRIBUTE_COUNTER = ATTRIBUTE_PREFIX + "Count";

    private int count = 0;

    /**
     * specify only 'bean' elements as a Filter - we're not interested in
     * decorating anything else
     */
    public CsvDecorator() {
        setFilter(new ElementFilter("bean"));
    }
    
    /**
     * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement(org.jdom.Element)
     */
    protected void decorateElement(Element element) {
        element.setAttribute(ATTRIBUTE_COUNTER, String.valueOf(++count));      
    }
}
