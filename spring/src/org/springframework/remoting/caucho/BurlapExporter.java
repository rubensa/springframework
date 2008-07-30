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

package org.springframework.remoting.caucho;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.server.BurlapSkeleton;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;

/**
 * General stream-based protocol exporter for a Burlap endpoint.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>.
 *
 * <p>This exporter will work with both Burlap 2.x and 3.x (respectively
 * Resin 2.x and 3.x), autodetecting the corresponding skeleton class.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 * @see #invoke(java.io.InputStream, java.io.OutputStream)
 * @see BurlapServiceExporter
 * @see SimpleBurlapServiceExporter
 */
public class BurlapExporter extends RemoteExporter implements InitializingBean {

	private BurlapSkeleton skeleton;


	public void afterPropertiesSet() {
		prepare();
	}

	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		try {
			try {
				// Try Burlap 3.x (with service interface argument).
				Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class, Class.class});
				checkService();
				checkServiceInterface();
				this.skeleton = (BurlapSkeleton)
						ctor.newInstance(new Object[] {getProxyForService(), getServiceInterface()});
			}
			catch (NoSuchMethodException ex) {
				// Fall back to Burlap 2.x (without service interface argument).
				Constructor ctor = BurlapSkeleton.class.getConstructor(new Class[] {Object.class});
				this.skeleton = (BurlapSkeleton) ctor.newInstance(new Object[] {getProxyForService()});
			}
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Burlap skeleton initialization failed", ex);
		}
	}


	/**
	 * Perform an invocation on the exported object.
	 * @param inputStream the request stream
	 * @param outputStream the response stream
	 * @throws Throwable if invocation failed
	 */
	public void invoke(InputStream inputStream, OutputStream outputStream) throws Throwable {
		Assert.notNull(this.skeleton, "Burlap exporter has not been initialized");
		ClassLoader originalClassLoader = overrideThreadContextClassLoader();
		try {
			this.skeleton.invoke(new BurlapInput(inputStream), new BurlapOutput(outputStream));
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException ex) {
			}
			try {
				outputStream.close();
			}
			catch (IOException ex) {
			}
			resetThreadContextClassLoader(originalClassLoader);
		}
	}

}
