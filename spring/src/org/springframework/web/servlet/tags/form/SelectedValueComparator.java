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

package org.springframework.web.servlet.tags.form;

import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.core.JdkVersion;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.BindStatus;

/**
 * Utility class for testing whether a candidate value matches a {@link BindStatus#getValue data bound value}.
 * Eagerly attempts to prove a comparison through a number of avenues to deal with issues such as instance
 * inequality, logical (String-representation-based) equality and {@link PropertyEditor}-based comparison.
 *
 * <p>Full support is provided for comparing arrays, {@link Collection Collections} and {@link Map Maps}.
 * 
 * <p><h1><a name="equality-contract">Equality Contract</a></h1>
 * For single-valued objects equality is first tested using standard {@link Object#equals Java equality}. As
 * such, user code should endeavour to implement {@link Object#equals} to speed up the comparison process. If
 * {@link Object#equals} returns <code>false</code> then an attempt is made at an
 * {@link #exhaustiveCompare exhaustive comparison} with the aim being to <strong>prove</strong> equality rather
 * than disprove it.
 * 
 * <p>Special support is given for instances of {@link LabeledEnum} with a <code>String</code>-based
 * comparison of the candidate value against the code of the {@link LabeledEnum}. This can be useful when a
 * {@link LabeledEnum} is used to define a list of '<code>&lt;option&gt;</code>' elements in HTML.
 *
 * <p>Next, an attempt is made to compare the <code>String</code> representations of both the candidate and bound
 * values. This may result in <code>true</code> in a number of cases due to the fact both values will be represented
 * as <code>Strings</code> when shown to the user.
 * 
 * <p>Next, if the candidate value is a <code>String</code>, an attempt is made to compare the bound value to
 * result of applying the corresponding {@link PropertyEditor} to the candidate. This comparison may be
 * executed twice, once against the direct <code>String</code> instances, and then against the <code>String</code>
 * representations if the first comparison results in <code>false</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
abstract class SelectedValueComparator {

	/**
	 * Returns <code>true</code> if the supplied candidate value is equal to the value bound to
	 * the supplied {@link BindStatus}. Equality in this case differs from standard Java equality and
	 * is described in more detail <a href="#equality-contract">here</a>.
	 */
	public static boolean isSelected(BindStatus bindStatus, Object candidateValue) {
		Object boundValue = getBoundValue(bindStatus);

		if (boundValue == null) {
			return (candidateValue == null);
		}

		boolean selected = false;

		if (boundValue.getClass().isArray()) {
			selected = collectionCompare(CollectionUtils.arrayToList(boundValue), candidateValue, bindStatus);
		}
		else if (boundValue instanceof Collection) {
			selected = collectionCompare((Collection) boundValue, candidateValue, bindStatus);
		}
		else if (boundValue instanceof Map) {
			selected = mapCompare((Map) boundValue, candidateValue, bindStatus);
		}

		if (!selected) {
			if (ObjectUtils.nullSafeEquals(boundValue, candidateValue)) {
				selected = true;
			}
			else {
				selected = exhaustiveCompare(boundValue, candidateValue, bindStatus.getEditor(), null);
			}
		}

		return selected;
	}


	private static boolean mapCompare(Map boundMap, Object candidateValue, BindStatus bindStatus) {
		if (boundMap.containsKey(candidateValue)) {
			return true;
		}
		else {
			return exhaustiveCollectionCompare(boundMap.keySet(), candidateValue, bindStatus);
		}
	}

	private static boolean collectionCompare(Collection boundCollection, Object candidateValue, BindStatus bindStatus) {
		if (boundCollection.contains(candidateValue)) {
			return true;
		}
		else {
			return exhaustiveCollectionCompare(boundCollection, candidateValue, bindStatus);
		}
	}

	private static Object getBoundValue(BindStatus bindStatus) {
		if (bindStatus == null) {
			return null;
		}
		else if (bindStatus.getActualValue() != null) {
			return bindStatus.getActualValue();
		}
		else {
			return bindStatus.getValue();
		}
	}

	private static boolean exhaustiveCollectionCompare(
			Collection collection, Object candidateValue, BindStatus bindStatus) {

		PropertyEditorRegistry editorRegistry = null;
		if (bindStatus.getErrors() instanceof BindingResult) {
			editorRegistry = ((BindingResult) bindStatus.getErrors()).getPropertyEditorRegistry();
		}
		Map convertedValueCache = new HashMap(1);
		for (Iterator it = collection.iterator(); it.hasNext();) {
			Object element = it.next();
			PropertyEditor propertyEditor = null;
			if (element != null && editorRegistry != null) {
				propertyEditor = editorRegistry.findCustomEditor(element.getClass(), bindStatus.getPath());
			}
			if (exhaustiveCompare(element, candidateValue, propertyEditor, convertedValueCache)) {
				return true;
			}
		}
		return false;
	}

	private static boolean exhaustiveCompare(
			Object boundValue, Object candidate, PropertyEditor propertyEditor, Map convertedValueCache) {

		String candidateDisplayString = ValueFormatter.getDisplayString(candidate, propertyEditor, false);
		if (boundValue instanceof LabeledEnum) {
			LabeledEnum labeledEnum = (LabeledEnum) boundValue;
			String enumCodeAsString = ObjectUtils.getDisplayString(labeledEnum.getCode());
			if (enumCodeAsString.equals(candidateDisplayString)) {
				return true;
			}
			String enumLabelAsString = ObjectUtils.getDisplayString(labeledEnum.getLabel());
			if (enumLabelAsString.equals(candidateDisplayString)) {
				return true;
			}
		}
		else if (JdkVersion.isAtLeastJava15() && boundValue.getClass().isEnum()) {
			Enum boundEnum = (Enum) boundValue;
			String enumCodeAsString = ObjectUtils.getDisplayString(boundEnum.name());
			if (enumCodeAsString.equals(candidateDisplayString)) {
				return true;
			}
			String enumLabelAsString = ObjectUtils.getDisplayString(boundEnum.toString());
			if (enumLabelAsString.equals(candidateDisplayString)) {
				return true;
			}
		}
		else if (ObjectUtils.getDisplayString(boundValue).equals(candidateDisplayString)) {
			return true;
		}
		else if (propertyEditor != null && candidate instanceof String) {
			// Try PE-based comparison (PE should *not* be allowed to escape creating thread)
			String candidateAsString = (String) candidate;
			Object candidateAsValue = null;
			if (convertedValueCache != null && convertedValueCache.containsKey(propertyEditor)) {
				candidateAsValue = (String) convertedValueCache.get(propertyEditor);
			}
			else {
				propertyEditor.setAsText(candidateAsString);
				candidateAsValue = propertyEditor.getValue();
				if (convertedValueCache != null) {
					convertedValueCache.put(propertyEditor, candidateAsValue);
				}
			}
			if (ObjectUtils.nullSafeEquals(boundValue, candidateAsValue)) {
				return true;
			}
		}
		return false;
	}

}
