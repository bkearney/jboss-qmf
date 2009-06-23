package org.jboss.qmf.core.deployment;

import org.apache.qpid.agent.Agent;
import org.apache.qpid.agent.ManagedEJB;
import org.apache.qpid.agent.annotations.QMFObject;
import org.apache.qpid.client.AMQConnection;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.injection.AbstractPropertyInjector;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.qmf.core.metadata.QmfAgentDefinitionsMetaData;
import org.jboss.qmf.core.metadata.QmfAgentReferencesMetaData;
import org.jboss.qmf.core.metadata.QmfRegisteredClassesMetaData;

import javax.management.ObjectName;
import java.lang.reflect.Field;

public class QmfAgentDeployment
{
    private static final Logger log = Logger
            .getLogger(QmfAgentDeployment.class);
    private Agent agent;

    public QmfAgentDeployment(QmfAgentDefinitionsMetaData metadata)
    {
        if (metadata == null)
            return;
        try
        {
            AMQConnection amqConnection = new AMQConnection(metadata
                    .getBroker());
            agent = new Agent();
            agent.setConnection(amqConnection);
            agent.setLabel(metadata.getLabel());
            agent.setSessionTransacted(metadata.getSessionTransacted());
            agent.start();
        } catch (Exception e)
        {
            log.error("Unable to deploy QMF Agent", e);
        }
    }

    public boolean isActive()
    {
        return agent != null;
    }

    public void stop()
    {
        if (agent == null)
            return;
        // ok lets stop it
        agent.stop();
        agent = null;
    }

    public void registerQmfTypes(QmfRegisteredClassesMetaData metadata)
    {
        if (metadata == null)
            return;
        for (Class classToRegister : metadata.getAnnotatedClasses())
        {
            log.info("Exposing " + classToRegister.getSimpleName()
                    + " as QMFType");
            agent.registerClass(classToRegister);
        }
    }

    public void registerQmfObjects(JBossMetaData beans,
            Ejb3Deployment ejb3Deployment,
            QmfAgentReferencesMetaData agentMetadata)
            throws DeploymentException
    {
        if (beans == null)
            return;
        for (JBossEnterpriseBeanMetaData beanMetaData : beans
                .getEnterpriseBeans())
        {
            // extract ejb container
            EJBContainer ejbContainer = null;
            if (ejb3Deployment != null && !beanMetaData.isEntity())
            {
                ObjectName objName = null;
                try
                {
                    objName = new ObjectName(beanMetaData
                            .determineContainerName());
                } catch (Exception e)
                {
                    throw new DeploymentException(e);
                }
                ejbContainer = (EJBContainer) ejb3Deployment
                        .getContainer(objName);
            }
            if (ejbContainer == null)
                continue;
            deployAsQmfObject(beanMetaData, ejbContainer);
            injectQmfAgent(beanMetaData, ejbContainer, agentMetadata);
        }
    }

    private void injectQmfAgent(JBossEnterpriseBeanMetaData beanMetaData,
            EJBContainer ejbContainer, QmfAgentReferencesMetaData agentMetadata)
    {
        // no injections
        if (agentMetadata == null || agentMetadata.getRefFields().isEmpty())
            return;
        // look for injected field
        Class beanClass = ejbContainer.getBeanClass();
        final Field refField = findInjectedField(beanClass, agentMetadata);
        if (refField == null)
            return;
        // ok perform injection
        log.info("Injecting QMF Agent into "
                + refField.getDeclaringClass().getSimpleName() + ":"
                + refField.getName());
        ejbContainer.getInjectors().add(
                new AbstractPropertyInjector(new FieldBeanProperty(refField))
                {
                    public void inject(Object instance)
                    {
                        try
                        {
                            refField.set(instance, agent);
                        } catch (IllegalAccessException e)
                        {
                            log.info("Unable to inject QMF Agent", e);
                        }
                    }
                });
    }

    private Field findInjectedField(Class clazz,
            QmfAgentReferencesMetaData agentMetadata)
    {
        // if field defined on this class return it
        Field field = agentMetadata.getRefFields().get(clazz);
        if (field != null)
            return field;
        // try to check if it's defined on super class
        return clazz.equals(Object.class) ? null : findInjectedField(clazz
                .getSuperclass(), agentMetadata);
    }

    private void deployAsQmfObject(JBossEnterpriseBeanMetaData m,
            EJBContainer ejbContainer)
    {
        // try to find QMFObject
        QMFObject annotation = null;
        Class exposedClass = null;
        // get annotation from bean
        annotation = ejbContainer.getAnnotation(QMFObject.class);
        if (annotation != null)
            exposedClass = ejbContainer.getBeanClass();
        // try to get it from interfaces
        if (annotation == null)
        {
            for (Class localIf : ejbContainer.getBusinessInterfaces())
            {
                annotation = (QMFObject) localIf.getAnnotation(QMFObject.class);
                exposedClass = localIf;
                if (annotation != null)
                    break;
            }
        }
        // ok lets see if we got QMFObject
        if (annotation != null)
        {
            // expose it
            String qmfName = annotation.className();
            String qmfClass = exposedClass.getName();
            String jndiName = m.getLocalJndiName();
            log.info("Exposing " + m.getEjbName() + " as QMF Object: "
                    + qmfName + ", " + qmfClass + ", " + jndiName);
            ManagedEJB managedEJB = new ManagedEJB();
            managedEJB.setName(qmfName);
            managedEJB.setClassName(qmfClass);
            managedEJB.setJndiLocation(jndiName);
            managedEJB.setClassLoader(ejbContainer.getClassloader());
            // register on qmf agent
            agent.register(managedEJB);
        }
    }
}
