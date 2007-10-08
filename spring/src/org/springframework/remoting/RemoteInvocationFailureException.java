/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.remoting;

/**
 * RemoteAccessException subclass to be thrown when the execution
 * of the target method failed on the server side, for example
 * when a method was not found on the target object.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see RemoteProxyFailureException
 */
public class RemoteInvocationFailureException extends RemoteAccessException {

	/**
	 * Constructor for RemoteInvocationFailureException.
	 * @param msg the detail message
	 * @param cause the root cause from the remoting API in use
	 */
	public RemoteInvocationFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
