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

package org.springframework.instrument;

import java.lang.instrument.Instrumentation;

/**
 * Java agent that saves the {@link Instrumentation} interface from the JVM for
 * later use.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver
 */
public class InstrumentationSavingAgent {
	
	private static Instrumentation instrumentation;


	/**
	 * Save the {@link Instrumentation} interface exposed by the JVM.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}


	/**
	 * Return the {@link Instrumentation} interface exposed by the JVM.
    * @return the <code>Instrumentation</code> instance previously saved when
    * the {@link #premain} method was called by the JVM; will be
    * <code>null</code> if this class was not used as the Java agent when this
    * JVM was started.
	 */
	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}

}
