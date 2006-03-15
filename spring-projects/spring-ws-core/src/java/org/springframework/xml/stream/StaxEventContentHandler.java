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

package org.springframework.xml.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * SAX <code>ContentHandler</code> that transforms callback calls to <code>XMLEvent</code>s and writes them to a
 * <code>XMLEventConsumer</code>.
 *
 * @author Arjen Poutsma
 * @see XMLEvent
 * @see XMLEventConsumer
 */
public class StaxEventContentHandler extends StaxContentHandler {

    private final XMLEventFactory eventFactory;

    private final XMLEventConsumer eventConsumer;

    private Locator locator;

    /**
     * Constructs a new instance of the <code>StaxEventContentHandler</code> that writes to the given
     * <code>XMLEventConsumer</code>. A default <code>XMLEventFactory</code> will be created.
     *
     * @param consumer the consumer to write events to
     */
    public StaxEventContentHandler(XMLEventConsumer consumer) {
        this.eventFactory = XMLEventFactory.newInstance();
        this.eventConsumer = consumer;
    }

    /**
     * Constructs a new instance of the <code>StaxEventContentHandler</code> that uses the given event factory to create
     * events and writes to the given <code>XMLEventConsumer</code>.
     *
     * @param consumer the consumer to write events to
     * @param factory  the factory used to create events
     */
    public StaxEventContentHandler(XMLEventConsumer consumer, XMLEventFactory factory) {
        this.eventFactory = factory;
        this.eventConsumer = consumer;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    protected void startDocumentInternal() throws XMLStreamException {
        consumeEvent(eventFactory.createStartDocument());
    }

    protected void endDocumentInternal() throws XMLStreamException {
        consumeEvent(eventFactory.createEndDocument());
    }

    protected void startElementInternal(QName name, Attributes atts, SimpleNamespaceContext namespaceContext)
            throws XMLStreamException {
        List attributes = getAttributes(atts);
        List namespaces = createNamespaces(namespaceContext);
        consumeEvent(eventFactory.createStartElement(name, attributes.iterator(), namespaces.iterator()));
    }

    protected void endElementInternal(QName name, SimpleNamespaceContext namespaceContext) throws XMLStreamException {
        List namespaces = createNamespaces(namespaceContext);
        consumeEvent(eventFactory.createEndElement(name, namespaces.iterator()));
    }

    protected void charactersInternal(char[] ch, int start, int length) throws XMLStreamException {
        consumeEvent(eventFactory.createCharacters(new String(ch, start, length)));
    }

    protected void ignorableWhitespaceInternal(char[] ch, int start, int length) throws XMLStreamException {
        consumeEvent(eventFactory.createIgnorableSpace(new String(ch, start, length)));
    }

    protected void processingInstructionInternal(String target, String data) throws XMLStreamException {
        consumeEvent(eventFactory.createProcessingInstruction(target, data));
    }

    private void consumeEvent(XMLEvent event) throws XMLStreamException {
        if (locator != null) {
            eventFactory.setLocation(new SaxLocation(locator));
        }
        eventConsumer.add(event);
    }

    /**
     * Creates and returns a list of <code>NameSpace</code> objects from the <code>NamespaceContext</code>.
     */
    private List createNamespaces(SimpleNamespaceContext namespaceContext) {
        List namespaces = new ArrayList();
        String defaultNamespaceUri = namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
        if (StringUtils.hasLength(defaultNamespaceUri)) {
            namespaces.add(eventFactory.createNamespace(defaultNamespaceUri));
        }
        for (Iterator iterator = namespaceContext.getBoundPrefixes(); iterator.hasNext();) {
            String prefix = (String) iterator.next();
            String namespaceUri = namespaceContext.getNamespaceURI(prefix);
            namespaces.add(eventFactory.createNamespace(prefix, namespaceUri));
        }
        return namespaces;
    }

    private List getAttributes(Attributes attributes) {
        List list = new ArrayList();
        for (int i = 0; i < attributes.getLength(); i++) {
            QName name = QNameUtils.toQName(attributes.getURI(i), attributes.getQName(i));
            if (!(XMLConstants.XMLNS_ATTRIBUTE.equals(name.getLocalPart()) ||
                    XMLConstants.XMLNS_ATTRIBUTE.equals(name.getPrefix()))) {
                list.add(eventFactory.createAttribute(name, attributes.getValue(i)));
            }
        }
        return list;
    }

    //
    // No operation
    //

    protected void skippedEntityInternal(String name) throws XMLStreamException {
    }

    private static class SaxLocation implements Location {

        private Locator locator;

        public SaxLocation(Locator locator) {
            this.locator = locator;
        }

        public int getLineNumber() {
            return locator.getLineNumber();
        }

        public int getColumnNumber() {
            return locator.getColumnNumber();
        }

        public int getCharacterOffset() {
            return -1;
        }

        public String getPublicId() {
            return locator.getPublicId();
        }

        public String getSystemId() {
            return locator.getSystemId();
        }
    }
}