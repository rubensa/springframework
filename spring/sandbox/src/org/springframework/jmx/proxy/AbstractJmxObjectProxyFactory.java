/*
 * Created on Jul 23, 2004
 */
package org.springframework.jmx.proxy;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.springframework.jmx.exceptions.ProxyCreationException;
import org.springframework.util.ClassUtils;

/**
 * @author robh
 */
public abstract class AbstractJmxObjectProxyFactory implements
        JmxObjectProxyFactory {

    /**
     * Store the interfaces to proxy
     */
    protected Class[] proxyInterfaces = null;

    /**
     * Specfiy with invalid invocations should be ignored or not.
     */
    protected boolean ignoreInvalidInvocations = true;

    public boolean getIgnoreInvalidInvocations() {
        return this.ignoreInvalidInvocations;
    }

    public Class[] getProxyInterfaces() {
        return this.proxyInterfaces;
    }

    public void setIgnoreInvalidInvocations(boolean ignoreInvalidInvocatios) {
        this.ignoreInvalidInvocations = ignoreInvalidInvocatios;
    }

    public void setProxyInterfaces(Class[] interfaces) {
        this.proxyInterfaces = interfaces;
    }

    /**
     * @param objectName
     */
    protected Class getClassForInstance(final ObjectName objectName,
            final MBeanServerConnection server) {
        ObjectInstance instance = null;
        
        try {

            instance = server.getObjectInstance(objectName);
            return ClassUtils.forName(instance.getClassName());

        } catch (InstanceNotFoundException ex) {

            // invalid ObjectName provided
            throw new ProxyCreationException(
                    "Unable to locate object specified by object name:"
                            + objectName
                            + ". Check the supplied ObjectName is valid");
        } catch (ClassNotFoundException ex) {

            // Unable to load class
            throw new ProxyCreationException("Unable to load class ["
                    + instance.getClassName() + "] for MBean [" + objectName
                    + "]. Ensure that this class is on the classpath.");
        } catch (IOException ex) {
			throw new ProxyCreationException(
					"An IOException occurred when communicating with the MBeanServer. "
							+ "It is likely that you are communicating with a remote MBeanServer. "
							+ "Check the inner exception for exact details.",
					ex);
		}
    }
}