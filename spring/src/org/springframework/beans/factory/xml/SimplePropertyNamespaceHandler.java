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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.Conventions;

/**
 * Simple <code>NamespaceHandler</code> implementation that maps custom attributes
 * directly through to bean properties. An important point to note is that this
 * <code>NamespaceHandler</code> does not have a corresponding schema since there
 * is no way to know in advance all possible attribute names.
 *
 * <p>An example of the usage of this <code>NamespaceHandler</code> is shown below:
 *
 * <pre class="code">
 * &lt;bean id=&quot;rob&quot; class=&quot;..TestBean&quot; p:name=&quot;Rob Harrop&quot; p:spouse-ref=&quot;sally&quot;/&gt;</pre>
 *
 * Here the '<code>p:name</code>' corresponds directly to the '<code>name</code>'
 * property on class '<code>TestBean</code>'. The '<code>p:spouse-ref</code>'
 * attributes corresponds to the '<code>spouse</code>' property and, rather
 * than being the concrete value, it contains the name of the bean that will
 * be injected into that property.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class SimplePropertyNamespaceHandler implements NamespaceHandler {

	private static final String REF_SUFFIX = "-ref";


	public void init() {
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		parserContext.getReaderContext().error(
				"Class [" + getClass().getName() + "] does not support custom elements.", element);
		return null;
	}

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		if (node instanceof Attr) {
			Attr attr = (Attr) node;
			String propertyName = attr.getLocalName();
			String propertyValue = attr.getValue();
			MutablePropertyValues pvs = definition.getBeanDefinition().getPropertyValues();
			if (pvs.contains(propertyName)) {
				parserContext.getReaderContext().error("Property '" + propertyName + "' is already defined using " +
						"both <property> and inline syntax. Only one approach may be used per property.", attr);
			}
			if (propertyName.endsWith(REF_SUFFIX)) {
				propertyName = propertyName.substring(0, propertyName.length() - REF_SUFFIX.length());
				pvs.addPropertyValue(
						Conventions.attributeNameToPropertyName(propertyName), new RuntimeBeanReference(propertyValue));
			}
			else {
				pvs.addPropertyValue(Conventions.attributeNameToPropertyName(propertyName), propertyValue);
			}
		}
		return definition;
	}

}
