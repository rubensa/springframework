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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.MutableAttributesAccessor;

/**
 * Simple attributes mapper that allows mappings to be configured in the Spring
 * application context.
 * 
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td>toMappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping parent flow data <i>to </i> a newly
 * spawned sub flow. The provided list contains the names of the attributes in
 * the parent to pass to the subflow for access. The same name is used in both
 * parent flow and sub flow model.</td>
 * </tr>
 * <tr>
 * <td>toMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping parent flow data <i>to </i> a newly
 * spawned sub flow. The keys in given map are the names of entries in the
 * parent model that will be mapped. The value associated with a key is the name
 * of the target entry that will be placed in the subflow model.</td>
 * </tr>
 * <tr>
 * <td>fromMappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow data <i>from </i> the subflow
 * back to the parent flow (once the subflow ends and the parent flow resumes).
 * The provided list contains the names of the attributes in the subflow to pass
 * to the parent for access. The same name is used in both parent flow and sub
 * flow model.</td>
 * </tr>
 * <tr>
 * <td>fromMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow flow data <i>from </i> the
 * subflow back to the parent flow (once the subflow ends and the parent flow
 * resumes). The keys in given map are the names of entries in the parent model
 * that will be mapped. The value associated with a key is the name of the
 * target entry that will be placed in the subflow model.</td>
 * </tr>
 * </table>
 * 
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * </p>
 * 
 * TODO: this guy needs a unit test!
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableFlowAttributesMapper implements FlowAttributesMapper, Serializable {

	protected final Log logger = LogFactory.getLog(getClass());;

	private Map toMappings = Collections.EMPTY_MAP;

	private Map fromMappings = Collections.EMPTY_MAP;

	private boolean mapMissingAttributesToNull = false;

	/**
	 * Set the mappings that will be executed when mapping model data <i>to </i>
	 * a sub flow. Each list item must be a String, a List, or a Map. If the
	 * list item is a simple String value, the attribute will be mapped as
	 * having the same name in the parent flow and in the child flow. If the
	 * list item is a Map, each map entry must be a String key naming the
	 * attribute in the parent flow, and a String value naming the attribute in
	 * the child flow. If the list item is itself a List, then that list is
	 * itself evaluated recursively, and must itself contain Strings, Lists, or
	 * Maps.
	 * 
	 * <p>
	 * Only <strong>one </strong> of setToMappings or setToMappingsMap must be
	 * called.
	 */
	public void setToMappings(Collection toMappings) {
		this.toMappings = new HashMap();
		putCollectionMappings(this.toMappings, toMappings);
	}

	/**
	 * <p>
	 * Set the mappings that will be executed when mapping model data <i>to </i>
	 * the sub flow. This is essentially a just a short form of calling
	 * @link ParameterizableFlowAttributesMapper#setToMappings(List) with a List
	 *       containing one item which is a Map. Each map entry must be a String
	 *       key naming the attribute in the parent flow, and a String value
	 *       naming the attribute in the child flow.
	 *       </p>
	 * 
	 * <p>
	 * Only <strong>one </strong> of setToMappings or setToMappingsMap must be
	 * called.
	 */
	public void setToMappingsMap(Map toMappings) {
		this.toMappings = new HashMap(toMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data <i>from
	 * </i> the sub flow. Each list item must be a String, a List, or a Map. If
	 * the list item is a simple String value, the attribute will be mapped as
	 * having the same name in the parent flow and in the child flow. If the
	 * list item is a Map, each map entry must be a String key naming the
	 * attribute in the child flow, and a String value naming the attribute in
	 * the parent flow. If the list item is itself a List, then that list is
	 * itself evaluated recursively, and must itself contain Strings, Lists, or
	 * Maps.
	 * 
	 * <p>
	 * Only <strong>one </strong> of setFromMappings or setFromMappingsMap must
	 * be called.
	 */
	public void setFromMappings(Collection fromMappings) {
		this.fromMappings = new HashMap(fromMappings.size());
		putCollectionMappings(this.fromMappings, fromMappings);
	}

	/**
	 * <p>
	 * Set the mappings that will be executed when mapping model data <i>from
	 * </i> the sub flow. This is essentially a just a short form of calling
	 * @link ParameterizableFlowAttributesMapper#setFromMappings(List) with a
	 *       List containing one item which is a Map. Each map entry must be a
	 *       String key naming the attribute in the child flow, and a String
	 *       value naming the attribute in the parent flow.
	 *       </p>
	 * 
	 * <p>
	 * Only <strong>one </strong> of setToMappings or setToMappingsMap must be
	 * called.
	 */
	public void setFromMappingsMap(Map fromMappings) {
		this.fromMappings = new HashMap(fromMappings);
	}

	// internal worker function
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
							"ParameterizableFlowAttributesMapper key or value: ");
					Assert.isInstanceOf(String.class, entry.getValue(),
							"ParameterizableFlowAttributesMapper key or value: ");
					map.put(entry.getKey(), entry.getValue());
				}
			}
			else {
				Assert.isInstanceOf(String.class, key, "ParameterizableFlowAttributesMapper key or value: ");
				map.put(key, key);
			}
		}
	}

	public void setMapMissingAttributesToNull(boolean toNull) {
		this.mapMissingAttributesToNull = toNull;
	}

	public boolean isMapMissingAttributesToNull() {
		return this.mapMissingAttributesToNull;
	}

	public Map createSubFlowInputAttributes(AttributesAccessor parentFlowModel) {
		Map subFlowAttributes = new HashMap();
		map(parentFlowModel, new MapAttributesAccessorAdapter(subFlowAttributes), toMappings);
		return Collections.unmodifiableMap(subFlowAttributes);
	}

	public void mapSubFlowOutputAttributes(AttributesAccessor subFlowModel,
			MutableAttributesAccessor parentFlowModel) {
		map(subFlowModel, parentFlowModel, fromMappings);
	}

	/**
	 * Map data from one map to another map using specified mappings.
	 */
	protected void map(AttributesAccessor from, MutableAttributesAccessor to, Map mappings) {
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
	 * <p>
	 * Create a new bean wrapper wrapping given object. Can be redefined in
	 * subclasses in case special property editors need to be registered or when
	 * other similar tuning is required.
	 */
	protected BeanWrapper createBeanWrapper(Object obj) {
		return new BeanWrapperImpl(obj);
	}

	private static class MapAttributesAccessorAdapter implements MutableAttributesAccessor {
		private Map map;

		public MapAttributesAccessorAdapter(Map map) {
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
	}
}