package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;

public interface ConnectorPolicy {
    public ConfigurationItem[] getConfigurationItems();
    public Connector newConnector(String id, Properties config);

}
