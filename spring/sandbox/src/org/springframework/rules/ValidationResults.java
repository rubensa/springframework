/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.rules;

import java.util.Map;

/**
 * %single sentence summary caption%.
 * 
 * %long description%.
 *
 * @author  Keith Donald
 */
public interface ValidationResults {
    public Map getResults();
    public UnaryPredicate getResults(String propertyName);
}