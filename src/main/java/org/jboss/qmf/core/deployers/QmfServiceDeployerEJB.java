package org.jboss.qmf.core.deployers;

import org.apache.qpid.agent.Agent;
import org.apache.qpid.agent.ManagedEJB;
import org.apache.qpid.agent.annotations.QMFObject;
import org.apache.qpid.agent.annotations.QMFSeeAlso;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.MergedJBossMetaDataDeployer;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.qmf.core.metadata.QmfRegisteredClassesMetaData;

import javax.management.ObjectName;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: gmostizk
 * Date: Apr 26, 2009
 * Time: 4:59:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class QmfServiceDeployerEJB extends AbstractComponentDeployer {
    private static final Logger log = Logger.getLogger(QmfServiceDeployerEJB.class);

    private Agent qmfAgent;
    
    public QmfServiceDeployerEJB() {
        log.warn("Starting Deployer");
        setInput(JBossSessionBeanMetaData.class);
    }

    @Override public void internalDeploy(DeploymentUnit unit) throws DeploymentException {
        registerQmfObjects(unit);

        registerQmfTypes(unit);
    }

    private void registerQmfTypes(DeploymentUnit unit) {
        QmfRegisteredClassesMetaData metadata = unit.getAttachment(QmfRegisteredClassesMetaData.class);
        if(metadata==null) return;

        for (Class classToRegister : metadata.getAnnotatedClasses()) {
            log.info("Exposing "+classToRegister.getSimpleName()+" as QMFType");
            qmfAgent.registerClass(classToRegister);
        }
    }

    private void registerQmfObjects(DeploymentUnit unit) throws DeploymentException {
        JBossMetaData beans = (JBossMetaData) unit.getAttachment(
                MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME
        );
        Ejb3Deployment ejb3Deployment = unit.getAttachment(Ejb3Deployment.class);


        if (beans == null) return;

        for (JBossEnterpriseBeanMetaData m : beans.getEnterpriseBeans()) {
//            log.info("Detected EJB " + m.getEjbClass());

            // extract ejb container
            EJBContainer ejbContainer = null;
            if (ejb3Deployment != null && !m.isEntity()) {
                ObjectName objName = null;
                try {
                    objName = new ObjectName(m.determineContainerName());
                }
                catch (Exception e) {
                    throw new DeploymentException(e);
                }
                ejbContainer = (EJBContainer) ejb3Deployment.getContainer(objName);
            }

            if(ejbContainer!=null) {
                QMFObject annotation = null;
                Class exposedClass = null;

                // get annotation from bean
                annotation = ejbContainer.getAnnotation(QMFObject.class);
                if(annotation!=null) exposedClass=ejbContainer.getBeanClass();

                // try to get it from interfaces
                if(annotation==null) {
                    for(Class localIf : ejbContainer.getBusinessInterfaces()) {
                        annotation = (QMFObject) localIf.getAnnotation(QMFObject.class);
                        exposedClass = localIf;
                        if(annotation!=null) break;
                    }
                }

                // ok lets see if we got QMFObject
                if(annotation!=null) {
                    // expose it
                    String qmfName = annotation.className();
                    String qmfClass = exposedClass.getName();
                    String jndiName = m.getLocalJndiName();
                    log.info("Exposing "+m.getEjbName()+" as QMF Object: "+qmfName+", "+qmfClass+", "+jndiName);

                    ManagedEJB managedEJB = new ManagedEJB();
                    managedEJB.setName(qmfName);
                    managedEJB.setClassName(qmfClass);
                    managedEJB.setJndiLocation(jndiName);

                    // register on qmf agent
                    qmfAgent.register(managedEJB);
                }

            }
        }
    }

    @Override public void internalUndeploy(DeploymentUnit deploymentUnit) {
    }

    public void setQmfAgent(Agent qmfAgent) {
        this.qmfAgent = qmfAgent;
    }
}
