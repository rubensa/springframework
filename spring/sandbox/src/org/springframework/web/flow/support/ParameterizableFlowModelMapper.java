/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.web.flow.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.FlowModel;
import org.springframework.web.flow.FlowModelMapper;
import org.springframework.web.flow.MutableFlowModel;

/**
 * Generic flow model mapper implementation that allows mappings to be
 * configured programatically or in a Spring application context.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td>inputMappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from the parent flow
 * to a newly spawned sub flow. The provided list contains the names of the
 * attributes in the parent to pass to the subflow for access. The same name is
 * used in both parent flow and sub flow model.</td>
 * </tr>
 * <tr>
 * <td>inputMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from the parent flow
 * to a newly spawned sub flow. The keys in given map are the names of entries
 * in the parent model that will be mapped. The value associated with a key is
 * the name of the target entry that will be placed in the subflow model.</td>
 * </tr>
 * <tr>
 * <td>outputMappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow <i>output </i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). The provided
 * list contains the names of the attributes in the subflow to pass to the
 * parent for access. The same name is used in both parent flow and sub flow
 * model.</td>
 * </tr>
 * <tr>
 * <td>outputMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow <i>output </i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). The keys in
 * given map are the names of entries in the subflow model that will be mapped.
 * The value associated with a key is the name of the target entry that will be
 * placed in the parent flow model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * </p>
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableFlowModelMapper implements FlowModelMapper, Serializable {

	protected final Log logger = LogFactory.getLog(getClass());;

	private Map inputMappings = Collections.EMPTY_MAP;

	private Map outputMappings = Collections.EMPTY_MAP;

	private boolean mapMissingAttributesToNull = false;

	/**
	 * Set the mappings that will be executed when mapping model data to a sub
	 * flow. Each list item must be a String, a List, or a Map. If the list item
	 * is a simple String value, the attribute will be mapped as having the same
	 * name in the parent flow and in the sub flow. If the list item is a Map,
	 * each map entry must be a String key naming the attribute in the parent
	 * flow, and a String value naming the attribute in the child flow. If the
	 * list item is itself a List, then that list is itself evaluated
	 * recursively, and must itself contain Strings, Lists, or Maps.
	 * <p>
	 * Note: only <strong>one </strong> of setInputMappings or
	 * setInputMappingsMap must be called.
	 * @param inputMappings The input mappings
	 */
	public void setInputMappings(Collection inputMappings) {
		this.inputMappings = new HashMap();
		putCollectionMappings(this.inputMappings, inputMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data to the sub
	 * flow. This is essentially just a short form of calling
	 * <p>
	 * Note: only <strong>one </strong> of setInputMappings or
	 * setInputMappingsMap must be called.
	 * @link ParameterizableFlowModelMapper#setInputMappings(List) with a
	 *       List containing one item which is a Map. Each map entry must be a
	 *       String key naming the attribute in the parent flow, and a String
	 *       value naming the attribute in the child flow.
	 * @param inputMappings The input mappings
	 */
	public void setInputMappingsMap(Map inputMappings) {
		this.inputMappings = new HashMap(inputMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from the
	 * sub flow. Each list item must be a String, a List, or a Map. If the list
	 * item is a simple String value, the attribute will be mapped as having the
	 * same name in the parent flow and in the child flow. If the list item is a
	 * Map, each map entry must be a String key naming the attribute in the sub
	 * flow, and a String value naming the attribute in the parent flow. If the
	 * list item is itself a List, then that list is itself evaluated
	 * recursively, and must itself contain Strings, Lists, or Maps.
	 * <p>
	 * Note: only <strong>one </strong> of setOutputMappings or
	 * setOutputMappingsMap must be called.
	 * @param outputMappings The output mappings
	 */
	public void setOutputMappings(Collection outputMappings) {
		this.outputMappings = new HashMap(outputMappings.size());
		putCollectionMappings(this.outputMappings, outputMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from the
	 * sub flow. This is essentially just a short form of calling
	 * <p>
	 * Note: Only <strong>one </strong> of setOutputMappings or
	 * setOutputMappingsMap must be called.
	 * @param outputMappings The output mappings
	 * @link ParameterizableFlowModelMapper#setOutputMappings(List) with a
	 *       List containing one item which is a Map. Each map entry must be a
	 *       String key naming the attribute in the sub flow, and a String value
	 *       naming the attribute in the parent flow.
	 */
	public void setOutputMappingsMap(Map outputMappings) {
		this.outputMappings = new HashMap(outputMappings);
	}

	/**
	 * Internal worker function to convert given mappingsList to a simple
	 * mappings map.
	 */
	private void putCollectionMappings(Map map, Collection mappingsList) {
		Iterator it = mappingsList.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (key instanceof Collection) {
				putCollectionMappings(map, (Collection)key);
			}
			else if (key instanceof Map) {
				Map internalMap = (Map)key;
				// we could just add the map into the other, but want to
				// validate key and value
				// types!
				Iterator itMap = internalMap.entrySet().iterator();
				while (itMap.hasNext()) {
					Map.Entry entry = (Map.Entry)itMap.next();
					Assert.isInstanceOf(String.class, entry.getKey(),
							"ParameterizableFlowModelMapper key: ");
					Assert.isInstanceOf(String.class, entry.getValue(),
							"ParameterizableFlowModelMapper value: ");
					map.put(entry.getKey(), entry.getValue());
				}
			}
			else {
				Assert.isInstanceOf(String.class, key, "ParameterizableFlowModelMapper key or value: ");
				map.put(key, key);
			}
		}
	}

	/**
	 * Set whether or not missing attributes in the model should be mapped to a
	 * null value or shouldn't be mapped at all.
	 */
	public void setMapMissingAttributesToNull(boolean toNull) {
		this.mapMissingAttributesToNull = toNull;
	}

	/**
	 * Get whether or not missing attributes in the model should be mapped to a
	 * null value or shouldn't be mapped at all.
	 */
	public boolean isMapMissingAttributesToNull() {
		return this.mapMissingAttributesToNull;
	}

	public Map createSubFlowInputAttributes(FlowModel parentFlowModel) {
		Map subFlowAttributes = new HashMap();
		map(parentFlowModel, new MapMutableFlowModelAdapter(subFlowAttributes), inputMappings);
		return Collections.unmodifiableMap(subFlowAttributes);
	}

	public void mapSubFlowOutputAttributes(FlowModel subFlowModel, MutableFlowModel parentFlowModel) {
		map(subFlowModel, parentFlowModel, outputMappings);
	}

	/**
	 * Map data from one map to another map using specified mappings.
	 */
	protected void map(FlowModel from, MutableFlowModel to, Map mappings) {
		if (mappings != null) {
			Iterator fromNames = mappings.keySet().iterator();
			while (fromNames.hasNext()) {
				// get source value
				String fromName = (String)fromNames.next();
				int idx = fromName.indexOf('.');
				Object fromValue;
				if (idx != -1) {
					// fromName is something like "beanName.propName"
					String beanName = fromName.substring(0, idx);
					String propName = fromName.substring(idx + 1);
					BeanWrapper bw = createBeanWrapper(from.getAttribute(beanName));
					fromValue = bw.getPropertyValue(propName);
				}
				else {
					fromValue = from.getAttribute(fromName);
				}
				// set target value
				String toName = (String)mappings.get(fromName);
				idx = toName.indexOf('.');
				if (idx != -1) {
					// toName is something like "beanName.propName"
					String beanName = toName.substring(0, idx);
					String propName = toName.substring(idx + 1);

					BeanWrapper bw = createBeanWrapper(to.getAttribute(beanName));
					if (logger.isDebugEnabled()) {
						logger.debug("Mapping bean property attribute from path '" + fromName + "' to path '" + toName
								+ "' with value '" + fromValue + "'");
					}
					bw.setPropertyValue(propName, fromValue);
				}
				else {
					if (fromValue == null && !from.containsAttribute(fromName)) {
						if (isMapMissingAttributesToNull()) {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model - thus, I will map a null value");
							}
							to.setAttribute(toName, null);
						}
						else {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model - thus, I will NOT map a value");
							}
						}
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Mapping attribute from name '" + fromName + "' to name '" + toName
									+ "' with value '" + fromValue + "'");
						}
						to.setAttribute(toName, fromValue);
					}
				}
			}
		}
	}

	/**
	 * Create a new bean wrapper wrapping given object. Can be redefined in
	 * subclasses in case special property editors need to be registered or when
	 * other similar tuning is required.
	 */
	protected BeanWrapper createBeanWrapper(Object obj) {
		return new BeanWrapperImpl(obj);
	}

	/**
	 * Helper class that wraps a map in a MutableFlowModel interface.
	 */
	private static class MapMutableFlowModelAdapter implements MutableFlowModel {
		
		private Map map;

		public MapMutableFlowModelAdapter(Map map) {
			this.map = map;
		}

		public Object getAttribute(String attributeName) {
			return map.get(attributeName);
		}

		public boolean containsAttribute(String attributeName) {
			return map.containsKey(attributeName);
		}

		public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		public void assertAttributePresent(String attributeName) throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		public boolean containsAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public Object getAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public Object getRequiredAttribute(String attributeName) {
			throw new UnsupportedOperationException();
		}

		public Collection attributeEntries() {
			throw new UnsupportedOperationException();
		}

		public Collection attributeNames() {
			throw new UnsupportedOperationException();
		}

		public Collection attributeValues() {
			throw new UnsupportedOperationException();
		}

		public Collection findAttributes(Constraint criteria) {
			throw new UnsupportedOperationException();
		}

		public Object getRequiredAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public void setAttribute(String attributeName, Object attributeValue) {
			map.put(attributeName, attributeValue);
		}

		public void setAttributes(Map attributes) {
			throw new UnsupportedOperationException();
		}

		public void removeAttribute(String attributeName) {
			map.remove(attributeName);
		}

		public void assertInTransaction(HttpServletRequest request, boolean reset) throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		public boolean inTransaction(HttpServletRequest request, boolean reset) {
			throw new UnsupportedOperationException();
		}

		public void beginTransaction() {
			throw new UnsupportedOperationException();
		}

		public void endTransaction() {
			throw new UnsupportedOperationException();
		}
	}
}