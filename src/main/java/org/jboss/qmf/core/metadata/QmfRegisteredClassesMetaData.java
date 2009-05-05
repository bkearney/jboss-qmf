package org.jboss.qmf.core.metadata;

import java.util.List;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: gmostizk
 * Date: May 4, 2009
 * Time: 10:26:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class QmfRegisteredClassesMetaData {
    private List<Class> annotatedClasses = new LinkedList<Class>();

    public void add(Class<?> annotatedClass) {
        annotatedClasses.add(annotatedClass);
    }

    public List<Class> getAnnotatedClasses() {
        return annotatedClasses;
    }
}
