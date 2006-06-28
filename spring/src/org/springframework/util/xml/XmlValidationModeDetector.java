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


import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Rob Harrop
 */
public class XmlValidationModeDetector {

	/**
	 * Indicates that DTD validation should be used.
	 */
	public static final int VALIDATION_DTD = 2;

	/**
	 * Indicates that XSD validation should be used.
	 */
	public static final int VALIDATION_XSD = 3;

	/**
	 * The token in a XML document that declares the DTD to use for validation
	 * and thus that DTD validation is being used.
	 */
	private static final String DOCTYPE = "DOCTYPE";

	/**
	 * The token that indicates the start of an XML comment.
	 */
	private static final String START_COMMENT = "<!--";

	/**
	 * The token that indicates the end of an XML comment.
	 */
	private static final String END_COMMENT = "-->";

	/**
	 * Indicates whether or not the current parse position is inside an XML comment.
	 */
	private boolean inComment;

	/**
	 * Detects the validation mode for the XML document in the supplied {@link InputStream}.
	 * Note that the supplied {@link InputStream} is closed by this method before returning.
	 * @see #VALIDATION_DTD
	 * @see #VALIDATION_XSD
	 */
	public int detectValidationMode(InputStream inputStream) throws IOException {
		// Peek into the file to look for DOCTYPE.
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			boolean isDtdValidated = false;
			String content;
			while ((content = reader.readLine()) != null) {
				content = consumeCommentTokens(content);
				if (inComment || !StringUtils.hasText(content)) {
					continue;
				}

				if (hasDoctype(content)) {
					isDtdValidated = true;
					break;
				}
				else if (hasOpeningTag(content)) {
					// End of meaningful data
					break;
				}
			}
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
		finally {
			reader.close();
		}
	}

	private boolean hasDoctype(String content) {
		return content.indexOf(DOCTYPE) > -1;
	}

	/**
	 * Does the supplied content contain an XML opening tag. If the parse state is currently
	 * in an XML comment then this method always returns false. It is expected that all comment
	 * tokens will have consumed for the supplied content before passing the remainder to this method.
	 */
	private boolean hasOpeningTag(String content) {
		if (inComment) {
			return false;
		}
		int openTagIndex = content.indexOf('<');
		return (openTagIndex > -1 && content.length() > openTagIndex && Character.isLetter(content.charAt(openTagIndex + 1)));
	}

	/**
	 * Consumes all the leading comment data in the given String and returns the remaining content, which
	 * may be empty since the supplied content might be all comment data. For our purposes it is only important
	 * to strip leading comment content on a line since the first piece of non comment content will be either
	 * the DOCTYPE declaration or the root element of the document.
	 */
	private String consumeCommentTokens(String line) {
		if(line.indexOf(START_COMMENT) == -1 && line.indexOf(END_COMMENT) == -1) {
			return line;
		}

		while ((line = consume(line)) != null) {
			if(!inComment && !line.trim().startsWith(START_COMMENT)) {
				return line;
			}
		}
		return line;
	}

	/**
	 * Consume
	 * @param line
	 * @return
	 */
	private String consume(String line) {
		int index = (inComment ? endComment(line) : startComment(line));
		return (index == -1 ? null : line.substring(index));
	}

	public boolean isInComment() {
		return inComment;
	}

	private int startComment(String line) {
		return commentToken(line, START_COMMENT, true);

	}

	private int endComment(String line) {
		return commentToken(line, END_COMMENT, false);
	}

	private int commentToken(String line, String token, boolean inCommentIfPresent) {
		int index = line.indexOf(token);
		if (index > - 1) {
			inComment = inCommentIfPresent;
		}
		return (index == -1 ? index : index + token.length());
	}
}
