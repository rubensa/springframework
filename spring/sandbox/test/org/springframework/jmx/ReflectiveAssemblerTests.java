/*
 * Created on Jul 5, 2004
 */
package org.springframework.jmx;

import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.reflection.ReflectiveModelMBeanInfoAssembler;


/**
 * @author robh
 */
public class ReflectiveAssemblerTests extends AbstractJmxAssemblerTests {

	protected static final String OBJECT_NAME = "bean:name=testBean1";

	public ReflectiveAssemblerTests(String name) {
		super(name);
	}
	
	protected String getObjectName() {
		return OBJECT_NAME;
	}
	
	protected int getExpectedOperationCount() {
	    return 9;
	}
	
	protected int getExpectedAttributeCount() {
	    return 4;
	}
	
	protected ModelMBeanInfoAssembler getAssembler() {
	    return new ReflectiveModelMBeanInfoAssembler();
	}
}