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

package org.springframework.ws.context;

import org.springframework.ws.WebServiceMessage;

/**
 * Context holder for message requests. Contains both the message request as well as the response.
 *
 * @author Arjen Poutsma
 */
public interface MessageContext {

    /**
     * Returns the request message.
     *
     * @return the request message
     */
    WebServiceMessage getRequest();

    /**
     * Creates a new response message. This method can be called only once, afterwards, the response can be retrieved
     * using <code>getResponse</code>.
     *
     * @return the created response message
     * @throws IllegalStateException if a response was already created
     * @see #getResponse()
     */
    WebServiceMessage createResponse();

    /**
     * Returns the response message, if created. Returns <code>null</code> if no response message was created so far.
     *
     * @return the response message, or <code>null</code> if none was created
     * @see #createResponse()
     */
    WebServiceMessage getResponse();

    /**
     * Sets the name and value of a property associated with the <code>MessageContext</code>. If the
     * <code>MessageContext</code> contains a value of the same property, the old value is replaced.
     *
     * @param name  name of the property associated with the value
     * @param value value of the property
     */
    void setProperty(String name, Object value);

    /**
     * Gets the value of a specific property from the <code>MessageContext</code>.
     *
     * @param name name of the property whose value is to be retrieved
     * @return value of the property
     */
    Object getProperty(String name);

    /**
     * Removes a property from the <code>MessageContext</code>.
     *
     * @param name name of the property to be removed
     */
    void removeProperty(String name);

    /**
     * Check if this message context contains a property with the given name.
     *
     * @param name the name of the property to look fo
     * @return <code>true</code> if the <code>MessageContext</code> contains the property; <code>false</code> otherwise
     */
    boolean containsProperty(String name);

    /**
     * Return the names of all properties in this <code>MessageContext</code>.
     *
     * @return the names of all properties in this context, or an empty array if none defined
     */
    String[] getPropertyNames();

}