/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.values;

import java.beans.PropertyEditor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;

/**
 * <p>
 * An extension of the base property access strategy interface that allows for
 * mutable operations. Specifically, this interface allows:
 * </p>
 * <ul>
 * <li>registering custom property editors for performing type conversions
 * <li>returning a domain object holder allowing the underying domain object to
 * be changed and subscribed to for modification, and
 * <li>adding listeners for changes on particular properties.
 * </ul>
 * 
 * @author Keith Donald
 */
public interface MutablePropertyAccessStrategy extends PropertyAccessStrategy,
        PropertyAccessor {
    public void registerCustomEditor(Class propertyType,
            PropertyEditor propertyEditor);

    public void registerCustomEditor(String propertyName,
            PropertyEditor propertyEditor);

    public PropertyEditor findCustomEditor(String propertyName);

    public MutablePropertyAccessStrategy getPropertyAccessStrategyForPath(
            String propertyPath) throws BeansException;

    public ValueModel getDomainObjectHolder();

    public void addValueListener(ValueListener listener, String propertyPath);

    public void removeValueListener(ValueListener listener, String propertyPath);

    public MutablePropertyAccessStrategy newPropertyAccessStrategy(
            ValueModel domainObjectHolder);

}