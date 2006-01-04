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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jndi.JndiTemplate;

/**
 * Simple implementation of the JmsAdmin interface
 *
 * @author Mark Pollack
 */
public class DefaultJmsAdmin implements JmsAdmin {

	protected final Log logger = LogFactory.getLog(getClass());

	/** JNDI prefix used in a J2EE container */
	public static String CONTAINER_PREFIX = "java:comp/env/";

	private boolean resourceRef = false;

	protected Map queueMap;

	protected Map topicMap;

	protected JndiTemplate jndiTemplate;

	protected JmsOperations jmsTemplate;

	public DefaultJmsAdmin() {
		jndiTemplate = new JndiTemplate();
		queueMap = Collections.synchronizedMap(new HashMap());
		topicMap = Collections.synchronizedMap(new HashMap());
	}

	/**
	 * Set the JNDI template to use for the JNDI lookup.
	 * You can also specify JNDI environment settings via setJndiEnvironment.
	 * @see #setJndiEnvironment
	 */
	public final void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * Return the JNDI template to use for the JNDI lookup.
	 */
	public final JndiTemplate getJndiTemplate() {
		return jndiTemplate;
	}

	/**
	 * Set the JNDI environment to use for the JNDI lookup.
	 * Creates a JndiTemplate with the given environment settings.
	 * @see #setJndiTemplate
	 */
	public final void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiTemplate = new JndiTemplate(jndiEnvironment);
	}

	/**
	 * Return the JNDI enviromment to use for the JNDI lookup.
	 */
	public final Properties getJndiEnvironment() {
		return jndiTemplate.getEnvironment();
	}

	/**
	 * Set if the lookup occurs in a J2EE container, i.e. if the prefix
	 * "java:comp/env/" needs to be added if the JNDI name doesn't already
	 * contain it. Default is false.
	 * <p>Note: Will only get applied if no other scheme like "java:" is given.
	 */
	public void setResourceRef(boolean resourceRef) {
		this.resourceRef = resourceRef;
	}

	/**
	 * Return if the lookup occurs in a J2EE container.
	 */
	public final boolean isResourceRef() {
		return resourceRef;
	}

	/**
	 * Create a dynamic queue
	 */
	public QueueInfo createQueue(final QueueInfo queueInfo)
	    throws JmsException {
		getJmsTemplate().execute(new SessionCallback() {
			public Object doInJms(Session s) throws JMSException {
				//TODO look into side effects of calling twice
				Queue q = s.createQueue(queueInfo.getName());
				logger.info(
				    "Created dynamic queue with name = " + queueInfo.getName());
				queueMap.put(queueInfo.getName(), q);
				return null;
			}
		});
		return queueInfo;
	}

	/**
	 * Create a dynamic topic
	 */
	public TopicInfo createTopic(final TopicInfo topicInfo) {
		getJmsTemplate().execute(new SessionCallback() {
			public Object doInJms(Session s) throws JMSException {
				//TODO look into side effects of calling twice
				Topic t = s.createTopic(topicInfo.getName());
				logger.info(
				    "Created dynamic topic with name = " + topicInfo.getName());
				topicMap.put(topicInfo.getName(), t);
				return null;
			}
		});
		return topicInfo;
	}


	/**
	 * Return either a dynamic topic or queue destination.  You should add
	 * a JNDI prefix to the destination name as appropriate for your environment.
	 * @param destinationName name of the dynamic destination.
	 * @return The JMS destination. Null if not found.
	 */
	public Destination lookupDynamic(String destinationName) {
		if (queueMap.containsKey(destinationName)) {
			return (Destination) queueMap.get(destinationName);
		}
		else if (topicMap.containsKey(destinationName)) {
			return (Destination) topicMap.get(destinationName);
		}
		else {
			return null;
		}
	}

	public Destination lookup(String destName) throws NamingException {
		destName = convertJndiName(destName);
		Destination d = (Destination) jndiTemplate.lookup(destName);
		if (logger.isInfoEnabled()) {
			logger.info(
			    "Looked up destination with name ["
			    + destName
			    + "]"
			    + " in JNDI");
		}
		return d;
	}

	private String convertJndiName(String destName) {
		// prepend container prefix if not already specified and no other scheme given
		if (this.resourceRef && !destName.startsWith(CONTAINER_PREFIX) && destName.indexOf(':') == -1) {
			destName = CONTAINER_PREFIX + destName;
		}
		return destName;
	}

	public Destination lookup(
	    String destName,
	    boolean createDynamic,
	    boolean isPubSubDomain) {

		Destination dest = null;
		if (createDynamic) {
			if (isPubSubDomain) {
				dest = lookupDynamicTopic(destName);
			}
			else {
				dest = lookupDynamicQueue(destName);
			}
		}
		if (dest == null) {
			try {
				destName = convertJndiName(destName);
				dest = (Destination) jndiTemplate.lookup(destName);
				if (logger.isInfoEnabled()) {
					logger.info(
					    "Looked up destination with name ["
					    + destName
					    + "]"
					    + " in JNDI");
				}
			}
			catch (NamingException e) {
				if (createDynamic) {
					if (isPubSubDomain) {
						createTopic(new TopicInfo(destName));
					}
					else {
						createQueue(new QueueInfo(destName));
					}
				}
				else {
                    //RuntimeException just to get it to compile. throw away code now anyway.
					throw new RuntimeException(
					    "Couldn't get destination name ["
					    + destName
					    + "] from JNDI");
                        
				}
			}
		}
		return dest;
	}

	public Topic lookupDynamicTopic(String topicName) {
		return (Topic) topicMap.get(topicName);
	}

	public Queue lookupDynamicQueue(String queueName) {
		return (Queue) queueMap.get(queueName);
	}

	/**
	 * The JmsTemplate to delegate some operations to
	 * @return the JmsTemplate
	 */
	public JmsOperations getJmsTemplate() {
		return jmsTemplate;
	}

	/**
	 * Set the JmsTemplate to delegate some operations to
	 * @param t the JmsTemplate
	 */
	public void setJmsTemplate(JmsOperations t) {
		jmsTemplate = t;
	}

}