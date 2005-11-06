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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.util.FileCopyUtils;

/**
 * Helper class that aides in handling a flow execution as if it was a
 * continuation. Mainly intended for use in FlowExecutionStorage implementations
 * that store flow executions as if they were continuations.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionContinuation implements Serializable {

	/**
	 * The serialized continuation (flow execution snapshot).
	 */
	private byte[] data;

	/**
	 * Whether or not this continuation is compressed.
	 */
	private boolean compressed;

	/**
	 * Create a new flow execution continuation using given data, which should
	 * be a serialized representation of a <code>FlowExecution</code> object.
	 * @param data serialized flow execution data
	 * @param compressed indicates whether or not given data is compressed
	 * (using GZIP compression)
	 */
	public FlowExecutionContinuation(byte[] data, boolean compressed) {
		this.data = data;
		this.compressed = compressed;
	}

	/**
	 * Create a new flow execution continuation for given flow execution.
	 * @param flowExecution the flow execution to wrap
	 * @throws FlowExecutionStorageException when the flow execution cannot be
	 * serialized
	 * @param compress indicates whether or not the flow execution continuation
	 * should compress its state
	 */
	public FlowExecutionContinuation(FlowExecution flowExecution, boolean compress) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(flowExecution);
		oos.flush();
		if (compress) {
			this.data = compress(baos.toByteArray());
		}
		else {
			this.data = baos.toByteArray();
		}
		this.compressed = compress;
	}

	/**
	 * Returns a clone of the flow execution wrapped by this object.
	 * @throws FlowExecutionStorageException when the flow execution cannot be
	 * restored
	 */
	public FlowExecution readFlowExecution() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(getData(true)));
		return (FlowExecution)ois.readObject();
	}

	/**
	 * Returns the binary representation of the flow execution continuation.
	 * This is actually a serialized version of the continuation.
	 * @param decompress indicates whether or not the data should be
	 * decompressed (when it's compressed) before returning it
	 * @return the serialized flow execution data
	 * @throws FlowExecutionStorageException when the flow execution data cannot
	 * be obtained
	 */
	public byte[] getData(boolean decompress) throws IOException {
		if (isCompressed() && decompress) {
			return decompress(data);
		}
		else {
			return data;
		}
	}

	/**
	 * Returns whether or not this flow execution continuation is compressed.
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * Internal helper method to compress given data using GZIP compression.
	 */
	private byte[] compress(byte[] dataToCompress) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipos = new GZIPOutputStream(baos);
		gzipos.write(dataToCompress);
		gzipos.flush();
		gzipos.close();
		return baos.toByteArray();
	}

	/**
	 * Internal helper method to decompress given data using GZIP decompression.
	 */
	private byte[] decompress(byte[] dataToDecompress) throws IOException {
		GZIPInputStream gzipin = new GZIPInputStream(new ByteArrayInputStream(dataToDecompress));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileCopyUtils.copy(gzipin, baos);
		gzipin.close();
		return baos.toByteArray();
	}
}