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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.regex.Pattern;

/**
 * Editor for <code>java.util.regex.Pattern</code>, to directly populate a Pattern property.
 * Expects the same syntax as Pattern's <code>compile</code> method.
 *
 * <p>Since <code>java.util.regex.Pattern</code> is only available on JDK 1.4 or higher,
 * this editor is only available on JDK 1.4 or higher as well.
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see java.util.regex.Pattern
 * @see java.util.regex.Pattern#compile(String)
 */
public class PatternEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		setValue(text != null ? Pattern.compile(text) : null);
	}

	public String getAsText() {
		Pattern value = (Pattern) getValue();
		return (value != null ? value.pattern() : "");
	}

}
