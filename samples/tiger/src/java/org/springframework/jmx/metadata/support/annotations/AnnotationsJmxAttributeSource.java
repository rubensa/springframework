package org.springframework.jmx.metadata.support.annotations;

import org.springframework.jmx.metadata.support.InvalidMetadataException;
import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.annotations.AnnotationsAttributes;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author robh
 */
public class AnnotationsJmxAttributeSource implements JmxAttributeSource {

    private Attributes attributes = new AnnotationsAttributes();

    public ManagedResource getManagedResource(Class beanClass) throws InvalidMetadataException {
        Collection attrs = attributes.getAttributes(beanClass, org.springframework.jmx.metadata.support.annotations.ManagedResource.class);

        if (attrs.isEmpty()) {
            return null;
        } else if (attrs.size() == 1) {
            org.springframework.jmx.metadata.support.annotations.ManagedResource mr = (org.springframework.jmx.metadata.support.annotations.ManagedResource) attrs.iterator().next();
            ManagedResource attr = new ManagedResource();
            attr.setObjectName(mr.objectName());
            attr.setDescription(mr.description());
            attr.setCurrencyTimeLimit(mr.currencyTimeLimit());
            attr.setLog(mr.log());
            attr.setLogFile(mr.logFile());
            attr.setPersistPolicy(mr.persistPolicy());
            attr.setPersistPeriod(mr.persistPeriod());
            attr.setPersistLocation(mr.persistLocation());
            attr.setPersistName(mr.persistName());
            return attr;
        } else {
            throw new InvalidMetadataException("A Class can have only one ManagedResource attribute");
        }
    }

    public ManagedAttribute getManagedAttribute(Method method) throws InvalidMetadataException {
        Collection attrs = attributes.getAttributes(method, org.springframework.jmx.metadata.support.annotations.ManagedAttribute.class);

        if (attrs.isEmpty()) {
            return null;
        } else if (attrs.size() == 1) {
            org.springframework.jmx.metadata.support.annotations.ManagedAttribute ma = (org.springframework.jmx.metadata.support.annotations.ManagedAttribute) attrs.iterator().next();
            ManagedAttribute attr = new ManagedAttribute();

            attr.setDescription(ma.description());
            attr.setPersistPolicy(ma.persistPolicy());
            attr.setPersistPeriod(ma.persistPeriod());
            attr.setCurrencyTimeLimit(ma.currencyTimeLimit());

            if (ma.defaultValue().length() > 0) {
                attr.setDefaultValue(ma.defaultValue());
            }

            return attr;
        } else {
            throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
        }
    }

    public ManagedOperation getManagedOperation(Method method) throws InvalidMetadataException {
        if(JmxUtils.isProperty(method)) {
            throw new InvalidMetadataException("The ManagedOperation attribute is not valid for JavaBean properties. Use ManagedAttribute instead.");
        }

        Collection attrs = attributes.getAttributes(method, org.springframework.jmx.metadata.support.annotations.ManagedOperation.class);

        if(attrs.isEmpty()) {
            return null;
        } else if(attrs.size() == 1) {
            org.springframework.jmx.metadata.support.annotations.ManagedOperation mo = (org.springframework.jmx.metadata.support.annotations.ManagedOperation) attrs.iterator().next();
            ManagedOperation attr = new ManagedOperation();

            attr.setDescription(mo.description());
            attr.setCurrencyTimeLimit(mo.currencyTimeLimit());

            return attr;
        } else {
            throw new InvalidMetadataException("A Method can have only one ManagedAttribute attribute");
        }
    }
}
