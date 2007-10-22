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

package org.springframework.beandoc.output;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.util.Assert;
import org.springframework.beandoc.DefaultContextProcessor;

/**
 * Generates a consolidated Document from the array of individual Document
 * objects and allows subclasses to transform this instead. The generated DOM
 * simply aggregates multiple &lt;beans&gt; tags inside a &lt;consolidated&gt;
 * tag.
 * 
 * @author Darren Davison
 * @author Marat Radchenko
 * @since 1.0
 */
public class ConsolidatedTransformer extends XslTransformer {

	private static final String TAG_CONSOLIDATED = "consolidated";

	private String filenameRoot;

	protected Document consolidatedDocument;

	/**
	 */
	public ConsolidatedTransformer() {
	}

	/**
	 * @param templateName
	 */
	public ConsolidatedTransformer(String templateName) {
		super(templateName);
	}

	/**
	 * Generates a single <code>Document</code> from the array of input
	 * <code>Document</code>s and stores the reference for later use.
	 * 
	 * @see org.springframework.beandoc.output.XslTransformer#initTransform
	 */
	protected final void initTransform(Document[] contextDocuments, File outputDirectory) throws Exception {
		consolidatedDocument = new Document();
		Element root = new Element(TAG_CONSOLIDATED);
		DefaultContextProcessor.setSpringNamespace(root, false);
		consolidatedDocument.setRootElement(root);

		for (int i = 0; i < contextDocuments.length; i++) {
			Element inputRoot = (Element) contextDocuments[i].getRootElement().clone();
			root.addContent(inputRoot);
		}
	}

	/**
	 * Override default behaviour to provide a single transformation of the
	 * consolidated DOM created.
	 * 
	 * @see org.springframework.beandoc.output.XslTransformer#handleTransform
	 */
	protected void handleTransform(Document[] contextDocuments, File outputDir) {
		doXslTransform(consolidatedDocument, outputDir);
	}

	/**
	 * Always ignore any parameter and return the consolidated file root input
	 * with the default strategy
	 * 
	 * @see org.springframework.beandoc.output.XslTransformer#getOutputForDocument(java.lang.String)
	 */
	protected String getOutputForDocument(String inputFileName) {
		return filenameStrategy.getFileName(this.filenameRoot);
	}

	/**
	 * @param filenameRoot the filename that will represent the consolidated
	 * output of the DOM transformation
	 */
	public void setFilenameRoot(String filenameRoot) {
		Assert.hasText(filenameRoot);
		this.filenameRoot = filenameRoot;
	}

	/**
	 * @return the filename root for this transformer
	 */
	public String getFilenameRoot() {
		return filenameRoot;
	}
}
