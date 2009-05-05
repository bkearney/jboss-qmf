package org.jboss.qmf.core.metadata;

import java.lang.reflect.Field;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: gmostizk
 * Date: May 5, 2009
 * Time: 12:44:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class QmfAgentReferencesMetaData {
    private Map<Class, Field> refFields = new HashMap<Class, Field>();

    public void add(Field refField) {
        refFields.put(refField.getDeclaringClass(), refField);
    }

    public Map<Class, Field> getRefFields() {
        return refFields;
    }
}
