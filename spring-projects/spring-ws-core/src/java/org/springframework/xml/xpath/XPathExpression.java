/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml.xpath;

import org.w3c.dom.Node;

/**
 * Defines the contract for a precompiled XPath expression. Concrete instances can be obtained through the
 * <code>XPathExpressionFactory</code>.
 *
 * @author Arjen Poutsma
 * @see XPathExpressionFactory
 */
public interface XPathExpression {

    /**
     * Evaluates the given expression as a <code>Node</code>. Returns the evaluation of the expression, or
     * <code>null</code> if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     */
    Node evaluateAsNode(Node node);

    /**
     * Evaluates the given expression as a <code>boolean</code>. Returns the boolean evaluation of the expression, or
     * <code>false</code> if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     */
    boolean evaluateAsBoolean(Node node);

    /**
     * Evaluates the given expression as a number (<code>double</code>). Returns the numeric evaluation of the
     * expression, or <code>Double.NaN</code> if it is invalid.
     *
     * @param node the starting point
     * @return the result of the evaluation
     * @see Double#NaN
     */
    double evaluateAsNumber(Node node);

    /**
     * Evaluates the given expression, and returns the first <code>Node</code> that conforms to it. Returns
     * <code>null</code> if no result could be found.
     *
     * @param node the starting point
     * @return the first <code>Node</code> that is selected by the expression
     */
    String evaluateAsString(Node node);

    /**
     * Evaluates the given expression, and returns all <code>Node</code>s that conform to it. Returns and empty array if
     * no result could be found.
     *
     * @param node the starting point
     * @return the <code>Node</code>s that are selected by the expression
     */
    Node[] evaluateAsNodes(Node node);
}
