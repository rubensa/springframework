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

package org.springframework.jmx.assembler;

import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.jmx.AbstractJmxTests;
import org.springframework.jmx.IJmxTestBean;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * @author Rob Harrop
 */
public abstract class AbstractJmxAssemblerTests extends AbstractJmxTests {

	protected abstract String getObjectName();

	public void testMBeanRegistration() throws Exception {
		// beans are registered at this point - just grab them from the server
		ObjectInstance instance = getObjectInstance();
		assertNotNull("Bean should not be null", instance);
	}

	public void testRegisterOperations() throws Exception {
		IJmxTestBean bean = getBean();
		MBeanInfo inf = getMBeanInfo();
		assertEquals("Incorrect number of operations registered",
				getExpectedOperationCount(), inf.getOperations().length);
	}

	public void testRegisterAttributes() throws Exception {
		IJmxTestBean bean = getBean();
		MBeanInfo inf = getMBeanInfo();
		assertEquals("Incorrect number of attributes registered",
				getExpectedAttributeCount(), inf.getAttributes().length);
	}

	private IJmxTestBean getBean() {
		Object bean = getContext().getBean("testBean");
		return (IJmxTestBean) bean;
	}

	public void testGetMBeanInfo() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		assertNotNull("MBeanInfo should not be null", info);
	}

	public void testGetMBeanAttributeInfo() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		MBeanAttributeInfo[] inf = info.getAttributes();
		assertEquals("Invalid number of Attributes returned",
				getExpectedAttributeCount(), inf.length);

		for (int x = 0; x < inf.length; x++) {
			assertNotNull("MBeanAttributeInfo should not be null", inf[x]);
			assertNotNull(
					"Description for MBeanAttributeInfo should not be null",
					inf[x].getDescription());
		}

	}

	public void testGetMBeanOperationInfo() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		MBeanOperationInfo[] inf = info.getOperations();
		assertEquals("Invalid number of Operations returned",
				getExpectedOperationCount(), inf.length);

		for (int x = 0; x < inf.length; x++) {
			assertNotNull("MBeanOperationInfo should not be null", inf[x]);
			assertNotNull(
					"Description for MBeanOperationInfo should not be null",
					inf[x].getDescription());
		}
	}

	public void testDescriptionNotNull() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		assertNotNull("The MBean description should not be null",
				info.getDescription());
	}

	public void testSetAttribute() throws Exception {
		ObjectName objectName = ObjectNameManager.getInstance(getObjectName());
		server.setAttribute(objectName, new Attribute("name", "Rob Harrop"));
		IJmxTestBean bean = (IJmxTestBean) getContext().getBean("testBean");
		assertEquals("Rob Harrop", bean.getName());
	}

	public void testAttributeInfoHasDescriptors() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		ModelMBeanAttributeInfo attr = info.getAttribute("name");
		Descriptor desc = attr.getDescriptor();
		assertNotNull("getMethod field should not be null",
				desc.getFieldValue("getMethod"));
		assertNotNull("setMethod field should not be null",
				desc.getFieldValue("setMethod"));
		assertEquals("getMethod field has incorrect value", "getName",
				desc.getFieldValue("getMethod"));
		assertEquals("setMethod field has incorrect value", "setName",
				desc.getFieldValue("setMethod"));
	}

	public void testAttributeHasCorrespondingOperations() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();

		ModelMBeanOperationInfo get = info.getOperation("getName");
		assertNotNull("get operation should not be null", get);
		assertEquals("get operation should have visibility of four",
				(Integer) get.getDescriptor().getFieldValue("visibility"),
				new Integer(4));
		assertEquals("get operation should have role \"getter\"", "getter", get.getDescriptor().getFieldValue("role"));

		ModelMBeanOperationInfo set = info.getOperation("setName");
		assertNotNull("set operation should not be null", set);
		assertEquals("set operation should have visibility of four",
				(Integer) set.getDescriptor().getFieldValue("visibility"),
				new Integer(4));
		assertEquals("set operation should have role \"setter\"", "setter", set.getDescriptor().getFieldValue("role"));
	}

	protected ModelMBeanInfo getMBeanInfoFromAssembler() throws JMException {
		IJmxTestBean bean = getBean();
		ModelMBeanInfo info = getAssembler().getMBeanInfo("foo", bean.getClass());
		return info;
	}

	protected MBeanInfo getMBeanInfo() throws Exception {
		return server.getMBeanInfo(ObjectNameManager.getInstance(getObjectName()));
	}

	protected ObjectInstance getObjectInstance() throws Exception {
		return server.getObjectInstance(ObjectNameManager.getInstance(getObjectName()));
	}

	protected abstract int getExpectedOperationCount();

	protected abstract int getExpectedAttributeCount();

	protected abstract ModelMBeanInfoAssembler getAssembler();

}
