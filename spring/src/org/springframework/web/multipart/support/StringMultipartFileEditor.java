package org.springframework.web.multipart.support;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartFile;

/**
 * Custom PropertyEditor for converting MultipartFiles to Strings.
 * Allows to specify the charset to use.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class StringMultipartFileEditor extends PropertyEditorSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private String charsetName;

	/**
	 * Create a new StringMultipartFileEditor, using the default charset.
	 */
	public StringMultipartFileEditor() {
	}

	/**
	 * Create a new StringMultipartFileEditor, using the given charset.
	 * @param charsetName valid charset name
	 * @see java.lang.String#String(byte[],String)
	 */
	public StringMultipartFileEditor(String charsetName) {
		this.charsetName = charsetName;
	}

	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) value;
			try {
				super.setValue(this.charsetName != null ?
											 new String(multipartFile.getBytes(), this.charsetName) :
											 new String(multipartFile.getBytes()));
			}
			catch (IOException ex) {
				logger.error("Cannot read contents of multipart file", ex);
				throw new IllegalArgumentException("Cannot read contents of multipart file: " + ex.getMessage());
			}
		}
	}

}
