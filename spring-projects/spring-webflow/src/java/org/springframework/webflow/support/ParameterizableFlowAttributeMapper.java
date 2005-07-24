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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;

/**
 * Generic flow attribute mapper implementation that allows mappings to be
 * configured in a declarative fashion.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>inputAttribute(s)</td>
 * <td><i>null</i></td>
 * <td>Sets the name of input attributes in flow scope to map to the subflow.</td>
 * </tr>
 * <tr>
 * <td>inputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributeMapper strategy responsible for mapping starting
 * subflow input attributes from a suspending parent flow.</td>
 * </tr>
 * <tr>
 * <td>inputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent
 * flow to a newly spawned sub flow. Each list item must be a String, a
 * Map or a List. If the list item is a simple String value, the attribute
 * will be mapped as having the same name in the parent flow and in the sub
 * flow. If the list item is a Map, each map entry must be a String key
 * naming the attribute in the parent flow, and a String value naming the
 * attribute in the child flow. If the list item is itself a List, then that
 * list is evaluated recursively, and must contain Strings, Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>inputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent
 * flow to a newly spawned sub flow. The keys in given map are the names
 * of entries in the parent model that will be mapped. The value associated
 * with a key is the name of the target entry that will be placed in the
 * subflow model.</td>
 * </tr>
 * <tr>
 * <td>outputAttribute(s)</td>
 * <td><i>null</i></td>
 * <td>ets the name of output attributes in flow scope to map to the parent flow.</td>
 * </tr>
 * <tr>
 * <td>outputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributeMapper strategy responsible for mapping ending subflow
 * output attributes to a resuming parent flow as output.</td>
 * </tr>
 * <tr>
 * <td>outputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to
 * the parent flow (once the subflow ends and the parent flow resumes).
 * Each list item must be a String, a List or a Map. If the list item is
 * a simple String value, the attribute will be mapped as having the same
 * name in the parent flow and in the child flow. If the list item is a Map,
 * each map entry must be a String key naming the attribute in the sub flow,
 * and a String value naming the attribute in the parent flow. If the list
 * item is itself a List, then that list is evaluated recursively,
 * and must contain Strings, Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>outputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). The keys
 * in given map are the names of entries in the subflow model that will be mapped.
 * The value associated with a key is the name of the target entry that will
 * be placed in the parent flow model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * When the <i>from</i> mapping string is enclosed in "${...}", it will be
 * interpreted as an expression that will be evaluated against the request execution
 * context.
 * 
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableFlowAttributeMapper implements FlowAttributeMapper, Serializable {

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());;

	private AttributeMapper inputMapper = new FlowScopeAwareParameterizableAttributeMapper();

	private AttributeMapper outputMapper = new FlowScopeAwareParameterizableAttributeMapper();

	/**
	 * Sets the name of an input attribute in flow scope to map to the subflow.
	 * @param attributeName the attributeName
	 */
	public void setInputAttribute(String attributeName) {
		setInputMappings(Collections.singletonList(attributeName));
	}

	/**
	 * Sets the name of input attributes in flow scope to map to the subflow.
	 * @param attributeNames the attribute names
	 */
	public void setInputAttributes(String[] attributeNames) {
		setInputMappings(Arrays.asList(attributeNames));
	}

	/**
	 * Set the input mapping to use when mapping properties in the request context
	 * to the sub flow scope. This is a convenience method for setting a single mapping.
	 * @param mapping the mapping object
	 */
	public void setInputMapping(Mapping mapping) {
		setInputMappings(Collections.singletonList(mapping));
	}

	/**
	 * Set the mappings that will be executed when mapping model data to the
	 * sub flow. This method is provided as a configuration convenience.
	 * @param inputMappings the input mappings
	 * @see #setInputMappings(Collection) with a Collection containing one
	 *      item which is a Map. Each map entry must be a String key naming
	 *      the attribute in the parent flow, and a String value naming the
	 *      attribute in the child flow.
	 */
	public void setInputMappingsMap(Map inputMappings) {
		setInputMappings(Collections.singletonList(inputMappings));
	}

	/**
	 * Set the mappings that will be executed when mapping properties in
	 * a parent flow to a sub flow scope. Each list item must be
	 * a String, a Mapping, a List, or a Map. If the list item is a simple
	 * String value, the attribute will be mapped as having the same name
	 * in the parent flow and in the sub flow. If the list item is a Map,
	 * each map entry must be a String key naming the attribute in the
	 * parent flow, and a String value naming the attribute in the child flow.
	 * If the list item is itself a List, then that list is evaluated
	 * recursively, and must itself contain Strings, Lists or Maps.
	 * @param inputMappings the input mappings
	 */
	public void setInputMappings(Collection inputMappings) {
		setInputMapper(new FlowScopeAwareParameterizableAttributeMapper(inputMappings));
	}

	/**
	 * Set the AttributesMapper strategy responsible for mapping starting
	 * subflow input attributes from a suspending parent flow.
	 * @param mapper the mapper
	 */
	public void setInputMapper(AttributeMapper mapper) {
		this.inputMapper = mapper;
	}

	/**
	 * Sets the name of an output attribute in flow scope to map up to the parent flow.
	 * @param attributeName the attributeName
	 */
	public void setOutputAttribute(String attributeName) {
		setOutputMappings(Collections.singletonList(attributeName));
	}

	/**
	 * Sets the name of output attributes in flow scope to map to the parent flow.
	 * @param attributeNames the attribute names
	 */
	public void setOutputAttributes(String[] attributeNames) {
		setOutputMappings(Arrays.asList(attributeNames));
	}

	/**
	 * Set the output mapping to use when mapping properties in sub flow
	 * back to a resuming parent flow. This is a convenience method for
	 * setting a single mapping.
	 * @param mapping the mapping object
	 */
	public void setOutputMapping(Mapping mapping) {
		setOutputMappings(Collections.singletonList(mapping));
	}
	
	/**
	 * Set the mappings that will be executed when mapping model data from the
	 * sub flow. This method is provided as a configuration convenience.
	 * @param outputMappings the output mappings
	 * @see #setOutputMappings(Collection) with a Collection containing one
	 *      item which is a Map. Each map entry must be a String key naming the
	 *      attribute in the sub flow, and a String value naming the attribute
	 *      in the parent flow.
	 */
	public void setOutputMappingsMap(Map outputMappings) {
		setOutputMappings(Collections.singletonList(outputMappings));
	}

	/**
	 * Set the mappings that will be executed when mapping model data from
	 * the sub flow. Each list item must be a String, Mapping, a List, or a
	 * Map. If the list item is a simple String value, the attribute will be
	 * mapped as having the same name in the parent flow and in the child flow.
	 * If the list item is a Map, each map entry must be a String key naming
	 * the attribute in the sub flow, and a String value naming the attribute
	 * in the parent flow. If the list item is itself a List, then that list
	 * is evaluated recursively, and must itself contain Strings,
	 * Mappings, Lists, or Maps.
	 * @param outputMappings the output mappings
	 */
	public void setOutputMappings(Collection outputMappings) {
		setOutputMapper(new FlowScopeAwareParameterizableAttributeMapper(outputMappings));
	}

	/**
	 * Set the AttributesMapper strategy responsible for mapping ending subflow
	 * output attributes to a resuming parent flow as output.
	 * @param mapper the mapper
	 */
	public void setOutputMapper(AttributeMapper mapper) {
		this.outputMapper = mapper;
	}

	public Map createSubflowInput(RequestContext context) {
		if (this.inputMapper != null) {
			Map input = new HashMap();
			// map from request context to input map
			this.inputMapper.map(context, input, getMappingContext(context));
			return input;
		}
		else {
			// an empty, but modifyable map
			return new HashMap();
		}
	}

	public void mapSubflowOutput(RequestContext context) {
		if (this.outputMapper != null) {
			// map from request context to parent flow scope
			this.outputMapper.map(context,
					context.getFlowExecutionContext().getActiveSession().getParent().getScope(),
					getMappingContext(context));
		}
	}
	
	/**
	 * Returns a map of contextual data available during mapping.
	 */
	protected Map getMappingContext(RequestContext context) {
		return new HashMap();
	}

	public String toString() {
		return new ToStringCreator(this).append("inputMapper", inputMapper).
			append("outputMapper", outputMapper).toString();
	}

	/**
	 * Attribute mapper specialization that knows if an "attribute name" is provided, and not a
	 * value ${expression}, that the name should be treated as a flow scope expression.
	 * This is needed because all <i>from</i> expressions will be evaluated agains the request
	 * context. Expressions need to explicitly handle this (e.g. "${flowScope.bean.prop}"), but
	 * simple names should automatically evaluate against the flow scope ("bean.prop").
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public static class FlowScopeAwareParameterizableAttributeMapper extends ParameterizableAttributeMapper {

		/**
		 * Create a new flow scope aware attribute mapper.
		 */
		public FlowScopeAwareParameterizableAttributeMapper() {
			super();
		}

		/**
		 * Create a new flow scope aware attribute mapper wrapping given collection
		 * of mappings.
		 * @param mappings the mappings to make "flow scope aware"
		 */
		public FlowScopeAwareParameterizableAttributeMapper(Collection mappings) {
			super(mappings);
		}
		
		public void addMapping(String sourceExpression, String targetExpression) {
			if (ExpressionFactory.isParseableExpression(sourceExpression)) {
				// use expression "as is"
				addMapping(new Mapping(sourceExpression, targetExpression));
			}
			else {
				// use flow scope aware mapping for "from" expression
				addMapping(
					new Mapping(
						new FlowScopeExpression(ExpressionFactory.parseExpression(sourceExpression)),
						ExpressionFactory.parsePropertyExpression(targetExpression)));
			}
		}
	}
}