/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import java.io.ByteArrayOutputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class SaajSoapMessageTest extends XMLTestCase {

    private SaajSoapMessage message;

    private SOAPMessage saajMessage;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        saajMessage = messageFactory.createMessage();
        this.message = new SaajSoapMessage(saajMessage);
    }

    public void testGetPayloadSource() throws Exception {
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = message.getPayloadSource();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadSourceText() throws Exception {
        saajMessage.getSOAPBody().addTextNode(" ");
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = message.getPayloadSource();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadResult() throws Exception {
        StringSource source = new StringSource("<child/>");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Result result = message.getPayloadResult();
        transformer.transform(source, result);
        assertTrue("No child nodes created", saajMessage.getSOAPBody().hasChildNodes());
        assertEquals("Invalid child node created", "child", saajMessage.getSOAPBody().getFirstChild().getLocalName());
    }

    public void testGetSoapAction() throws Exception {
        saajMessage.getMimeHeaders().addHeader("SOAPAction", "value");
        assertEquals("Invalid mime header value", "value", message.getSoapAction());
    }

    public void testWriteTo() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
                new String(outputStream.toByteArray(), "UTF-8"));
    }

    public void testGetVersion() throws Exception {
        assertEquals("Invalid SOAP version", SoapVersion.SOAP_11, message.getVersion());
    }
}