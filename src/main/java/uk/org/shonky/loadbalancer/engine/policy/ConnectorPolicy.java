package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

public interface ConnectorPolicy {
    public String getName();
    public ConfigurationItem[] getConfigurationItems();
    public Connector newConnector(Forwarder forwarder);
}
