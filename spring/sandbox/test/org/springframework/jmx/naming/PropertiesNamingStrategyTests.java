/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.naming;

import java.util.Properties;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class PropertiesNamingStrategyTests extends AbstractNamingStrategyTests {

	private static final String OBJECT_NAME = "bean:name=namingTest";

	protected ObjectNamingStrategy getStrategy() throws Exception {
		KeyNamingStrategy strat = new KeyNamingStrategy();
		Properties mappings = new Properties();
		mappings.setProperty("namingTest", "bean:name=namingTest");
		strat.setMappings(mappings);
		strat.afterPropertiesSet();
		return strat;
	}

	protected Object getManagedResource() {
		return new Object();
	}

	protected String getKey() {
		return "namingTest";
	}

	protected String getCorrectObjectName() {
		return OBJECT_NAME;
	}

}
