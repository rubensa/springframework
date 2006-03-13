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

package org.springframework.ws.soap;

import org.springframework.ws.WebServiceMessage;

/**
 * Represents an abstraction for SOAP messages, providing access to a SOAP Envelope. The contents of the SOAP body can
 * be retrieved by <code>getPayloadSource()</code> and <code>getPayloadResult()</code> on
 * <code>WebServiceMessage</code>, the super-interface of this interface.
 *
 * @author Arjen Poutsma
 * @see #getPayloadSource()
 * @see #getPayloadResult()
 * @see #getEnvelope()
 */
public interface SoapMessage extends WebServiceMessage {

    /**
     * Returns the <code>SoapEnvelope</code> associated with this <code>SoapMessage</code>.
     */
    SoapEnvelope getEnvelope();

    /**
     * Get the SOAP Action for this messaage, or <code>null</code> if not present.
     *
     * @return the SOAP Action.
     */
    String getSoapAction();

    /**
     * Returns the <code>SoapBody</code> associated with this <code>SoapMessage</code>. This is a convenience method for
     * <code>getEnvelope().getBody()</code>.
     *
     * @see SoapEnvelope#getBody()
     */
    SoapBody getSoapBody();

    /**
     * Returns the <code>SoapHeader</code> associated with this <code>SoapMessage</code>. This is a convenience method
     * for <code>getEnvelope().getHeader()</code>.
     *
     * @see SoapEnvelope#getHeader()
     */
    SoapHeader getSoapHeader();

    /**
     * Returns the SOAP version of this message. This can be either SOAP 1.1 or SOAP 1.2.
     *
     * @return the SOAP version
     * @see SoapVersion#SOAP_11
     * @see SoapVersion#SOAP_12
     */
    SoapVersion getVersion();
}
