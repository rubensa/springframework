/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.reflection.ReflectiveModelMBeanInfoAssembler;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.jmx.naming.KeyNamingStrategy;
import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.proxy.JmxProxyFactoryBean;
import org.springframework.jmx.util.JmxUtils;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import java.util.HashMap;
import java.util.Map;

/**
 * A bean that allows for any Spring managed to be exposed to an <code>MBeanServer</code>
 * without the need to define any JMX specific information in the bean classes.
 * <p/>
 * If the bean implements one of the JMX management interface then
 * JmxMBeanAdapter will simply register the MBean with the server automatically.
 * <p/>
 * If the bean does not implement on the JMX management interface then
 * <code>JmxMBeanAdapter</code> will create the management information using the supplied
 * <code>ModelMBeanMetadataAssembler</code> implementation.
 *
 * @author Rob Harrop
 * @since 1.2
 */
public class JmxMBeanAdapter implements InitializingBean, DisposableBean,
        BeanFactoryAware {

    /**
     * <code>Log</code> instance for this class.
     */
    private static final Log log = LogFactory.getLog(JmxMBeanAdapter.class);

    /**
     * The beans to be exposed as JMX managed resources.
     */
    private Map beans;

    /**
     * Stores the <code>ModelMBeanProvider</code> used by this class to obatin
     * <code>ModelMBean</code> instances.
     */
    private ModelMBeanProvider mbeanProvider = new RequiredModelMBeanProvider();

    /**
     * Stores the <code>ModelMBeanInfoAssembler</code> to use for this
     * adaptor.
     */
    private ModelMBeanInfoAssembler assembler = new ReflectiveModelMBeanInfoAssembler();


    /**
     * The strategy to use for creating ObjectNames for an object
     */
    private ObjectNamingStrategy namingStrategy = new KeyNamingStrategy();

    /**
     * The MBeanServer instance being used to register beans
     */
    private MBeanServer server;

    /**
     * The beans that have been registered.
     */
    private ObjectName[] registeredBeans = null;

    /**
     * Stores the BeanFactory for use in autodetection process
     */
    private ConfigurableListableBeanFactory beanFactory = null;

    /**
     * 
     */
    public void afterPropertiesSet() throws Exception {
        // register the beans now
        registerBeans();
    }

    /**
     * Supply a <code>Map</code> of beans to be registered with the JMX
     * <code>MBeanServer</code>.
     *
     * @param beans a <code>Map</code> whose entries are the beans to register via JMX.
     */
    public void setBeans(Map beans) {
        this.beans = beans;
    }

    /**
     * Set the implementation of the <code>ModelMBeanInfoAssembler</code> interface
     * to use for this instance.
     *
     * @param assembler an implementation of the <code>ModelMBeanInfoAssembler</code> interface.
     */
    public void setAssembler(ModelMBeanInfoAssembler assembler) {
        this.assembler = assembler;
    }


    /**
     * Set the implementation of the <code>ObjectNamingStrategy</code> interface to
     * use for this instance.
     *
     * @param namingStrategy an implementation of the <code>ObjectNamingStrategy</code> interface.
     */
    public void setNamingStrategy(ObjectNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    /**
     * Specify an instance <code>MBeanServer</code> with which all beans should
     * be registered. The <code>JmxMBeanAdapter</code> will attempt to locate an
     * existing <code>MBeanServer</code> if none is supplied.
     *
     * @param server an instance of <code>MBeanServer</code>.
     */
    public void setServer(MBeanServer server) {
        this.server = server;
    }

    /**
     * Set the implementation of the <code>ModelMBeanProvider</code> interface to
     * use for this instance.
     *
     * @param mbeanProvider an implementation of the <code>ModelMBeanProvider</code> interface.
     */
    public void setBeanProvider(ModelMBeanProvider mbeanProvider) {
        this.mbeanProvider = mbeanProvider;
    }

    /**
     * Registers the defined beans with the <code>MBeanServer</code>. Each bean is exposed
     * to the <code>MBeanServer</code> via a <code>ModelMBean</code>. The actual implemetation
     * of the <code>ModelMBean</code> interface used depends on the implementation of the
     * <code>ModelMBeanProvider</code> interface that is configuerd. By default the <code>
     * RequiredModelMBean</code> class that is supplied with all JMX implementations is used. The management
     * interface produced for each bean is dependent on te <code>ModelMBeanInfoAssembler</code>
     * implementation being used. The <code>ObjectName</code> given to each bean is dependent on
     * the implementation of the <code>ObjectNamingStrategy</code> interface being used.
     */
    public void registerBeans() {

        // the beans property may be null
        // initially if we are relying solely
        // on autodetection
        if (beans == null) {
            beans = new HashMap();
        }

        // if no server was provided
        // then try to load one.
        // This is useful in environment such as
        // JBoss where there is already an MBeanServer loaded
        if (server == null) {
            log.debug("No MBeanServer provided. Attempting to locate one...");
            this.server = JmxUtils.locateMBeanServer();
        }

        // allow the metadata assembler a chance to
        // vote for bean inclusion
        if (assembler instanceof AutodetectCapableModelMBeanInfoAssembler) {
            autodetectBeans();
        }

        // check we now have at least one bean
        if (beans.size() < 1) {
            throw new IllegalArgumentException("Must specify at least one bean for registration");
        }

        Object[] keys = beans.keySet().toArray();
        registeredBeans = new ObjectName[keys.length];

        try {
            for (int x = 0; x < keys.length; x++) {
                String key = (String) keys[x];

                Object bean = beans.get(key);
                ObjectName objectName = namingStrategy.getObjectName(bean, key);

                if (bean instanceof DynamicMBean) {
                    log.info("Registering User Created MBean: "
                            + objectName.toString());

                    server.registerMBean(bean, objectName);
                } else {
                    ModelMBean mbean = mbeanProvider.getModelMBean();
                    mbean.setModelMBeanInfo(assembler.getMBeanInfo(bean));
                    mbean.setManagedResource(bean, "ObjectReference");

                    log.info("Registering and Assembling MBean: "
                            + objectName.toString());

                    server.registerMBean(mbean, objectName);
                }
                registeredBeans[x] = objectName;

                log.info("Registered MBean: " + objectName.toString());

            }
        } catch (JMException ex) {
            throw new MBeanAssemblyException("A JMX error occured when trying to assemble "
                    + "the management interface metadata.", ex);
        } catch (InvalidTargetObjectTypeException ex) {
            // we should never get this
            log.warn("Received InvalidTargetObjectTypeException - this should not occur!");
            throw new MBeanAssemblyException("An invalid object type was used when specifying a managed resource. "
                    + "This is a serious error and points to an error in the Spring JMX Code",
                    ex);
        }
    }

    /**
     * Invoked when using an <code>AutodetectCapableModelMBeanInfoAssembler</code>. Gives the
     * assembler the opportunity to additional beans from the <code>BeanFactory</code> to the list
     * of beans to be exposed via JMX. This implementation prevents a bean from being added to the
     * list automatically if it has already been added manually and it prevents certain internal
     * classes from being registered automatically.
     */
    private void autodetectBeans() {
        AutodetectCapableModelMBeanInfoAssembler autodetectAssembler = (AutodetectCapableModelMBeanInfoAssembler) assembler;

        String[] beanNames = beanFactory.getBeanDefinitionNames();
        String[] excludeBeans = beanFactory.getBeanDefinitionNames(JmxProxyFactoryBean.class);

        for (int x = 0; x < beanNames.length; x++) {
            String beanName = beanNames[x].trim();
            boolean exclude = false;

            for (int y = 0; y < excludeBeans.length; y++) {
                String excludeName = excludeBeans[y].trim();
                if (beanName.equals(excludeName)) {
                    exclude = true;
                }
            }

            if (exclude)
                continue;

            Object bean = beanFactory.getBean(beanName);

            if (!beans.containsValue(bean)) {
                // not already registered
                // for JMXification

                if (autodetectAssembler.includeBean(beanName, bean)) {
                    if (log.isInfoEnabled()) {
                        log.info("Bean Name: " + beanName
                                + " has been autodetected for JMXification");
                    }
                    beans.put(beanName, bean);
                }
            }
        }
    }

    /**
     * Unregisters all the beans when the enclosing <code>BeanFactory</code> is destroyed.
     */
    public void destroy() throws Exception {
        log.info("Unregistering all beans");

        for (int x = 0; x < registeredBeans.length; x++) {
            server.unregisterMBean(registeredBeans[x]);
        }

        server = null;
    }

    /**
     * Implemented to grab the <code>BeanFactory</code> to allow for auto detection of
     * managed bean resources
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        } else {
            log.info("Not using a ConfigurableListableBeanFactory - auto detection of managed beans is disabled");
        }
    }
}