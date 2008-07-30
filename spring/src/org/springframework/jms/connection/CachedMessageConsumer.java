/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.jms.connection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * JMS MessageConsumer decorator that adapts all calls
 * to a shared MessageConsumer instance underneath.
 *
 * @author Juergen Hoeller
 * @since 2.5.6
 */
class CachedMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {

	protected final MessageConsumer target;


	public CachedMessageConsumer(MessageConsumer target) {
		this.target = target;
	}


	public String getMessageSelector() throws JMSException {
		return this.target.getMessageSelector();
	}

	public Queue getQueue() throws JMSException {
		return (this.target instanceof QueueReceiver ? ((QueueReceiver) this.target).getQueue() : null);
	}

	public Topic getTopic() throws JMSException {
		return (this.target instanceof TopicSubscriber ? ((TopicSubscriber) this.target).getTopic() : null);
	}

	public boolean getNoLocal() throws JMSException {
		return (this.target instanceof TopicSubscriber && ((TopicSubscriber) this.target).getNoLocal());
	}

	public MessageListener getMessageListener() throws JMSException {
		return this.target.getMessageListener();
	}

	public void setMessageListener(MessageListener messageListener) throws JMSException {
		this.target.setMessageListener(messageListener);
	}

	public Message receive() throws JMSException {
		return this.target.receive();
	}

	public Message receive(long timeout) throws JMSException {
		return this.target.receive(timeout);
	}

	public Message receiveNoWait() throws JMSException {
		return this.target.receiveNoWait();
	}

	public void close() throws JMSException {
		// It's a cached MessageConsumer...
	}


	public String toString() {
		return "Cached JMS MessageConsumer: " + this.target;
	}

}
