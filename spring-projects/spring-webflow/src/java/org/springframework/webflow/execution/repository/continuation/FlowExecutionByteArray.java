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
package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.util.FileCopyUtils;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Helper class that represents a serialized FlowExecution. Mainly intended for
 * use in FlowExecutionStorage implementations that store flow execution
 * continuations in their serialized forms.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.execution.FlowExecutionStorage
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionByteArray implements Serializable {

	private static final long serialVersionUID = -6346556580752644469L;

	/**
	 * The serialized flow execution.
	 */
	private byte[] data;

	/**
	 * Whether or not this flow execution array is compressed.
	 */
	private boolean compressed;

	public FlowExecutionByteArray(byte[] data, boolean compressed) {
		this.data = data;
		this.compressed = compressed;
	}

	public FlowExecutionByteArray(FlowExecution flowExecution, boolean compress) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(384);
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		try {
			oos.writeObject(flowExecution);
			oos.flush();
			if (compress) {
				this.data = compress(baos.toByteArray());
			}
			else {
				this.data = baos.toByteArray();
			}
		}
		finally {
			oos.close();
		}
		this.compressed = compress;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public FlowExecution deserializeFlowExecution() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(getData(true)));
		try {
			return (FlowExecution)ois.readObject();
		}
		finally {
			ois.close();
		}
	}

	public byte[] getData() {
		return data;
	}
	
	public byte[] getData(boolean decompress) throws IOException {
		if (isCompressed() && decompress) {
			return decompress(data);
		}
		else {
			return data;
		}
	}

	/**
	 * Internal helper method to compress given data using GZIP compression.
	 */
	private byte[] compress(byte[] dataToCompress) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipos = new GZIPOutputStream(baos);
		try {
			gzipos.write(dataToCompress);
			gzipos.flush();
		}
		finally {
			gzipos.close();
		}
		return baos.toByteArray();
	}

	/**
	 * Internal helper method to decompress given data using GZIP decompression.
	 */
	private byte[] decompress(byte[] dataToDecompress) throws IOException {
		GZIPInputStream gzipin = new GZIPInputStream(new ByteArrayInputStream(dataToDecompress));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileCopyUtils.copy(gzipin, baos);
		} finally {
			gzipin.close();
		}
		return baos.toByteArray();
	}
}