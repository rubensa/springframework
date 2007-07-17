/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jmx.export.metadata;

/**
 * Metadata that indicates to expose a given bean property as JMX attribute.
 * Only valid when used on a JavaBean getter or setter.
 *
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class ManagedAttribute extends AbstractJmxAttribute {

	public static final ManagedAttribute EMPTY = new ManagedAttribute();


	private Object defaultValue;

	private String persistPolicy;

	private int persistPeriod = -1;


	/**
	 * Set the default value of this attribute.
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Return the default value of this attribute.
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	public void setPersistPolicy(String persistPolicy) {
		this.persistPolicy = persistPolicy;
	}

	public String getPersistPolicy() {
		return this.persistPolicy;
	}

	public void setPersistPeriod(int persistPeriod) {
		this.persistPeriod = persistPeriod;
	}

	public int getPersistPeriod() {
		return this.persistPeriod;
	}

}
