
package org.springframework.jmx;

import org.springframework.jmx.metadata.JmxAttributeSource;
import org.springframework.jmx.metadata.support.annotations.AnnotationsJmxAttributeSource;

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
