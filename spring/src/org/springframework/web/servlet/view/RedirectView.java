package org.springframework.web.servlet.view;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * View that redirects to an internal or external URL,
 * exposing all model attributes as HTTP query parameters.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id$
 */
public class RedirectView extends AbstractView {

	public static final String DEFAULT_ENCODING_SCHEME = "UTF-8";

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;

	private String url;

	public RedirectView() {
	}

	public RedirectView(String url) {
		setUrl(url);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected String getUrl() {
		return url;
	}

	/**
	 * Set the encoding scheme for this view.
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}

	/**
	 * Return the encoding scheme for this view.
   */
	protected String getEncodingScheme() {
		return this.encodingScheme;
	}

	/**
	 * Overridden lifecycle method to check that 'url' property is set.
	 */
	protected void initApplicationContext() throws IllegalArgumentException {
		if (this.url == null) {
			throw new IllegalArgumentException("Must set 'url' property in class [" + getClass().getName() + "]");
		}
	}

	/**
	 * Convert model to request parameters and redirect to the given URL.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuffer url = new StringBuffer(getUrl());
		// If there are not already some parameters, we need a ?
		boolean first = (getUrl().indexOf('?') < 0);
		Iterator entries = queryProperties(model).entrySet().iterator();
		while (entries.hasNext()) {
			if (first) {
				url.append("?");
				first = false;
			}
			else {
				url.append("&");
			}
			Map.Entry entry = (Map.Entry)entries.next();
			String encodedKey = URLEncoder.encode(entry.getKey().toString());
			String encodedValue = (entry.getValue() != null ? URLEncoder.encode(entry.getValue().toString()) : "");
			url.append(new String(encodedKey.getBytes(this.encodingScheme), this.encodingScheme));
			url.append("=");
			url.append(new String(encodedValue.getBytes(this.encodingScheme), this.encodingScheme));
		}
		response.sendRedirect(response.encodeRedirectURL(url.toString()));
	}

	/**
	 * Subclasses can override this method to return name-value pairs for query strings,
	 * which will be URLEncoded and formatted by this class.
	 * This implementation tries to stringify all model elements.
	 */
	protected Map queryProperties(Map model) {
		return model;
	}

}
