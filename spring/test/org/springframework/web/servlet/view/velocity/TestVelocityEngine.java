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

package org.springframework.web.servlet.view.velocity;

import junit.framework.Assert;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author Juergen Hoeller
 * @since 09.10.2004
 */
public class TestVelocityEngine extends VelocityEngine {

	private Template t;
	private String expectedName;

	public TestVelocityEngine(String expectedName, Template t) {
		this.t = t;
		this.expectedName = expectedName;
	}

	public Template getTemplate(String name) {
		Assert.assertEquals(name, this.expectedName);
		return t;
	}

}
