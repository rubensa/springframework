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

package org.springframework.jms.support.destination;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Simple implementation of the DestinationResolver interface,
 * resolving destination names as dynamic destinations.
 * @author Juergen Hoeller
 * @since 20.07.2004
 * @see javax.jms.Session#createQueue
 * @see javax.jms.Session#createTopic
 */
public class DynamicDestinationResolver implements DestinationResolver {

	public Destination resolveDestinationName(Session session, String destinationName, boolean isPubSubDomain)
			throws JMSException {
		if (isPubSubDomain) {
			return session.createTopic(destinationName);
		}
		else {
			return session.createQueue(destinationName);
		}
	}

}
