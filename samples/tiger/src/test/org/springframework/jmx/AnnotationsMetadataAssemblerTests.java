package org.springframework.jmx;

import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.annotations.AnnotationsJmxAttributeSource;

/**
 * @author robh
 */
public class AnnotationsMetadataAssemblerTests extends AbstractMetadataAssemblerTests {

    private static final String OBJECT_NAME = "bean:name=testBean4";

    public AnnotationsMetadataAssemblerTests(String s) {
        super(s);
    }

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
