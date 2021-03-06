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

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Abstract implementation of the <code>SoapMessage</code> interface.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractSoapMessage implements SoapMessage {

    /**
     * Returns <code>getEnvelope().getBody()</code>.
     */
    public SoapBody getSoapBody() {
        return getEnvelope().getBody();
    }

    /**
     * Returns <code>getEnvelope().getHeader()</code>.
     */
    public SoapHeader getSoapHeader() {
        return getEnvelope().getHeader();
    }

    /**
     * Returns <code>getSoapBody().getPayloadSource()</code>.
     */
    public Source getPayloadSource() {
        return getSoapBody().getPayloadSource();
    }

    /**
     * Returns <code>getSoapBody().getPayloadResult()</code>.
     */
    public Result getPayloadResult() {
        return getSoapBody().getPayloadResult();
    }
}
