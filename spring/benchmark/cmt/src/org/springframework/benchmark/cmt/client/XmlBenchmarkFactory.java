/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.client;

import java.io.InputStream;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ClasspathBeanDefinitionRegistryLocation;
import org.springframework.beans.factory.xml.XmlBeanFactory;

import org.springframework.benchmark.cmt.server.Benchmark;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class XmlBenchmarkFactory implements BenchmarkFactory {
	
	private String file;
	
	private Benchmark benchmark;

	public void setFile(String file) {
		this.file = file;
		InputStream is = getClass().getResourceAsStream(file);
		BeanFactory bf = new XmlBeanFactory(is, new ClasspathBeanDefinitionRegistryLocation(file));
		this.benchmark = (Benchmark) bf.getBean("benchmark");
	} 

	/**
	 * @see org.springframework.benchmark.cmt.client.BenchmarkCreator#getBenchmark()
	 */
	public Benchmark getBenchmark() {
		return benchmark;
	}
	
	public String toString() {
		return "XmlBenchmarkFactory: file='" + file + "'";
	}

}
