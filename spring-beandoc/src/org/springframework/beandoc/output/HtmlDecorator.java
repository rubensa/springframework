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
import org.springframework.util.StringUtils;


/**
 * HtmlDecorator
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class HtmlDecorator extends SimpleDecorator {

    private static final String DEFAULT_CSS_FILE = "context.css";
  
    private static final String ATTRIBUTE_CSS_NAME = "beandocCssLocation";

    private static final String ATTRIBUTE_HTML_NAME = "beandocHtmlFileName";

    private String contextCssUrl = DEFAULT_CSS_FILE;
    
    private String title = "Application Context";    
    
    private String thisFileName;  
    
    private String thisHtmlFileName;

    /**
     * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement
     */
    protected void decorateElement(Element element) {
        if (element.isRootElement()) {
            // add CSS file locations
            element.setAttribute(ATTRIBUTE_CSS_NAME, contextCssUrl);
            thisFileName = element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
            thisHtmlFileName = StringUtils.replace(thisFileName, ".xml", ".html");
        }
        
        String refFileName = element.getAttributeValue(Tags.ATTRIBUTE_BD_FILENAME);
        if (refFileName != null) {
            String htmlFileName = StringUtils.replace(refFileName, ".xml", ".html");
            element.setAttribute(ATTRIBUTE_HTML_NAME, htmlFileName);
        }
        
        String tag = element.getName();
        if (tag.equals(Tags.TAGNAME_BEAN) || tag.equals(Tags.TAGNAME_DESCRIPTION))
            element.setAttribute(ATTRIBUTE_HTML_NAME, thisHtmlFileName);
            
    }

    /**
     * Optional location of a CSS file that can be used to skin the beandoc output.  By default,
     * a file will be supplied and copied into the output directory which the HTML file will
     * reference.  If you set a value here, this file will not be copied to the output directory
     * and your reference will be used instead.
     * 
     * @param contextCssUrl a locations (absolute or relative to your output directory) 
     *      that the CSS file can be found which is used to skin the beandoc output.
     */
    public void setContextCssUrls(String contextCssUrl) {
        this.contextCssUrl = contextCssUrl;
    }

    /**
     * An URI (absolute or relative) that will be added toi the HTML output as a
     * <code>&lt;link rel="stylesheet"&gt;</code> tag in the header.  If none is provided
     * then a default CSS file is copied to the output directory and linked in the HTML
     * header.
     * 
     * @return the String representing absolute or relative references to a CSS file
     *      used to skin the beandoc output.
     */
    public String getContextCssUrl() {
        return contextCssUrl;
    }
    
    /**
     * Sets the page titles for the documentation output.  Graph titles are taken from 
     * the individual file names used to generate the graphs.
     * 
     * @param title the page title used in documentation output
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Title of the application context.  For example "JPetStore Application Context".  Will
     * be used as the <code>&lt;title&gt;</code> tag in HTML documentation.
     * 
     * @return the page title used in the documentation 
     */
    public String getTitle() {
        return title;
    }

}
