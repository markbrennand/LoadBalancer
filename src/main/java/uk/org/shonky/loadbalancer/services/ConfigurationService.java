package uk.org.shonky.loadbalancer.services;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

public interface ConfigurationService {
    public List<Service> getServices();
    public Service getService(String name);
    public List<ConnectorPolicy> getConnectorPolicies();
    public ConnectorPolicy getConnectorPolicy(String name);
}
