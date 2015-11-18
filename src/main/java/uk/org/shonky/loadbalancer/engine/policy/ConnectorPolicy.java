package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

public interface ConnectorPolicy {
    public ConfigurationItem[] getConfigurationItems();
    public Connector newConnector(Service service);

}
