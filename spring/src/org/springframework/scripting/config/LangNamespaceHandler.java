/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.scripting.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.scripting.bsh.BshScriptFactory;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.jruby.JRubyScriptFactory;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class LangNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerScriptBeanDefinitionParser("groovy", GroovyScriptFactory.class);
		registerScriptBeanDefinitionParser("jruby", JRubyScriptFactory.class);
		registerScriptBeanDefinitionParser("bsh", BshScriptFactory.class);
	}

	private void registerScriptBeanDefinitionParser(String key, Class scriptFactoryClass) {
		registerBeanDefinitionParser(key, new ScriptBeanDefinitionParser(scriptFactoryClass));
	}

}
