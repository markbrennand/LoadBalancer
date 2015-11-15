package uk.org.shonky.loadbalancer.engine.config;

import java.util.Properties;

public interface Connector {
    public void configure(Properties configuration);
    public Endpoint nextEndpoint();
    public void endpointClosed(Endpoint endpoint);
    public ConfigurationItem[] getConfigurationItems();
}
