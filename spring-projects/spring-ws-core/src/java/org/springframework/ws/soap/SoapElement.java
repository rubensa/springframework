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

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

/**
 * The base interface for all elements that are contained in a SOAP message.
 *
 * @author Arjen Poutsma
 * @see SoapMessage
 */
public interface SoapElement {

    /**
     * Returns the qualified name of this element.
     *
     * @return the qualified name of this element
     */
    QName getName();

    /**
     * Returns the <code>Source</code> of this element. This includes the element itself, i.e.
     * <code>SoapEnvelope.getSource()</code> will include the <code>Envelope</code> tag.
     *
     * @return the <code>Source</code> of this element
     */
    Source getSource();

}
