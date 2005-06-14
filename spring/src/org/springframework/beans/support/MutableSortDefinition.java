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

package org.springframework.beans.support;

import java.io.Serializable;

/**
 * Mutable implementation of SortDefinition.
 * Supports toggling the ascending value on setting the same property again.
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 26.05.2003
 * @see #setToggleAscendingOnProperty
 */
public class MutableSortDefinition implements SortDefinition, Serializable {

	private String property = "";

	private boolean ignoreCase = true;

	private boolean ascending = true;

	private boolean toggleAscendingOnProperty = false;


	/**
	 * Create an empty MutableSortDefinition,
	 * to be populated via its bean properties.
	 * @see #setProperty
	 * @see #setIgnoreCase
	 * @see #setAscending
	 */
	public MutableSortDefinition() {
	}

	/**
	 * Copy constructor: create a new MutableSortDefinition
	 * that mirrors the given sort definition.
	 * @param source the original sort definition
	 */
	public MutableSortDefinition(SortDefinition source) {
		this.property = source.getProperty();
		this.ignoreCase = source.isIgnoreCase();
		this.ascending = source.isAscending();
	}

	/**
	 * Create a MutableSortDefinition for the given settings.
	 * @param property the property to compare
	 * @param ignoreCase whether upper and lower case in String values should be ignored
	 * @param ascending whether to sort ascending (true) or descending (false)
	 */
	public MutableSortDefinition(String property, boolean ignoreCase, boolean ascending) {
		this.property = property;
		this.ignoreCase = ignoreCase;
		this.ascending = ascending;
	}

	public MutableSortDefinition(boolean toggleAscendingOnSameProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnSameProperty;
	}


	/**
	 * Set the property to compare.
	 * <p>If the property was the same as the current, the sort is reversed if
	 * "toggleAscendingOnProperty" is activated, else simply ignored.
	 * @see #setToggleAscendingOnProperty
	 */
	public void setProperty(String property) {
		if (property == null || "".equals(property)) {
			this.property = "";
		}
		else {
			// implicit toggling of ascending?
			if (this.toggleAscendingOnProperty) {
				if (property.equals(this.property)) {
					this.ascending = !this.ascending;
				}
				else {
					this.ascending = true;
				}
			}
			this.property = property;
		}
	}

	public String getProperty() {
		return property;
	}

	/**
	 * Set whether upper and lower case in String values should be ignored.
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * Set whether to sort ascending (true) or descending (false).
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setToggleAscendingOnProperty(boolean toggleAscendingOnProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnProperty;
	}

	public boolean isToggleAscendingOnProperty() {
		return toggleAscendingOnProperty;
	}


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SortDefinition)) {
			return false;
		}
		SortDefinition otherSd = (SortDefinition) other;
		return (getProperty().equals(otherSd.getProperty()) &&
		    isAscending() == otherSd.isAscending() && isIgnoreCase() == otherSd.isIgnoreCase());
	}

	public int hashCode() {
		int hashCode = this.property.hashCode();
		hashCode = 29 * hashCode + (this.ignoreCase ? 1 : 0);
		hashCode = 29 * hashCode + (this.ascending ? 1 : 0);
		return hashCode;
	}

}
