
package org.springframework.jmx;

import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;

import org.springframework.jmx.assembler.AbstractMetadataAssemblerTests;
import org.springframework.jmx.metadata.JmxAttributeSource;
import org.springframework.jmx.metadata.support.annotations.AnnotationsJmxAttributeSource;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * @author robh
 */
public class AnnotationsMetadataAssemblerTests extends AbstractMetadataAssemblerTests {

	private static final String OBJECT_NAME = "bean:name=testBean4";

	private static final String SERVICE_OBJECT_NAME = "spring:service=foo";

	protected JmxAttributeSource getAttributeSource() {
		return new AnnotationsJmxAttributeSource();
	}

	protected String getObjectName() {
		return OBJECT_NAME;
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/annotations.xml";
	}
}
