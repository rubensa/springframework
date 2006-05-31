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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

public abstract class AbstractSoapEnvelopeTest extends XMLTestCase {

    protected SoapEnvelope soapEnvelope;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        soapEnvelope = createSoapEnvelope();
    }

    protected abstract SoapEnvelope createSoapEnvelope() throws Exception;

    public void testGetHeader() throws Exception {
        SoapHeader header = soapEnvelope.getHeader();
        assertNotNull("No header returned", header);
    }

    public void testGetBody() throws Exception {
        SoapBody body = soapEnvelope.getBody();
        assertNotNull("No body returned", body);
    }
}
