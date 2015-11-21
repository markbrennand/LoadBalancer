package uk.org.shonky.loadbalancer.dao;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

public interface ConfigurationDAO {
    public String getName();
    public List<Forwarder> getForwarders();
    public List<ConnectorPolicy> getConnectorPolicies();
}
