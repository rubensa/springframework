/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.beans.PropertyChangeListener;

/**
 * Interface implemented by domain objects that can publish property change
 * events. Clients can use this interface to subscribe to the object for change
 * notifications.
 * 
 * @author Keith Donald
 */
public interface PropertyChangePublisher {
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener,
            String propertyName);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener,
            String propertyName);
}