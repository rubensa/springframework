/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Simple listener interface for clients interested in being notified of a value
 * change.
 * 
 * @author Keith Donald
 */
public interface ValueListener {
    public void valueChanged();
}