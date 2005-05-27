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

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Metadata auto proxy test that sources attributes from source-level metadata.
 *
 * @author Rod Johnson
 */
public class DummyAttributesMetadataAutoProxyTests extends AbstractMetadataAutoProxyTests {

	protected BeanFactory getBeanFactory() throws IOException {
		// Load from class path.
		return new ClassPathXmlApplicationContext(
				new String[] {
					"/org/springframework/aop/framework/autoproxy/metadata/dummyAttributes.xml",
					"/org/springframework/aop/framework/autoproxy/metadata/enterpriseServices.xml"});
	}

}
