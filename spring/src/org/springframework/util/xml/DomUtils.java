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

package org.springframework.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Convenience methods for working with the DOM API,
 * in particular for working with DOM Nodes and DOM Elements.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.2
 * @see org.w3c.dom.Node
 * @see org.w3c.dom.Element
 */
public abstract class DomUtils {

	/**
	 * Retrieve all child elements of the given DOM element that match
	 * the given element name. Only look at the direct child level of the
	 * given element; do not go into further depth (in contrast to the
	 * DOM API's <code>getElementsByTagName</code> method).
	 * @param ele the DOM element to analyze
	 * @param childEleName the child element name to look for
	 * @return a List of child <code>org.w3c.dom.Element</code> instances
	 * @see org.w3c.dom.Element
	 * @see org.w3c.dom.Element#getElementsByTagName
	 */
	public static List getChildElementsByTagName(Element ele, String childEleName) {
		NodeList nl = ele.getChildNodes();
		List childEles = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && nodeNameEquals(node, childEleName)) {
				childEles.add(node);
			}
		}
		return childEles;
	}

	/**
	 * Extract the text value from the given DOM element,
	 * ignoring XML comments.
	 * <p>Appends all CharacterData nodes and EntityReference nodes
	 * into a single String value, excluding Comment nodes.
	 * @see CharacterData
	 * @see EntityReference
	 * @see Comment
	 */
	public static String getTextValue(Element valueEle) {
		StringBuffer value = new StringBuffer();
		NodeList nl = valueEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if ((item instanceof CharacterData && !(item instanceof Comment)) ||
					item instanceof EntityReference) {
				value.append(item.getNodeValue());
			}
		}
		return value.toString();
	}

	/**
	 * Namespace-aware equals comparison. Returns <code>true</code> if either
	 * {@link Node#getLocalName} or {@link Node#getNodeName} equals <code>desiredName</code>,
	 * otherwise returns <code>false</code>.
	 */
	public static boolean nodeNameEquals(Node node, String desiredName) {
		Assert.notNull(node, "'node' cannot be null.");
		Assert.notNull(desiredName, "'desiredName' cannot be null.");
		return desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName());
	}
}
