package uk.org.shonky.loadbalancer.dao;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

public interface ConfigurationDAO {
    public List<Service> getServices();
    public List<ConnectorPolicy> getConnectorPolicies();
}
