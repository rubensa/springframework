/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.LifecycleBean;

/**
 * Simple bean used to check constructor dependency checking.
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class ConstructorDependenciesBean {
	
	private int age;
	
	private String name;
	
	private TestBean spouse1;

	private TestBean spouse2;

	private LifecycleBean other;

	public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, LifecycleBean other) {
		this.spouse1 = spouse1;
		this.spouse2 = spouse2;
		this.other = other;
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}

	public TestBean getSpouse1() {
		return spouse1;
	}

	public TestBean getSpouse2() {
		return spouse2;
	}

	public LifecycleBean getOther() {
		return other;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setName(String name) {
		this.name = name;
	}

}
