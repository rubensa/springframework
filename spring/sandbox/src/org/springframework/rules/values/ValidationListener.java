/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.reporting.ValidationResults;

/**
 * 
 * @author Keith Donald
 */
public interface ValidationListener {
    public void constraintSatisfied(UnaryPredicate constraint);

    public void constraintViolated(UnaryPredicate constraint,
            ValidationResults results);
}