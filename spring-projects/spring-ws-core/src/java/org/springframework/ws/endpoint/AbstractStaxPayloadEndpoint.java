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

package org.springframework.ws.endpoint;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for endpoints use StAX. Provides an <code>XMLInputFactory</code> and an
 * <code>XMLOutputFactory</code>.
 *
 * @author Arjen Poutsma
 * @see XMLInputFactory
 * @see XMLOutputFactory
 */
public abstract class AbstractStaxPayloadEndpoint {

    protected final Log logger = LogFactory.getLog(getClass());

    private XMLInputFactory inputFactory;

    private XMLOutputFactory outputFactory;

    /**
     * Returns an <code>XMLInputFactory</code> to read XML from.
     */
    protected XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = createXmlInputFactory();
        }
        return inputFactory;
    }

    /**
     * Returns an <code>XMLOutputFactory</code> to write XML to.
     */
    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = createXmlOutputFactory();
        }
        return outputFactory;
    }

    /**
     * Create a <code>XMLInputFactory</code> that this endpoint will use to create <code>XMLStreamReader</code>s or
     * <code>XMLEventReader</code>. Can be overridden in subclasses, adding further initialization of the factory. The
     * resulting <code>XMLInputFactory</code> is cached, so this method will only be called once.
     *
     * @return the created <code>XMLInputFactory</code>
     */
    protected XMLInputFactory createXmlInputFactory() {
        return XMLInputFactory.newInstance();
    }

    /**
     * Create a <code>XMLOutputFactory</code> that this endpoint will use to create <code>XMLStreamWriters</code>s or
     * <code>XMLEventWriters</code>. Can be overridden in subclasses, adding further initialization of the factory. The
     * resulting <code>XMLOutputFactory</code> is cached, so this method will only be called once.
     *
     * @return the created <code>XMLOutputFactory</code>
     */
    protected XMLOutputFactory createXmlOutputFactory() {
        return XMLOutputFactory.newInstance();
    }
}
