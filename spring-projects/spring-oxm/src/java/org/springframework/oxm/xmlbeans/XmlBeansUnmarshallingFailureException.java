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
package org.springframework.oxm.xmlbeans;

import org.apache.xmlbeans.XmlException;
import org.springframework.oxm.UnmarshallingFailureException;
import org.xml.sax.SAXException;

/**
 * XMLBeans-specific subclass of <code>UnmarshallingFailureException</code>.
 * 
 * @author Arjen Poutsma
 * @see XmlBeansUtils#convertXmlBeansException(Exception, boolean) 
 */
public class XmlBeansUnmarshallingFailureException extends UnmarshallingFailureException {

    public XmlBeansUnmarshallingFailureException(XmlException ex) {
        super("XMLBeans unmarshalling exception: " + ex.getMessage(), ex);
    }

    public XmlBeansUnmarshallingFailureException(SAXException ex) {
        super("XMLBeans unmarshalling exception: " + ex.getMessage(), ex);
    }

}
