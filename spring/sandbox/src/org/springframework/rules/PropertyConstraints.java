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
package org.springframework.rules;

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

    public BeanPropertyExpression all(UnaryPredicate[] valueConstraints) {
        return c.all(propertyName, valueConstraints);
    }

    public BeanPropertyExpression any(UnaryPredicate[] valueConstraints) {
        return c.any(propertyName, valueConstraints);
    }

    public BeanPropertyExpression eq(Object value) {
        return c.eq(propertyName, value);
    }

    public BeanPropertyExpression lt(Object value) {
        return c.lt(propertyName, value);
    }

    public BeanPropertyExpression lte(Object value) {
        return c.lte(propertyName, value);
    }

    public BeanPropertyExpression gt(Object value) {
        return c.gte(propertyName, value);
    }

    public BeanPropertyExpression gte(Object value) {
        return c.gte(propertyName, value);
    }

    public BeanPropertyExpression eqProperty(String otherPropertyName) {
        return c.eqProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression ltProperty(String otherPropertyName) {
        return c.ltProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression lteProperty(String otherPropertyName) {
        return c.lteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression gtProperty(String otherPropertyName) {
        return c.gtProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression gteProperty(String otherPropertyName) {
        return c.gteProperty(propertyName, otherPropertyName);
    }

    public BeanPropertyExpression inRange(Comparable min, Comparable max) {
        return c.inRange(propertyName, min, max);
    }

    public BeanPropertyExpression inRangeProperties(String minProperty,
            String maxProperty) {
        return c.inRangeProperties(propertyName, minProperty, maxProperty);
    }

}