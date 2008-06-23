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

package org.springframework.jdbc.support.xml;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Interface defining handling involved with providing <code>OutputStream</code>
 * data for XML input.
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see java.io.OutputStream
 */
public interface XmlBinaryStreamProvider {

	/**
	 * Implementations must implement this method to provide the XML content
	 * for the <code>OutputStream</code>.
	 * @param outputStream the <code>OutputStream</code> object being used to provide the XML input
	 * @throws IOException if an I/O error occurs while providing the XML
	 */
	void provideXml(OutputStream outputStream) throws IOException;

}
