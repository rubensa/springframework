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

package org.springframework.web.servlet.view.xmlc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.enhydra.xml.xmlc.servlet.XMLCContext;
import org.enhydra.xml.xmlc.XMLObject;
import org.enhydra.xml.io.OutputOptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.view.AbstractView;
import org.springframework.context.ApplicationContextException;
import org.springframework.beans.BeansException;

/**
 * @author Rod Johnson
 * @author Rob Harrop
 */
public abstract class AbstractXmlcView extends AbstractView {

	/**
	 * <code>Log</code> for this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Stores the default
	 */
	private static final OutputOptions DEFAULT_OUTPUT_OPTIONS = new OutputOptions();

	/**
	 * Stores the <code>XMLCContext</code>.
	 */
	private XMLCContext xmlcContext;

	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		this.xmlcContext = (XMLCContext) getWebApplicationContext().getServletContext().getAttribute(XMLCContext.CONTEXT_ATTRIBUTE);
		if (this.xmlcContext == null)
			throw new ApplicationContextException("No XMLCContext inited. Use XMLCConfigServlet with loadOnStartup");
	}

	protected void renderMergedOutputModel(Map model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		XMLObject xo = createXMLObject(model, request, response, this.xmlcContext);
		response.setContentType(getContentType());

		OutputOptions oo = getOutputOptions();

		if(logger.isDebugEnabled()) {
			logger.debug("Using OutputOptions [" + oo + "]");
		}
		// write the response back to the client
		this.xmlcContext.writeDOM(request, response, oo, xo);
	}

	/**
	 * Subclasses can override this method to change the output options used
	 * by XMLC when writing view data to the client.
	 * @return the <code>OutputOptions</code> to use when writing view data to the client.
	 */
	protected OutputOptions getOutputOptions() {
		return DEFAULT_OUTPUT_OPTIONS;
	}

	protected abstract XMLObject createXMLObject(Map model, HttpServletRequest request,
			HttpServletResponse response, XMLCContext context) throws ServletException;


}
