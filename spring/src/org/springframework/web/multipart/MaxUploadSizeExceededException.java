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

package org.springframework.web.multipart;

/**
 * MultipartException subclass thrown when an upload exceeds the
 * maximum allowed size.
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public class MaxUploadSizeExceededException extends MultipartException {

	private long maxUploadSize;

	public MaxUploadSizeExceededException(String msg) {
		super(msg);
	}

	public MaxUploadSizeExceededException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public MaxUploadSizeExceededException(long maxUploadSize) {
		this(maxUploadSize, null);
	}

	public MaxUploadSizeExceededException(long maxUploadSize, Throwable ex) {
		super("Maximum upload size of " + maxUploadSize + " bytes exceeded", ex);
		this.maxUploadSize = maxUploadSize;
	}

	public long getMaxUploadSize() {
		return maxUploadSize;
	}

}
