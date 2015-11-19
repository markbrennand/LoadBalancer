package uk.org.shonky.loadbalancer.dao.impl;

import java.util.Map;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;

@Repository("ConfigurationDAO")
public class ConfigurationDAOImpl implements ConfigurationDAO {
    private List<Service> services;
    private Map<String, ConnectorPolicy> policies;

    @Autowired
    public ConfigurationDAOImpl(List<ConnectorPolicy> policies) {
        this.policies = newHashMap();
        for (ConnectorPolicy policy : policies) {
            this.policies.put(policy.getName(), policy);
        }
    }

    @Override
    public List<Service> getServices() {
        return services;
    }

    @Override
    public List<ConnectorPolicy> getConnectorPolicies() {
        List<ConnectorPolicy> retList = newArrayList();
        for (ConnectorPolicy policy : policies.values()) {
            retList.add(policy);
        }
        return copyOf(retList);
    }

    @PostConstruct
    public void configure() {
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.expiry", "30000");
        props.put("TEST.service.forward.addresses", "localhost:7002");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        Service service = new Service("TEST", config, policies.get("Round Robin Policy"));
        services = copyOf(newArrayList(service));
    }
}