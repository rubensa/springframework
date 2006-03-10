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
package org.springframework.oxm.jibx;

import org.springframework.oxm.AbstractUnmarshallerTest;
import org.springframework.oxm.Unmarshaller;

public class JibxUnmarshallerTest extends AbstractUnmarshallerTest {

    protected Unmarshaller createUnmarshaller() throws Exception {
        JibxMarshaller unmarshaller = new JibxMarshaller();
        unmarshaller.setTargetClass(Flights.class);
        unmarshaller.afterPropertiesSet();
        return unmarshaller;
    }

    protected void testFlights(Object o) {
        Flights flights = (Flights) o;
        assertNotNull("Flights is null", flights);
        assertEquals("Invalid amount of flight elements", 1, flights.sizeFlightList());
        FlightType flight = (FlightType) flights.getFlight(0);
        assertNotNull("Flight is null", flight);
        assertEquals("Number is invalid", 42L, flight.getNumber());
    }

    public void testUnmarshalDomSource() throws Exception {
        // Unfortunately, JiBX does not support DOMSources
        // hence the override here
    }

    public void testUnmarshalSAXSource() throws Exception {
        // Unfortunately, JiBX does not support SAXSources
        // hence the override here
    }

}
