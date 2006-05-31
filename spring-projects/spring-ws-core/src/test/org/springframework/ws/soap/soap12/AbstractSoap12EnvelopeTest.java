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

package org.springframework.ws.soap.soap12;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.AbstractSoapEnvelopeTest;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

public abstract class AbstractSoap12EnvelopeTest extends AbstractSoapEnvelopeTest {

    public void testGetName() throws Exception {
        assertEquals(new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Envelope"), soapEnvelope.getName());
    }

    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(soapEnvelope.getSource(), result);
        assertXMLEqual("<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Header/>" + "<Body/></Envelope>",
                result.toString());
    }

}
