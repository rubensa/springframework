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
public interface MutableAspectAccessStrategy extends AspectAccessStrategy {
    public void addValueListener(ValueListener o, String aspect);

    public void removeValueListener(ValueListener o, String aspect);

    public void setValue(String aspect, Object value);
}