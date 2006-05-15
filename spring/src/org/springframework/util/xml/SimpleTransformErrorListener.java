/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.util.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;

/**
 * Simple <code>javax.xml.transform.ErrorListener</code> implementation:
 * logs warnings using the given Commons Logging logger instance,
 * and rethrows errors to discontinue the XML transformation.
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public class SimpleTransformErrorListener implements ErrorListener {

	private final Log logger;


	/**
	 * Create a new SimpleTransformErrorListener for the given
	 * Commons Logging logger instance.
	 */
	public SimpleTransformErrorListener(Log logger) {
		this.logger = logger;
	}


	public void warning(TransformerException ex) throws TransformerException {
		logger.warn("Ignored XSLT transformation warning", ex);
	}

	public void error(TransformerException ex) throws TransformerException {
		throw ex;
	}

	public void fatalError(TransformerException ex) throws TransformerException {
		throw ex;
	}

}
