/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Metadata auto proxy creator test that sources attributes 
 * using Jakarta Commons Attributes.
 * <br>This test file ends with Test, rather than Tests or TestSuite,
 * to ensure that it isn't run as part of the Spring Ant build process.
 * <b>Requires source-level metadata compilation.</b>
 * <br>See the commonsBuild.xml Ant build script.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id$
 */
public class CommonsAttributesMetadataAutoProxyTest extends AbstractMetadataAutoProxyTests {
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public CommonsAttributesMetadataAutoProxyTest(String arg0) {
		super(arg0);
	}
	
	
	protected BeanFactory getBeanFactory() throws IOException {
		// Load from classpath, NOT a file path
		BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
					"/org/springframework/aop/framework/autoproxy/metadata/commonsAttributes.xml",
					"/org/springframework/aop/framework/autoproxy/metadata/enterpriseServices.xml"});
		return bf;
	}
	
	
}
