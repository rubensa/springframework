/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.access;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Unit test for DefaultBeanFactoryReference
 * 
 * @author Colin Sampaleanu
 */
public class DefaultBeanFactoryReferenceTests extends TestCase {

	public void testAllOperations() {
		MockControl control = MockControl.createControl(ConfigurableBeanFactory.class);
		ConfigurableBeanFactory bf = (ConfigurableBeanFactory) control.getMock();

		bf.destroySingletons();
		control.replay();

		DefaultBeanFactoryReference bfr = new DefaultBeanFactoryReference(bf);

		assertNotNull(bfr.getFactory());
		bfr.release();

		try {
			bfr.getFactory();
		}
		catch (IllegalStateException e) {
			// expected
		}

		control.verify();
	}
}
