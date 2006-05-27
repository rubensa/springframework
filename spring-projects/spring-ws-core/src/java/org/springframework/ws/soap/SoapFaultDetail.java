/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * Represents the <code>detail</code> element in a SOAP fault. A detail contains <code>SoapFaultDetailElement</code>s,
 * which represent the individual details.
 *
 * @author Arjen Poutsma
 * @see SoapFaultDetailElement
 */
public interface SoapFaultDetail extends SoapElement {

    /**
     * Adds a new <code>SoapFaultDetailElement</code> with the specified qualified name to this detail.
     *
     * @param name the qualified name of the new detail element
     * @return the created <code>SoapFaultDetailElement</code>
     */
    SoapFaultDetailElement addFaultDetailElement(QName name);

    /**
     * Gets an iterator over all of the <code>SoapFaultDetailElement</code>s in this detail.
     *
     * @return an iterator over all the <code>SoapFaultDetailElement</code>s
     * @see SoapFaultDetailElement
     */
    Iterator getDetailEntries();

}
