package org.jboss.qmf.core.metadata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class QmfAgentReferencesMetaData
{
    private Map<Class, Field> refFields = new HashMap<Class, Field>();

    public void add(Field refField)
    {
        refFields.put(refField.getDeclaringClass(), refField);
    }

    public Map<Class, Field> getRefFields()
    {
        return refFields;
    }
}
