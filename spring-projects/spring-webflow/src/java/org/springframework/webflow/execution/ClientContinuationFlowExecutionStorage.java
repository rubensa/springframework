/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution;

import org.apache.commons.codec.binary.Base64;
import org.springframework.webflow.Event;

/**
 * Flow execution storage implementation that will store a flow execution as a
 * <i>continuation</i> on the client side. It will actually encode the state of
 * the flow execution in the unique id that is returned from the
 * {@link #save(String, FlowExecution, Event) save} method. The load method
 * just decodes the incoming id and restores the <code>FlowExecution</code>
 * object.
 * <p>
 * Note that all clients in a web flow based application need to include the
 * unique flow execution id in each event they signal to the system. For HTTP
 * based clients, the flow execution id is sent using a request parameter.
 * If you're using client side continuations, you should make sure to use
 * the HTTP POST method to send the request parameters to the server. This is
 * required because there are limitations on the amount of data you can send
 * using an HTTP GET request and a client side continuation easily surpasses
 * that threshold.
 * <p>
 * <b>Warning:</b> storing state (a flow execution continuation) on the client
 * entails a certain security risk. This implementation does not provide a
 * secure way of storing state on the client, so a malicious client could
 * reverse engineer a continuation and get access to possible sensitive data stored
 * in the flow execution. If you need more security and still want to store
 * continuations on the client, subclass this class and override the methods
 * {@link #encode(FlowExecution)} and {@link #decode(String)}, implementing
 * them with a secure encoding/decoding algorithm, e.g. based on public/private
 * key encryption.
 * <p>
 * This class depends on the <a href="http://jakarta.apache.org/commons/codec/">
 * Jakarta Commons Codec</a> library to do BASE64 encoding.
 * 
 * @author Erwin Vervaet
 */
public class ClientContinuationFlowExecutionStorage implements FlowExecutionStorage {

	private boolean compress = false;

	/**
	 * Returns whether or not continuations should be compressed.
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * Set whether or not continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecution load(String id, Event requestingEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException {
		return decode(id);
	}

	public String save(String id, FlowExecution flowExecution, Event requestingEvent)
			throws FlowExecutionStorageException {
		return encode(flowExecution);
	}

	public void remove(String id, Event requestingEvent) throws FlowExecutionStorageException {
		// nothing to do here
	}

	/**
	 * Decode given data string, received from the client, and return the
	 * corresponding flow execution object.
	 * <p>
	 * Subclasses can override this to change the decoding algorithm. This
	 * class just does a BASE64 decoding and then deserializes the flow
	 * execution.
	 * @param data the encode flow execution data
	 * @return the decoded flow execution instance
	 */
	protected FlowExecution decode(String data) {
		return new FlowExecutionContinuation(Base64.decodeBase64(data.getBytes()), isCompress()).getFlowExecution();
	}

	/**
	 * Encode given flow execution object into a data string that can be
	 * stored on the client.
	 * <p>
	 * Subclasses can override this to change the encoding algorithm. This
	 * class just does a BASE64 encoding of the serialized flow execution.
	 * @param flowExecution the flow execution instance
	 * @return the encoded string representation
	 */
	protected String encode(FlowExecution flowExecution) {
		byte[] data = new FlowExecutionContinuation(flowExecution, isCompress()).getData(false);
		return new String(Base64.encodeBase64(data));
	}
}