/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

public interface CommitListener {
    public boolean preEditCommitted(Object formObject);

    public void postEditCommitted(Object formObject);
}