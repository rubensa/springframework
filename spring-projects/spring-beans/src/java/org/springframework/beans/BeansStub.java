package org.springframework.beans;

import org.springframework.core.CoreStub;

public class BeansStub {

	public static void hello() {
		CoreStub.hello();
		System.out.println("hello from: " + BeansStub.class.getName());
	}
}
