/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Client interface for value model objects whose value can change.
 * 
 * @author Keith Donald
 */
public interface ValueChangeable {
    public void addValueListener(ValueListener l);

    public void removeValueListener(ValueListener l);
}