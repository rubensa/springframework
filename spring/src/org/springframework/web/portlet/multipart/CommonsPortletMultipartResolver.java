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

package org.springframework.web.portlet.multipart;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletContext;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.portlet.PortletRequestContext;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsFileUploadSupport;
import org.springframework.web.portlet.context.PortletContextAware;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * PortletMultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>
 * 1.1 or higher.
 *
 * <p>Provides maxUploadSize, maxInMemorySize, and defaultEncoding settings as
 * bean properties (inherited from CommonsFileUploadSupport). See respective
 * PortletFileUpload / DiskFileItemFactory properties (sizeMax, sizeThreshold,
 * headerEncoding) for details in terms of defaults and accepted values.
 *
 * <p>Saves temporary files to the portlet container's temporary directory.
 * Needs to be initialized <i>either</i> by an application context <i>or</i>
 * via the constructor that takes a PortletContext (for standalone usage).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #CommonsPortletMultipartResolver(javax.portlet.PortletContext)
 * @see org.springframework.web.multipart.commons.CommonsMultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.apache.commons.fileupload.portlet.PortletFileUpload
 * @see org.apache.commons.fileupload.disk.DiskFileItemFactory
 */
public class CommonsPortletMultipartResolver extends CommonsFileUploadSupport
		implements PortletMultipartResolver, PortletContextAware {

	/**
	 * Constructor for use as bean. Determines the portlet container's
	 * temporary directory via the PortletContext passed in as through the
	 * PortletContextAware interface (typically by an ApplicationContext).
	 * @see #setPortletContext
	 * @see org.springframework.web.portlet.context.PortletContextAware
	 */
	public CommonsPortletMultipartResolver() {
		super();
	}

	/**
	 * Constructor for standalone usage. Determines the portlet container's
	 * temporary directory via the given PortletContext.
	 * @param portletContext the PortletContext to use
	 */
	public CommonsPortletMultipartResolver(PortletContext portletContext) {
		this();
		setPortletContext(portletContext);
	}

	/**
	 * Initialize the underlying <code>org.apache.commons.fileupload.portlet.PortletFileUpload</code>
	 * instance. Can be overridden to use a custom subclass, e.g. for testing purposes.
	 * @return the new PortletFileUpload instance
	 */
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new PortletFileUpload(fileItemFactory);
	}

	public void setPortletContext(PortletContext portletContext) {
		if (!isUploadTempDirSpecified()) {
			getFileItemFactory().setRepository(PortletUtils.getTempDir(portletContext));
		}
	}


	public boolean isMultipart(ActionRequest request) {
		return PortletFileUpload.isMultipartContent(new PortletRequestContext(request));
	}

	public MultipartActionRequest resolveMultipart(ActionRequest request) throws MultipartException {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		try {
			List fileItems = ((PortletFileUpload) fileUpload).parseRequest(request);
			MultipartParsingResult parsingResult = parseFileItems(fileItems, encoding);
			return new DefaultMultipartActionRequest(
					request, parsingResult.getMultipartFiles(), parsingResult.getMultipartParameters());
		}
		catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		}
		catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart portlet request", ex);
		}
	}

	/**
	 * Determine the encoding for the given request.
	 * Can be overridden in subclasses.
	 * <p>The default implementation checks the request encoding,
	 * falling back to the default encoding specified for this resolver.
	 * @param request current HTTP request
	 * @return the encoding for the request (never <code>null</code>)
	 * @see javax.portlet.ActionRequest#getCharacterEncoding
	 * @see #setDefaultEncoding
	 */
	protected String determineEncoding(ActionRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = getDefaultEncoding();
		}
		return encoding;
	}

	public void cleanupMultipart(MultipartActionRequest request) {
		cleanupFileItems(request.getFileMap().values());
	}

}
