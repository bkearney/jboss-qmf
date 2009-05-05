package org.jboss.qmf.core.deployers;

import org.apache.qpid.agent.annotations.QMFAgent;
import org.apache.qpid.agent.annotations.QMFEvent;
import org.apache.qpid.agent.annotations.QMFSeeAlso;
import org.apache.qpid.agent.annotations.QMFType;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.plugins.annotations.GenericAnnotationDeployer;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.qmf.core.metadata.QmfAgentReferencesMetaData;
import org.jboss.qmf.core.metadata.QmfRegisteredClassesMetaData;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: gmostizk
 * Date: Apr 27, 2009
 * Time: 6:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class QmfTypeAnnotationDeployer extends GenericAnnotationDeployer {
    private static final Logger log = Logger.getLogger(QmfTypeAnnotationDeployer.class);

    public QmfTypeAnnotationDeployer() {
        log.info("Initalizing "+this.getClass().getSimpleName());
        addInput(JBossMetaData.class);
        setOutputs(new HashSet<String>());
        addOutput(QmfRegisteredClassesMetaData.class);
        addOutput(QmfAgentReferencesMetaData.class);
    }

    @Override
    protected void visitModule(DeploymentUnit deploymentUnit, Module module, GenericAnnotationResourceVisitor genericAnnotationResourceVisitor) {
        super.visitModule(deploymentUnit, module, genericAnnotationResourceVisitor);

        AnnotationEnvironment annotationEnvironment = genericAnnotationResourceVisitor.getEnv();

        createQmfTypesRegistrationMetadata(deploymentUnit, annotationEnvironment);
        createQmfAgentReferencesMetadata(deploymentUnit, annotationEnvironment);
    }

    private void createQmfAgentReferencesMetadata(DeploymentUnit deploymentUnit, AnnotationEnvironment annotationEnvironment) {
        QmfAgentReferencesMetaData referencesMetadata = new QmfAgentReferencesMetaData();

        Set<Element<QMFAgent, Field>> agentReferenceAnnotations = annotationEnvironment.classHasFieldAnnotatedWith(QMFAgent.class);
        for (Element<QMFAgent, Field> agentReferenceAnnotation : agentReferenceAnnotations) {
            Field refField = agentReferenceAnnotation.getAnnotatedElement();
            referencesMetadata.add(refField);
        }

        deploymentUnit.addAttachment(QmfAgentReferencesMetaData.class, referencesMetadata);
    }

    private void createQmfTypesRegistrationMetadata(DeploymentUnit deploymentUnit, AnnotationEnvironment annotationEnvironment) {
        QmfRegisteredClassesMetaData metaData = new QmfRegisteredClassesMetaData();

        // create metadata for each class marked with QMFSeeAlso we need to register
        Set<Element<QMFSeeAlso,Class<?>>> qmfSeeAlsoAnnotations = annotationEnvironment.classIsAnnotatedWith(QMFSeeAlso.class);
        for (Element<QMFSeeAlso, Class<?>> qmfSeeAlsoAnnotation : qmfSeeAlsoAnnotations) {

            // add the root class
            Class<?> annotatedClass = qmfSeeAlsoAnnotation.getAnnotatedElement();
            metaData.add(annotatedClass);

            // add all the children
            Class[] subClasses = qmfSeeAlsoAnnotation.getAnnotation().value();
            for (Class subClass : subClasses) {
                metaData.add(subClass);
            }
        }

        // create metadata for each class marked with QMFType we need to register
        Set<Element<QMFType, Class<?>>> qmfTypeAnnotations = annotationEnvironment.classIsAnnotatedWith(QMFType.class);
        for (Element<QMFType, Class<?>> qmfTypeAnnotation : qmfTypeAnnotations) {
            metaData.add(qmfTypeAnnotation.getAnnotatedElement());
        }

        // create metadata for each class marked with QMFEvent we need to register
        Set<Element<QMFEvent, Class<?>>> qmfEventAnnotations = annotationEnvironment.classIsAnnotatedWith(QMFEvent.class);
        for (Element<QMFEvent, Class<?>> qmfEventAnnotation : qmfEventAnnotations) {
            metaData.add(qmfEventAnnotation.getAnnotatedElement());
        }

        // attach metadata to unit
        deploymentUnit.addAttachment(QmfRegisteredClassesMetaData.class, metaData);
    }

}
