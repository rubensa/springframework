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
package org.springframework.jmx.exceptions;

/**
 * @author Rob Harrop
 */
public class MBeanAssemblyException extends RuntimeException {

    /**
     * 
     */
    public MBeanAssemblyException() {
        super();
    }

    /**
     * @param arg0
     */
    public MBeanAssemblyException(String msg) {
        super(msg);
    }

    /**
     * @param arg0
     */
    public MBeanAssemblyException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public MBeanAssemblyException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

}
