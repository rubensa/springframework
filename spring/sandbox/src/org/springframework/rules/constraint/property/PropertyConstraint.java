/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.constraint.property;

import org.springframework.util.closure.Constraint;

/**
 * A predicate that constrains a bean property in some way.
 *
 * @author Keith Donald
 */
public interface PropertyConstraint extends Constraint {

	/**
	 * Returns the constrained property name.
	 *
	 * @return The property name
	 */
	public String getPropertyName();
}