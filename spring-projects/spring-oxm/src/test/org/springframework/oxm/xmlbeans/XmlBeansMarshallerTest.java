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

import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.AbstractMarshallerTest;
import org.springframework.oxm.Marshaller;
import org.springframework.samples.flight.FlightType;
import org.springframework.samples.flight.FlightsDocument;
import org.springframework.samples.flight.FlightsDocument.Flights;

public class XmlBeansMarshallerTest extends AbstractMarshallerTest {

    protected Marshaller createMarshaller() throws Exception {
        return new XmlBeansMarshaller();
    }

    public void testMarshalNonXmlObject() throws Exception {
        try {
            this.marshaller.marshal(new Object(), new StreamResult(new ByteArrayOutputStream()));
            fail("XmlBeansMarshaller did not throw ClassCastException for non-XmlObject");
        }
        catch (ClassCastException e) {
            // Expected behavior
        }
    }

    protected Object createFlights() {
        FlightsDocument flightsDocument = FlightsDocument.Factory.newInstance();
        Flights flights = flightsDocument.addNewFlights();
        FlightType flightType = flights.addNewFlight();
        flightType.setNumber(42L);
        return flightsDocument;
    }

}
