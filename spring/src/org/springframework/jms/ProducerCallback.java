/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Callback interface for JMS code.  To be used with JmsTemplate's send method,
 * often as an anonymous class within a method implementation.  The typical
 * implementation will perform multiple operations on the JMS Session and
 * MessageProducer.   When used with a 1.0.2 provider, you need to downcast
 * to the appropriate domain implementation, either QueueSender or TopicPublisher,
 * to send a message.
 *
 * @author Mark Pollack
 */
public interface ProducerCallback {

	/**
	 * Perform operations on the Session and MessageProducer.  The messgae producer is
	 * not associated with any destination.
	 * @param session The JMS session object to use
	 * @param msgProducer  The JMS MessageProducer object to use
	 * @return A return value, if any.
	 * @throws JMSException JMS provider exception.
	 */
	Object doInJms(Session session, MessageProducer msgProducer) throws JMSException;
}
