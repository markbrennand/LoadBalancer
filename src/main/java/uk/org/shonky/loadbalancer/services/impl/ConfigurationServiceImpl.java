package uk.org.shonky.loadbalancer.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

@Service("ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    private ConfigurationDAO configDAO;

    @Override
    public List<Forwarder> getForwarders() {
        return configDAO.getForwarders();
    }

    @Override
    public Forwarder getForwarder(String name) {
        for (Forwarder forwarder : configDAO.getForwarders()) {
            if (name.equals(forwarder.getName())) {
                return forwarder;
            }
        }
        return null;
    }

    @Override
    public List<ConnectorPolicy> getConnectorPolicies() {
        return configDAO.getConnectorPolicies();
    }

    @Override
    public ConnectorPolicy getConnectorPolicy(String name) {
        for (ConnectorPolicy policy : configDAO.getConnectorPolicies()) {
            if (name.equals(policy.getName())) {
                return policy;
            }
        }
        return null;
    }
}
