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

package org.springframework.jmx.export.assembler;

import java.util.Properties;

import javax.management.MBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * @author Rob Harrop
 */
public class MethodNameBasedMBeanInfoAssemblerMappedTests extends AbstractJmxAssemblerTests {

	protected static final String OBJECT_NAME = "bean:name=testBean4";

	public void testGetAgeIsReadOnly() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = info.getAttribute("age");

		assertTrue("Age is not readable", attr.isReadable());
		assertFalse("Age is not writable", attr.isWritable());
	}

	public void testWithFallThrough() throws Exception {
		MethodNameBasedMBeanInfoAssembler assembler =
				getWithMapping("foobar", "add,myOperation,getName,setName,getAge");
		assembler.setManagedMethods(new String[] {"getNickName", "setNickName"});

		ModelMBeanInfo inf = assembler.getMBeanInfo(getObjectName(), getBean().getClass());
		MBeanAttributeInfo attr = inf.getAttribute("nickName");

		assertNickName(attr);
	}

	public void testNickNameIsExposed() throws Exception {
		ModelMBeanInfo inf = (ModelMBeanInfo) getMBeanInfo();
		MBeanAttributeInfo attr = inf.getAttribute("nickName");

		assertNickName(attr);
	}

	protected String getObjectName() {
		return OBJECT_NAME;
	}

	protected int getExpectedOperationCount() {
		return 7;
	}

	protected int getExpectedAttributeCount() {
		return 3;
	}

	protected MBeanInfoAssembler getAssembler() throws Exception {
		return getWithMapping("getNickName,setNickName,add,myOperation,getName,setName,getAge");
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/export/assembler/methodNameAssemblerMapped.xml";
	}

	private MethodNameBasedMBeanInfoAssembler getWithMapping(String mapping) {
		return getWithMapping(OBJECT_NAME, mapping);
	}

	private MethodNameBasedMBeanInfoAssembler getWithMapping(String name, String mapping) {
		MethodNameBasedMBeanInfoAssembler assembler = new MethodNameBasedMBeanInfoAssembler();
		Properties props = new Properties();
		props.setProperty(name, mapping);
		assembler.setMethodMappings(props);
		return assembler;
	}

	private void assertNickName(MBeanAttributeInfo attr) {
		assertNotNull("Nick Name should not be null", attr);
		assertTrue("Nick Name should be writable", attr.isWritable());
		assertTrue("Nick Name should be readab;e", attr.isReadable());
	}

}
