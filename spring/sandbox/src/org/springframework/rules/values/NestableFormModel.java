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
public interface NestableFormModel extends FormModel {
    public void setParent(NestingFormModel parent);

    public ValueModel getValueModel(String domainObjectProperty,
            boolean queryParent);
}