/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface MetaAspectAccessStrategy {
    public boolean isReadable(String aspect);
    public boolean isWriteable(String aspect);
    public boolean isEnumeration(String aspect);
    public Class getAspectClass(String aspect);
}
