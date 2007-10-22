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

import org.jdom.Element;
import org.jdom.Attribute;

import org.springframework.beandoc.Tags;
import org.springframework.util.Assert;

import java.util.List;

/**
 * HtmlDecorator decorates the DOM's with attributes used predominantly in the
 * generation of HTML documentation. Bean properties on this class make it
 * possible to specify titles, page footers, CSS URL's and whether graphs should
 * be included in the HTML (whether they're generated or not).
 * 
 * @author Darren Davison
 * @author Moritz Kleine
 * @since 1.0
 */
public class HtmlDecorator extends SimpleDecorator {

	private static final String DEFAULT_CSS_FILE = "default.css";

	private static final String ATTRIBUTE_CSS_NAME = "beandocCssLocation";

	private static final String ATTRIBUTE_HTML_NAME = "beandocHtmlFileName";

	private static final String ATTRIBUTE_TITLE = "beandocContextTitle";

	private static final String ATTRIBUTE_FOOTER = "beandocPageFooter";

	private static final String ATTRIBUTE_NOGRAPHS = "beandocNoGraphs";

	private FilenameStrategy filenameStrategy = new FilenameAppenderStrategy(".html");

	private String cssUrl = DEFAULT_CSS_FILE;

	private String title = "Application Context";

	private String footer = "Copyright Spring BeanDoc contributers";

	private boolean includeGraphs = true;

	private String thisFileName;

	private String thisHtmlFileName;

	/**
	 * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement
	 */
	protected void decorateElement(Element element) {
		handleAttributes(element);
		if (element.isRootElement()) {
			// add CSS file locations, title, footer
			element.setAttribute(ATTRIBUTE_CSS_NAME, cssUrl);
			element.setAttribute(ATTRIBUTE_TITLE, title);
			element.setAttribute(ATTRIBUTE_FOOTER, footer);

			// keep a reference to file names
			thisFileName = element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
			thisHtmlFileName = filenameStrategy.getFileName(thisFileName);

			// set ignore graph output?
			if (!includeGraphs)
				element.setAttribute(ATTRIBUTE_NOGRAPHS, "noGraphs");
		}

		String refFileName = element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
		if (refFileName != null)
			element.setAttribute(ATTRIBUTE_HTML_NAME, filenameStrategy.getFileName(refFileName));

		String tag = element.getName();
		if (tag.equals(Tags.TAGNAME_BEAN) || tag.equals(Tags.TAGNAME_DESCRIPTION))
			element.setAttribute(ATTRIBUTE_HTML_NAME, thisHtmlFileName);
	}

	private void handleAttributes(Element element) {
		List attributes = element.getAttributes();
		int size = attributes.size();
		for (int i = 0; i < size; i++) {
			Attribute a = (Attribute) attributes.get(i);
			String name = a.getName();
			if (name.startsWith("beandoc") && name.endsWith("FileName")) {
				element.setAttribute(name + "Html", filenameStrategy.getFileName(a.getValue()));
			}
		}
	}

	/**
	 * Optional location of a CSS file that can be used to skin the beandoc
	 * output. By default, a file will be supplied and copied into the output
	 * directory which the HTML file will reference. If you set a value here,
	 * this file will not be copied to the output directory and your reference
	 * will be used instead.
	 * 
	 * @param cssUrl a locations (absolute or relative to your output directory)
	 * that the CSS file can be found which is used to skin the beandoc output.
	 */
	public void setCssUrl(String cssUrl) {
		Assert.hasText(cssUrl);
		this.cssUrl = cssUrl;
	}

	/**
	 * An URI (absolute or relative) that will be added toi the HTML output as a
	 * <code>&lt;link rel="stylesheet"&gt;</code> tag in the header. If none
	 * is provided then a default CSS file is copied to the output directory and
	 * linked in the HTML header.
	 * 
	 * @return the String representing absolute or relative references to a CSS
	 * file used to skin the beandoc output.
	 */
	public String getCssUrl() {
		return cssUrl;
	}

	/**
	 * Sets the page titles for the documentation output. Graph titles are taken
	 * from the individual file names used to generate the graphs.
	 * 
	 * @param title the page title used in documentation output
	 */
	public void setTitle(String title) {
		Assert.hasText(title);
		this.title = title;
	}

	/**
	 * Title of the application context. For example "JPetStore Application
	 * Context". Will be used as the <code>&lt;title&gt;</code> tag in HTML
	 * documentation.
	 * 
	 * @return the page title used in the documentation
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set a footer for each page which can contain HTML tags.
	 * 
	 * @param footer the value to use as a footer for each beandoc page
	 */
	public void setFooter(String footer) {
		Assert.hasText(footer);
		this.footer = footer;
	}

	/**
	 * Set to false if you wish the HTML documentation not to include images and
	 * links to graphing output. This could be used if you elect not to generate
	 * graphs, or if graphs are being generated in a non-compatible format for
	 * the HTML output.
	 * 
	 * @param includeGraphs set to false to prevent graphs being included in
	 * HTML documentation. True by default.
	 */
	public void setIncludeGraphs(boolean includeGraphs) {
		this.includeGraphs = includeGraphs;
	}

	/**
	 * sets the filename resolution strategy to use for this decorator
	 * 
	 * @param filenameStrategy
	 */
	public void setFilenameStrategy(FilenameStrategy filenameStrategy) {
		this.filenameStrategy = filenameStrategy;
	}
}
