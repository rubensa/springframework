/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import org.springframework.rules.Function;
import org.springframework.util.DefaultObjectStyler;

/**
 * @author Keith Donald
 */
public class RefreshableValueHolder extends ValueHolder {
    private Function refreshFunction;

    private boolean alwaysRefresh;

    public RefreshableValueHolder(Function refreshFunction) {
        this(refreshFunction, false);
    }

    public RefreshableValueHolder(Function refreshFunction,
            boolean alwaysRefresh) {
        super((alwaysRefresh ? refreshFunction.evaluate() : null));
        this.refreshFunction = refreshFunction;
    }

    public Object get() {
        if (alwaysRefresh) {
            refresh();
        }
        return super.get();
    }

    public void refresh() {
        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing held value '"
                    + DefaultObjectStyler.evaluate(get()) + "'");
        }
        set(refreshFunction.evaluate());
    }
}