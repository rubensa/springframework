/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;

/**
 * Bean definition for beans whose class is defined by their ancestry. PropertyValues
 * defined by the parent will also be "inherited", although it's possible to override
 * them by redefining them in the property values associated with the child.
 * @author Rod Johnson
 * @version $Revision$
 */
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	/**
	 * Create a new ChildBeanDefinition for the given parent.
	 */
	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(pvs);
		this.parentName = parentName;
	}

	/**
	 * Return the name of the parent bean definition in the bean factory.
	 */
	public String getParentName() {
		return parentName;
	}

	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("parentName must be set in ChildBeanDefinition");
		}
	}

	public String toString() {
		return "Child bean with parent '" + getParentName() + "' defined in " + getResourceDescription();
	}

}
