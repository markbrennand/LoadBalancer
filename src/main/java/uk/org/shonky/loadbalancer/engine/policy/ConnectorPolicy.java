package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

import java.util.Properties;

public interface ConnectorPolicy {
    public ConfigurationItem[] getConfigurationItems();
    public Connector newConnector(Properties config);

}
