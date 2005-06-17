package org.springframework.aop;

import org.springframework.beans.BeansStub;

public class AopStub {
	
	public static void hello() {
		BeansStub.hello();
		System.out.println("hello from: " + AopStub.class.getName());
	}
}
