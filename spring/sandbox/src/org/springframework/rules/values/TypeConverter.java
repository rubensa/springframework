/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.beans.PropertyEditor;

import org.springframework.rules.UnaryFunction;

/**
 * @author Keith Donald
 */
public class TypeConverter implements ValueModel {

    private ValueModel wrappedModel;

    private UnaryFunction convertTo;

    private UnaryFunction convertFrom;

    public TypeConverter(ValueModel wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    public TypeConverter(final PropertyEditor propertyEditor) {
        this.convertTo = new UnaryFunction() {
            public Object evaluate(Object o) {
                propertyEditor.setAsText((String)o);
                return propertyEditor.getValue();
            }
        };
        this.convertFrom = new UnaryFunction() {
            public Object evaluate(Object o) {
                propertyEditor.setValue(o);
                return propertyEditor.getAsText();
            }
        };
    }

    public TypeConverter(UnaryFunction convertTo, UnaryFunction convertFrom) {
        this.convertTo = convertTo;
        this.convertFrom = convertFrom;
    }

    /**
     * @see org.springframework.rules.values.ValueModel#get()
     */
    public Object get() {
        return convertTo.evaluate(wrappedModel.get());
    }

    /**
     * @see org.springframework.rules.values.ValueModel#set(java.lang.Object)
     */
    public void set(Object value) {
        wrappedModel.set(convertFrom.evaluate(value));
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#addValueListener(org.springframework.rules.values.ValueListener)
     */
    public void addValueListener(ValueListener l) {
        wrappedModel.addValueListener(l);
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#removeValueListener(org.springframework.rules.values.ValueListener)
     */
    public void removeValueListener(ValueListener l) {
        wrappedModel.removeValueListener(l);
    }

}