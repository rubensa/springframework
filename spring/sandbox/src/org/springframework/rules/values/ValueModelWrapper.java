/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public class ValueModelWrapper implements ValueModel, ValueListener {

    private Object value;

    private ValueModel wrappedModel;

    public ValueModelWrapper(ValueModel valueModel) {
        this.wrappedModel = valueModel;
        this.value = wrappedModel.get();
        this.wrappedModel.addValueListener(this);
    }

    /**
     * @see org.springframework.rules.values.ValueModel#get()
     */
    public Object get() {
        return value;
    }

    /**
     * @see org.springframework.rules.values.ValueModel#set(java.lang.Object)
     */
    public void set(Object value) {
        this.wrappedModel.set(value);
    }

    public void valueChanged() {
        this.value = wrappedModel.get();
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#addObserver(java.util.Observer)
     */
    public void addValueListener(ValueListener l) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#removeObserver(java.util.Observer)
     */
    public void removeValueListener(ValueListener l) {
        throw new UnsupportedOperationException();
    }

}