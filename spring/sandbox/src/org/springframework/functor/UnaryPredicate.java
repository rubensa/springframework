/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

import java.io.Serializable;

/**
 * @author  Keith Donald
 */
public interface UnaryPredicate extends Serializable {
    public boolean evaluate(Object value);
}
