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

package org.springframework.ws.endpoint;

import junit.framework.TestCase;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;

public class PayloadLoggingInterceptorTest extends TestCase {

    private PayloadLoggingInterceptor interceptor;

    private CountingAppender appender;

    private MockMessageContext messageContext;

    protected void setUp() throws Exception {
        interceptor = new PayloadLoggingInterceptor();
        interceptor.afterPropertiesSet();
        appender = new CountingAppender();
        BasicConfigurator.configure(appender);
        Logger.getRootLogger().setLevel(Level.DEBUG);
        MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
        messageContext = new MockMessageContext(request);
        appender.reset();
    }

    public void testHandleRequestDisabled() throws Exception {
        interceptor.setLogRequest(false);
        int eventCount = appender.getCount();
        interceptor.handleRequest(messageContext, null);
        assertEquals("PayloadLoggingInterceptor logged when disabled", appender.getCount(), eventCount);
    }

    public void testHandleRequestEnabled() throws Exception {
        int eventCount = appender.getCount();
        interceptor.handleRequest(messageContext, null);
        assertTrue("PayloadLoggingInterceptor did not log", appender.getCount() > eventCount);
    }

    public void testHandleResponseDisabled() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload("<response/>");
        interceptor.setLogResponse(false);
        int eventCount = appender.getCount();
        interceptor.handleResponse(messageContext, null);
        assertEquals("PayloadLoggingInterceptor logged when disabled", appender.getCount(), eventCount);
    }

    public void testHandleResponseEnabled() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload("<response/>");
        int eventCount = appender.getCount();
        interceptor.handleResponse(messageContext, null);
        assertTrue("PayloadLoggingInterceptor did not log", appender.getCount() > eventCount);
    }

    private static class CountingAppender extends AppenderSkeleton {

        private int count;

        public int getCount() {
            return count;
        }

        public void reset() {
            count = 0;
        }

        protected void append(LoggingEvent loggingEvent) {
            count++;
        }

        public boolean requiresLayout() {
            return false;
        }

        public void close() {
        }
    }
}