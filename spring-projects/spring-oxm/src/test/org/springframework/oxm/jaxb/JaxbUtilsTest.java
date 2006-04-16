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
package org.springframework.oxm.jaxb;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationException;

import junit.framework.TestCase;

public class JaxbUtilsTest extends TestCase {

    public void testConvertMarshallingException() {
        assertTrue("Invalid exception conversion",
                JaxbUtils.convertJaxbException(new MarshalException("")) instanceof JaxbMarshallingFailureException);
    }

    public void testConvertUnmarshallingException() {
        assertTrue("Invalid exception conversion", JaxbUtils
                .convertJaxbException(new UnmarshalException("")) instanceof JaxbUnmarshallingFailureException);
    }

    public void testConvertValidationException() {
        assertTrue("Invalid exception conversion",
                JaxbUtils.convertJaxbException(new ValidationException("")) instanceof JaxbValidationFailureException);
    }

    public void testConvertJAXBException() {
        assertTrue("Invalid exception conversion",
                JaxbUtils.convertJaxbException(new JAXBException("")) instanceof JaxbSystemException);
    }

    public void testGetJaxbVersion() throws Exception {
        assertEquals("Invalid JAXB version", JaxbUtils.JAXB_2, JaxbUtils.getJaxbVersion());
    }
}
