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
public interface NestingFormModel extends FormModel {
    public ValueModel findValueModelFor(FormModel delegatingChild,
            String domainObjectProperty);
}