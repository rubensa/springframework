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

package org.springframework.ws.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.EndpointAdapter;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Adapter to use a <code>PayloadEndpoint</code> as the endpoint for a <code>EndpointInvocationChain</code>.
 *
 * @author Arjen Poutsma
 * @see PayloadEndpoint
 * @see org.springframework.ws.EndpointInvocationChain
 */
public class PayloadEndpointAdapter implements EndpointAdapter, InitializingBean {

    private TransformerFactory transformerFactory;

    public boolean supports(Object endpoint) {
        return (endpoint instanceof PayloadEndpoint);
    }

    public void invoke(MessageContext messageContext, Object endpoint) throws Exception {
        PayloadEndpoint payloadEndpoint = (PayloadEndpoint) endpoint;
        Source requestSource = messageContext.getRequest().getPayloadSource();
        Source responseSource = payloadEndpoint.invoke(requestSource);
        if (responseSource != null) {
            WebServiceMessage response = messageContext.createResponse();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(responseSource, response.getPayloadResult());
        }
    }

    public final void afterPropertiesSet() throws Exception {
        transformerFactory = TransformerFactory.newInstance();
    }
}
