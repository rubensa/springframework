/*
 * $Header: /usr/local/cvs/module/src/java/File.java,v 1.7 2004/01/16 22:23:11
 * keith Exp $ $Revision$ $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.closure;

import java.io.Serializable;

import org.springframework.rules.BinaryClosure;
import org.springframework.util.Assert;

public abstract class AbstractBinaryClosure implements BinaryClosure, Serializable {

    public Object call(Object argument1) {
        if (argument1 == null || argument1 == NULL_VALUE) {
            argument1 = new Object[0];
        }
        Assert.isTrue(argument1.getClass().isArray(),
                "Binary argument must be an array");
        Object[] arguments = (Object[])argument1;
        Assert.isTrue(arguments.length == 2,
                "Binary argument must contain 2 elements");
        return call(arguments[0], arguments[1]);
    }

}