package org.jboss.qmf.core.deployers;

import org.jboss.deployers.plugins.annotations.GenericAnnotationDeployer;
import org.jboss.deployers.plugins.annotations.GenericAnnotationResourceVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.qmf.core.metadata.QmfRegisteredClassesMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.logging.Logger;
import org.jboss.deployment.AnnotationMetaDataDeployer;
import org.apache.qpid.agent.annotations.QMFSeeAlso;
import javassist.ClassPool;

import java.util.Set;
import java.util.HashSet;

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
        setOutput(QmfRegisteredClassesMetaData.class);
    }

    @Override
    protected void visitModule(DeploymentUnit deploymentUnit, Module module, GenericAnnotationResourceVisitor genericAnnotationResourceVisitor) {
        super.visitModule(deploymentUnit, module, genericAnnotationResourceVisitor);

        AnnotationEnvironment annotationEnvironment = genericAnnotationResourceVisitor.getEnv();
        Set<Element<QMFSeeAlso,Class<?>>> qmfSeeAlsoAnnotations = annotationEnvironment.classIsAnnotatedWith(QMFSeeAlso.class);

        QmfRegisteredClassesMetaData metaData = new QmfRegisteredClassesMetaData();

        // create metadata for each class we need to register
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

        // attach metadata to unit
        deploymentUnit.addAttachment(QmfRegisteredClassesMetaData.class, metaData);
    }

}
