/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.reporting.ValidationResults;

/**
 * @author Keith Donald
 */
public class FormError {
    private String formObjectProperty;
    private ValidationResults error;
    
    public FormError(String formObjectProperty, ValidationResults error) {
        this.formObjectProperty = formObjectProperty;
        this.error = error;
    }
    
    public String getProperty() {
        return formObjectProperty;
    }
    
    public ValidationResults getError() {
        return error;
    }
}
