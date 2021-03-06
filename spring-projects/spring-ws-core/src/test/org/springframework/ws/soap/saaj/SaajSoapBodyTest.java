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

package org.springframework.ws.soap.saaj;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTestCase;

public class SaajSoapBodyTest extends AbstractSoap11BodyTestCase {

    protected SoapBody createSoapBody() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        return saajSoapMessage.getSoapBody();
    }

}
