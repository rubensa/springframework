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
package org.springframework.rules.factory;

import org.springframework.rules.Constraint;
import org.springframework.rules.constraint.bean.BeanPropertyConstraint;
import org.springframework.util.Assert;

/**
 * Helper class for creating bean property constraints.
 * 
 * @author Keith Donald
 */
public class PropertyConstraints {
    private Constraints c = Constraints.instance();
    private String propertyName;

    public PropertyConstraints(String propertyName) {
        setPropertyName(propertyName);
    }

    public void setPropertyName(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public BeanPropertyConstraint all(Constraint[] valueConstraints) {
        return c.all(propertyName, valueConstraints);
    }

    public BeanPropertyConstraint any(Constraint[] valueConstraints) {
        return c.any(propertyName, valueConstraints);
    }

    public BeanPropertyConstraint eq(Object value) {
        return c.eq(propertyName, value);
    }

    public BeanPropertyConstraint lt(Object value) {
        return c.lt(propertyName, value);
    }

    public BeanPropertyConstraint lte(Object value) {
        return c.lte(propertyName, value);
    }

    public BeanPropertyConstraint gt(Object value) {
        return c.gte(propertyName, value);
    }

    public BeanPropertyConstraint gte(Object value) {
        return c.gte(propertyName, value);
    }

    public BeanPropertyConstraint eqProperty(String otherPropertyName) {
        return c.eqProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyConstraint ltProperty(String otherPropertyName) {
        return c.ltProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyConstraint lteProperty(String otherPropertyName) {
        return c.lteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyConstraint gtProperty(String otherPropertyName) {
        return c.gtProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyConstraint gteProperty(String otherPropertyName) {
        return c.gteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyConstraint inRange(Comparable min, Comparable max) {
        return c.inRange(propertyName, min, max);
    }

    public BeanPropertyConstraint inRangeProperties(String minProperty,
            String maxProperty) {
        return c.inRangeProperties(propertyName, minProperty, maxProperty);
    }

}