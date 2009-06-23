package org.jboss.qmf.core.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.MergedJBossMetaDataDeployer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.qmf.core.deployment.QmfAgentDeployment;
import org.jboss.qmf.core.metadata.QmfAgentDefinitionsMetaData;
import org.jboss.qmf.core.metadata.QmfAgentReferencesMetaData;
import org.jboss.qmf.core.metadata.QmfRegisteredClassesMetaData;

public class QmfServiceDeployerEJB extends AbstractComponentDeployer
{
    private static final Logger log = Logger
            .getLogger(QmfServiceDeployerEJB.class);

    public QmfServiceDeployerEJB()
    {
        log.info("Starting Deployer");
        setInput(JBossSessionBeanMetaData.class);
        setInput(QmfAgentReferencesMetaData.class);
    }

    protected QmfAgentDeployment getRootDeployment(DeploymentUnit unit)
    {
        QmfAgentDeployment returnValue = null;
        returnValue = unit.getAttachment(QmfAgentDeployment.class);
        if ((returnValue == null) && (unit.getParent() != null))
        {
            returnValue = getRootDeployment(unit.getParent());
        }
        return returnValue;
    }

    @Override
    public void internalDeploy(DeploymentUnit unit) throws DeploymentException
    {
        // check if qmf agent is defined for this unit if so create & start it's
        // deployment
        QmfAgentDefinitionsMetaData definitionsMetaData = unit
                .getAttachment(QmfAgentDefinitionsMetaData.class);
        QmfAgentDeployment deployment = new QmfAgentDeployment(
                definitionsMetaData);
        if (deployment.isActive())
        {
            // attach deployment to current unit
            unit.addAttachment(QmfAgentDeployment.class, deployment);
        } else
        {
            // Look for parent deployments which already have been deployed.
            deployment = getRootDeployment(unit);
            if (deployment == null)
                return;
        }
        // pull QMF Objects related metadata and process it
        JBossMetaData beans = (JBossMetaData) unit
                .getAttachment(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);
        Ejb3Deployment ejb3Deployment = unit
                .getAttachment(Ejb3Deployment.class);
        QmfAgentReferencesMetaData agentMetadata = unit
                .getAttachment(QmfAgentReferencesMetaData.class);
        if (agentMetadata == null)
            agentMetadata = unit.getParent().getAttachment(
                    QmfAgentReferencesMetaData.class); // metadata usually sits
        // on ear, but here we
        // are on jar level
        deployment.registerQmfObjects(beans, ejb3Deployment, agentMetadata);
        // pull QMF Types related metadata and process it
        deployment.registerQmfTypes(unit
                .getAttachment(QmfRegisteredClassesMetaData.class));
    }

    @Override
    public void internalUndeploy(DeploymentUnit unit)
    {
        QmfAgentDeployment deployment = unit
                .getAttachment(QmfAgentDeployment.class);
        if (deployment == null)
            return;
        log.info("Stopping QMF agent");
        deployment.stop();
        unit.removeAttachment(QmfAgentDeployment.class);
        super.internalUndeploy(unit);
    }
}
