/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * 
 * @author Keith Donald
 */
public interface MutableFormModel extends FormModel {
    public void setFormProperties(String[] domainObjectProperties);

    public ValueModel getValueModel(String domainObjectProperty);

    public ValueModel add(String domainObjectProperty);
}