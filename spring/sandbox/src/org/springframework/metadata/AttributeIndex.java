/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata;


/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public interface AttributeIndex {
	
	/**
	 * 
	 * @param attributeClass
	 * @return classes with this attribute. Never returns null.
	 */
	public Class[] getClassesWithAttribute (Class attributeClass);

}
