package org.jboss.qmf.core.metadata;

import java.util.Properties;

public class QmfAgentDefinitionsMetaData
{
    private Properties properties;

    public QmfAgentDefinitionsMetaData(Properties properties)
    {
        this.properties = properties;
    }

    public String getBroker()
    {
        return properties.getProperty("broker",
                "amqp://guest:guest@/?brokerlist='tcp://localhost'");
    }

    public String getLabel()
    {
        return properties.getProperty("label", "default_qmf_agent");
    }

    public boolean getSessionTransacted()
    {
        String value = properties.getProperty("sessionTransacted", "false");
        return Boolean.parseBoolean(value);
    }

    public boolean getId()
    {
        String value = properties.getProperty("id", "");
        return Boolean.parseBoolean(value);
    }
}
