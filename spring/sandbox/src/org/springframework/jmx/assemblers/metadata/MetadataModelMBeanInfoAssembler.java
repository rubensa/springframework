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
package org.springframework.jmx.assemblers.metadata;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;

import org.springframework.jmx.assemblers.AbstractReflectionBasedModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.metadata.support.MetadataReader;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * Implementation of <tt>ModelMBeanInfoAssembler</tt> that reads the
 * management interface information from source level metadata. Uses Spring's
 * metadata abstraction layer so that metadata can be read using any supported
 * implementation.
 * 
 * @author Rob Harrop
 */
public class MetadataModelMBeanInfoAssembler extends
        AbstractReflectionBasedModelMBeanInfoAssembler implements
        AutodetectCapableModelMBeanInfoAssembler {

    private static final String LOG = "log";

    private static final String LOG_FILE = "logFile";

    private static final String CURRENCY_TIME_LIMIT = "currencyTimeLimit";
    
    private static final String DEFAULT = "default";

    /**
     * Attributes implementation. Default is Commons Attributes
     */
    private Attributes attributes = new CommonsAttributes();

    /**
     * Set the <tt>Attributes</tt> implementation.
     * 
     * @param attributes
     * @see org.springframework.metadata.Attributes
     */
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    protected boolean includeReadAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeWriteAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeOperation(Method method) {
        if (JmxUtils.isProperty(method)) {
            return hasManagedAttribute(method);
        } else {
            return hasManagedOperation(method);
        }
    }

    protected String getOperationDescription(Method method) {

        if (JmxUtils.isProperty(method)) {
            ManagedAttribute ma = MetadataReader.getManagedAttribute(
                    attributes, method);

            if (ma == null) {
                return method.getName();
            } else {
                return ma.getDescription();
            }
        } else {
            ManagedOperation mo = MetadataReader.getManagedOperation(
                    attributes, method);

            if (mo == null) {
                return method.getName();
            } else {
                return mo.getDescription();
            }
        }
    }

    /**
     * Creates a description for the attribute corresponding to this property
     * descriptor. Attempts to create the description using metadata from either
     * the getter or setter attributes, otherwise uses the property name.
     * 
     * @param propertyDescriptor
     * @return
     */
    protected String getAttributeDescription(
            PropertyDescriptor propertyDescriptor) {

        Method readMethod = propertyDescriptor.getReadMethod();
        Method writeMethod = propertyDescriptor.getWriteMethod();

        ManagedAttribute getter = (readMethod != null) ? MetadataReader
                .getManagedAttribute(attributes, readMethod) : null;
        ManagedAttribute setter = (writeMethod != null) ? MetadataReader
                .getManagedAttribute(attributes, writeMethod) : null;

        StringBuffer sb = new StringBuffer();

        if ((getter != null) && (getter.getDescription() != null)
                && (getter.getDescription().length() > 0)) {
            return getter.getDescription();
        } else if ((setter != null) && (setter.getDescription() != null)
                && (setter.getDescription().length() > 0)) {
            return setter.getDescription();
        } else {
            return propertyDescriptor.getDisplayName();
        }
    }

    /**
     * Attempts to read managed resource description from the source level
     * metdata. Returns an empty <code>String</code> if no description can be
     * found.
     */
    protected String getDescription(Object bean) {
        ManagedResource mr = MetadataReader.getManagedResource(attributes, bean
                .getClass());

        if (mr == null) {
            return "";
        } else {
            return mr.getDescription();
        }
    }

    protected void populateMBeanDescriptor(Descriptor mbeanDescriptor,
            Object bean) {
        ManagedResource mr = MetadataReader.getManagedResource(attributes, bean
                .getClass());

        mbeanDescriptor.setField(LOG, mr.isLog() ? "true" : "false");

        if (mr.getLogFile() != null) {
            mbeanDescriptor.setField(LOG_FILE, mr.getLogFile());
        }

        mbeanDescriptor.setField(CURRENCY_TIME_LIMIT, new Integer(mr
                .getCurrencyTimeLimit()));
    }

    protected void populateAttributeDescriptor(Descriptor descriptor,
            Method getter, Method setter) {

        ManagedAttribute gma = (getter == null) ? ManagedAttribute.EMPTY : MetadataReader
                .getManagedAttribute(attributes, getter);
        
        ManagedAttribute sma = (setter == null) ? ManagedAttribute.EMPTY : MetadataReader
                .getManagedAttribute(attributes, setter);


        int ctl = getCurrencyTimeLimit(gma.getCurrencyTimeLimit(), sma.getCurrencyTimeLimit());
        descriptor.setField(CURRENCY_TIME_LIMIT, new Integer(ctl));
        
        Object defaultValue = getDefaultValue(gma.getDefaultValue(), sma.getDefaultValue());
        descriptor.setField(DEFAULT, defaultValue);
            
    }
    
    protected void populateOperationDescriptor(Descriptor descriptor,
            Method method) {
        ManagedOperation mo = MetadataReader.getManagedOperation(attributes, method);
        
        if(mo != null) {
            descriptor.setField(CURRENCY_TIME_LIMIT, new Integer(mo.getCurrencyTimeLimit()));
        }
    }
    
    /**
     * Determines which of two <code>int</code> values
     * should be used for the <code>currencyTimeLimit</code> descriptor.
     * In general only the getter or the setter will be have a non-zero
     * value so we use that value. In the event that both values
     * are non-zero we use the greater of the two.
     * @param getter the <code>int</code> value associated with the getter for this attribute.
     * @param setter the <code>int</code> value associated with the setter for this attribute.
     */
    private int getCurrencyTimeLimit(int getter, int setter) {
        if(getter == 0 && setter != 0) {
            return setter;
        } else if(setter == 0 && getter != 0) {
            return getter;
        } else {
            return (getter >= setter) ? getter : setter;
        }
    }
    
    /**
     * Locates the default value descriptor based on values attached
     * to both the getter and setter methods. If both have values
     * supplied then the value attached to the getter is preferred.
     * @param getter
     * @param setter
     * @return
     */
    private Object getDefaultValue(Object getter, Object setter) {
        if(getter != null) {
            return getter;
        } else if (setter != null) {
            return setter;
        } else {
            return null;
        }
    }

    private boolean hasManagedAttribute(Method method) {
        ManagedAttribute ma = MetadataReader.getManagedAttribute(attributes,
                method);

        return (ma != null) ? true : false;
    }

    private boolean hasManagedOperation(Method method) {
        ManagedOperation mo = MetadataReader.getManagedOperation(attributes,
                method);

        return (mo != null) ? true : false;
    }

    /**
     * Used for auto detection of beans. Checks to see if the bean's class has a
     * ManagedResource attribute. If so it will add it list of included beans
     */
    public boolean includeBean(String beanName, Object bean) {
        if (MetadataReader.getManagedResource(attributes, bean.getClass()) != null) {
            return true;
        } else {
            return false;
        }
    }
}