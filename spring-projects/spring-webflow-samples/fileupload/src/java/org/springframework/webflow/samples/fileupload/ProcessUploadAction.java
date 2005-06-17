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
package org.springframework.webflow.samples.fileupload;

import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormAction;

public class ProcessUploadAction extends FormAction {
	
	public ProcessUploadAction() {
		setFormObjectName("file");
		setFormObjectClass(FileUploadBean.class);
	}
	
	protected void initBinder(RequestContext context, DataBinder binder) {
		// to actually be able to convert a multipart object to a byte[]
		// we have to register a custom editor (in this case the
		// ByteArrayMultipartFileEditor)
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		// now Spring knows how to handle multipart objects and convert them
	}

}