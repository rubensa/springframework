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

package org.springframework.xml.transform;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.springframework.xml.stream.StaxEventXmlReader;
import org.springframework.xml.stream.StaxStreamXmlReader;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Implementation of the <code>Source</code> tagging interface for StAX readers. Can be constructed with a
 * <code>XMLEventReader</code> or a <code>XMLStreamReader</code>.
 * <p/>
 * Even though <code>StaxSource</code> extends from <code>SAXSource</code>, calling the methods of
 * <code>SAXSource</code> is <strong>not supported</strong>. In general, the only supported operation on this class is
 * to use the <code>XMLReader</code> obtained via {@link #getXMLReader()} to parse the input source obtained via {@link
 * #getInputSource()}. Calling {@link #setXMLReader(org.xml.sax.XMLReader)} or {@link
 * #setInputSource(org.xml.sax.InputSource)} will result in <code>UnsupportedOperationException</code>s.
 *
 * @author Arjen Poutsma
 * @see XMLEventReader
 * @see XMLStreamReader
 * @see javax.xml.transform.Transformer
 */
public class StaxSource extends SAXSource {

    private XMLEventReader eventReader;

    private XMLStreamReader streamReader;

    /**
     * Constructs a new instance of the <code>StaxSource</code> with the specified <code>XMLStreamReader</code>. The
     * supplied stream reader must be in <code>XMLStreamConstants.START_DOCUMENT</code> or
     * <code>XMLStreamConstants.START_ELEMENT</code> state.
     *
     * @param streamReader the <code>XMLStreamReader</code> to read from
     * @throws IllegalStateException if the reader is not at the start of a document or element
     */
    public StaxSource(XMLStreamReader streamReader) {
        super(new StaxStreamXmlReader(streamReader), new InputSource());
        this.streamReader = streamReader;
    }

    /**
     * Constructs a new instance of the <code>StaxSource</code> with the specified <code>XMLEventReader</code>. The
     * supplied event reader must be in <code>XMLStreamConstants.START_DOCUMENT</code> or
     * <code>XMLStreamConstants.START_ELEMENT</code> state.
     *
     * @param eventReader the <code>XMLEventReader</code> to read from
     * @throws IllegalStateException if the reader is not at the start of a document or element
     */
    public StaxSource(XMLEventReader eventReader) {
        super(new StaxEventXmlReader(eventReader), new InputSource());
        this.eventReader = eventReader;
    }

    /**
     * Returns the <code>XMLEventReader</code> used by this <code>StaxSource</code>. If this <code>StaxSource</code> was
     * created with an <code>XMLStreamReader</code>, the result will be <code>null</code>.
     *
     * @return the StAX event reader used by this source
     * @see StaxSource#StaxSource(javax.xml.stream.XMLEventReader)
     */
    public XMLEventReader getXMLEventReader() {
        return this.eventReader;
    }

    /**
     * Returns the <code>XMLStreamReader</code> used by this <code>StaxSource</code>. If this <code>StaxSource</code>
     * was created with an <code>XMLEventReader</code>, the result will be <code>null</code>.
     *
     * @return the StAX event reader used by this source
     * @see StaxSource#StaxSource(javax.xml.stream.XMLEventReader)
     */
    public XMLStreamReader getXMLStreamReader() {
        return this.streamReader;
    }

    /**
     * Throws a <code>UnsupportedOperationException</code>.
     *
     * @throws UnsupportedOperationException always
     */
    public void setInputSource(InputSource inputSource) {
        throw new UnsupportedOperationException("setInputSource is not supported");
    }

    /**
     * Throws a <code>UnsupportedOperationException</code>.
     *
     * @throws UnsupportedOperationException always
     */
    public void setXMLReader(XMLReader reader) {
        throw new UnsupportedOperationException("setXMLReader is not supported");
    }
}
