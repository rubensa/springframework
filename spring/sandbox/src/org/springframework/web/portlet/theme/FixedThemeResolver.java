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

package org.springframework.web.portlet.theme;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


/**
 * Implementation of ThemeResolver that simply uses a fixed theme.
 * The fixed name can be defined via the defaultTheme property.
 *
 * <p>Note: Does not support setThemeName, as the theme is fixed.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see #setDefaultThemeName
 */
public class FixedThemeResolver extends AbstractThemeResolver {

	public String resolveThemeName(RenderRequest request) {
		return getDefaultThemeName();
	}

	public void setThemeName(RenderRequest request, RenderResponse response, String themeName) {
		throw new IllegalArgumentException("Cannot change theme - use a different theme resolution strategy");
	}

}
