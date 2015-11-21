package uk.org.shonky.loadbalancer.services;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

public interface ConfigurationService {
    public List<Forwarder> getForwarders();
    public Forwarder getForwarder(String name);
    public List<ConnectorPolicy> getConnectorPolicies();
    public ConnectorPolicy getConnectorPolicy(String name);
    public void setConfigurationDAO(String name);
}
