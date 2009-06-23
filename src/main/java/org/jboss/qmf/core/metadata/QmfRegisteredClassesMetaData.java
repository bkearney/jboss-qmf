package org.jboss.qmf.core.metadata;

import java.util.LinkedList;
import java.util.List;

public class QmfRegisteredClassesMetaData
{
    private List<Class> annotatedClasses = new LinkedList<Class>();

    public void add(Class<?> annotatedClass)
    {
        annotatedClasses.add(annotatedClass);
    }

    public List<Class> getAnnotatedClasses()
    {
        return annotatedClasses;
    }
}
