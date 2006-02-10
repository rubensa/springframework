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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.context.MessageContext;
import org.w3c.dom.Element;

/**
 * Default implementation of the <code>EndpointInterceptor</code> interface, for simplified implementation of
 * pre-only/post-only interceptors.
 *
 * @author Arjen Poutsma
 */
public class EndpointInterceptorAdapter implements EndpointInterceptor {

    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Returns <code>false</code>.
     */
    public boolean understands(Element header) {
        return false;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) {
        return true;
    }
}
