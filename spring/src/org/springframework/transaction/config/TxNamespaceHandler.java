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

package org.springframework.transaction.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <code>NamespaceHandler</code> allowing for the configuration of
 * declarative transaction management using either XML or using annotations.
 *
 * <p>This namespace handler is the central piece of functionality in the
 * Spring 2 transaction management facilities and offers two appraoched
 * to declaratively manage transactions.
 *
 * <p>One approach uses transaction semantics defined in XML using the
 * <code>&lt;tx:advice&gt;</code> elements, the other uses annotations
 * in combination with the <code>&lt;tx:annotation-driven&gt;</code> element.
 * Both approached are detailed to great extent in the Spring reference manual.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class TxNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("advice", new TxAdviceBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		registerBeanDefinitionParser("jta-transaction-manager", new JtaTransactionManagerBeanDefinitionParser());
	}

}
