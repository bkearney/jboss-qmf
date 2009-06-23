package org.jboss.qmf.core.deployers;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jboss.qmf.core.metadata.QmfAgentDefinitionsMetaData;
import org.jboss.logging.Logger;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class QmfAgentDefinitionsParserDeployer extends
        AbstractVFSParsingDeployer<QmfAgentDefinitionsMetaData>
{
    private static final Logger log = Logger
            .getLogger(QmfAgentDefinitionsParserDeployer.class);

    public QmfAgentDefinitionsParserDeployer()
    {
        super(QmfAgentDefinitionsMetaData.class);
        setSuffix("qmf-agent.properties");
        log.info("Starting QMF property parser");
    }

    @Override
    protected QmfAgentDefinitionsMetaData parse(VFSDeploymentUnit unit,
            VirtualFile file, QmfAgentDefinitionsMetaData root)
            throws Exception
    {
        Properties properties = new Properties();
        InputStream is = openStreamAndValidate(file);
        try
        {
            properties.load(is);
            for (Object key : properties.keySet())
            {
                log.info("QMF Agent property: " + key + " -> "
                        + properties.get(key));
            }
            return new QmfAgentDefinitionsMetaData(properties);
        } finally
        {
            try
            {
                is.close();
            } catch (IOException ignored)
            {
            }
        }
    }
}
