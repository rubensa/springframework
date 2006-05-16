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

package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * @author Rob Harrop
 */
public class MapBasedReaderEventListener implements ReaderEventListener {

	private final Map componentDefinitions = new HashMap();

	private final Map aliasMap = new HashMap();

	private final List imports = new ArrayList();

	public void componentRegistered(ComponentDefinition componentDefinition) {
		this.componentDefinitions.put(componentDefinition.getName(), componentDefinition);
	}

	public ComponentDefinition getComponentDefinition(String name) {
		return (ComponentDefinition) this.componentDefinitions.get(name);
	}

	public ComponentDefinition[] getComponentDefinitions() {
		Collection collection = this.componentDefinitions.values();
		return (ComponentDefinition[]) collection.toArray(new ComponentDefinition[collection.size()]);
	}

	public void aliasRegistered(String targetBeanName, String alias) {
		List aliases = (List) this.aliasMap.get(targetBeanName);
		if(aliases == null) {
			aliases = new ArrayList();
			this.aliasMap.put(targetBeanName, aliases);
		}
		aliases.add(alias);
	}

	public List getAliases(String beanName) {
		List aliases = (List) this.aliasMap.get(beanName);
		return aliases == null ? null : Collections.unmodifiableList(aliases);
	}

	public void importProcessed(String importedResource) {
		this.imports.add(importedResource);
	}

	public List getImports() {
		return Collections.unmodifiableList(this.imports);
	}
}
