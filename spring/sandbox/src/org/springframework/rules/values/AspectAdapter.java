/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Adapts access to a domain model aspect (generally a property) to the value
 * model interface.  The aspect access strategy is pluggable.
 * 
 * @author Keith Donald
 */
public class AspectAdapter extends AbstractValueModel {
    private String aspect;

    private AspectAccessStrategy aspectAccessStrategy;

    public AspectAdapter(AspectAccessStrategy aspectAccessStrategy,
            String aspect) {
        this(null, aspectAccessStrategy, aspect);
    }

    public AspectAdapter(final ValueModel domainObjectHolder,
            AspectAccessStrategy aspectAccessStrategy, String aspect) {
        if (domainObjectHolder != null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Aspect Adapter attaching to mutable domain object holder.]");
            }
            domainObjectHolder.addValueListener(new ValueListener() {
                public void valueChanged() {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("[Notifying any dependents value may have changed; target object changed]");
                    }
                    AspectAdapter.this.fireValueChanged();
                }
            });
        }
        this.aspectAccessStrategy = aspectAccessStrategy;
        this.aspect = aspect;
    }

    public void addValueListener(ValueListener l) {
        super.addValueListener(l);
        aspectAccessStrategy.addValueListener(l, aspect);
    }

    public void removeValueListener(ValueListener l) {
        super.removeValueListener(l);
        aspectAccessStrategy.removeValueListener(l, aspect);
    }

    public Object get() {
        return aspectAccessStrategy.getValue(aspect);
    }

    public void set(Object value) {
        aspectAccessStrategy.setValue(aspect, value);
    }
}