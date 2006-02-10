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

package org.springframework.ws.soap.endpoint;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.WebServiceMessage;

/**
 * Test case for AbstractMapBasedSoapEndpointMapping.
 */
public class MapBasedSoapEndpointMappingTest extends TestCase {

    private MockControl mappingControl;

    private AbstractMapBasedSoapEndpointMapping mappingMock;

    protected void setUp() throws Exception {
        mappingControl = MockClassControl.createControl(AbstractMapBasedSoapEndpointMapping.class);
        mappingMock = (AbstractMapBasedSoapEndpointMapping) mappingControl.getMock();
    }

    public void testBeanNames() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerSingleton("endpointMapping", MyMapBasedEndpointMapping.class);
        context.registerSingleton("endpoint", Object.class);
        context.registerAlias("endpoint", "alias");
        MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();
        mapping.setValidKeys(new String[]{"endpoint", "alias"});

        mapping.setRegisterBeanNames(true);
        mapping.setApplicationContext(context);

        // try bean
        mapping.setKey("endpoint");
        assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

        // try alias
        mapping.setKey("alias");
        assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

        // try non-mapped values
        mapping.setKey("endpointMapping");
        assertNull("Endpoint returned", mapping.getEndpointInternal(null));

    }

    public void testDisabledBeanNames() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerSingleton("endpoint", Object.class);

        MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();

        mapping.setRegisterBeanNames(true);
        mapping.setApplicationContext(context);

        mapping.setKey("endpoint");
        assertNull("Endpoint returned", mapping.getEndpointInternal(null));
    }

    public void testEndpointMap() throws Exception {
        Map endpointMap = new TreeMap();
        Object endpoint1 = new Object();
        Object endpoint2 = new Object();
        endpointMap.put("endpoint1", endpoint1);
        endpointMap.put("endpoint2", endpoint2);

        MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();
        mapping.setValidKeys(new String[]{"endpoint1", "endpoint2"});

        mapping.setEndpointMap(endpointMap);
        mapping.initApplicationContext();

        // try endpoint1
        mapping.setKey("endpoint1");
        assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

        // try endpoint2
        mapping.setKey("endpoint2");
        assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

        // try non-mapped values
        mapping.setKey("endpoint3");
        assertNull("Endpoint returned", mapping.getEndpointInternal(null));
    }

    private static class MyMapBasedEndpointMapping extends AbstractMapBasedSoapEndpointMapping {

        private String key;

        private String[] validKeys = new String[0];

        public void setKey(String key) {
            this.key = key;
        }

        public void setValidKeys(String[] validKeys) {
            this.validKeys = validKeys;
            Arrays.sort(this.validKeys);
        }

        protected boolean validateLookupKey(String key) {
            return Arrays.binarySearch(validKeys, key) >= 0;
        }

        protected String getLookupKeyForMessage(WebServiceMessage message) throws Exception {
            return key;
        }
    }

}