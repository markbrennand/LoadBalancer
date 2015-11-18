package uk.org.shonky.loadbalancer.dao.impl;

import java.util.Map;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;

@Repository("ConfigurationDAO")
public class ConfigurationDAOImpl implements ConfigurationDAO, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private List<Service> services;
    private Map<String, ConnectorPolicy> policies;

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

    @Override
    public ConnectorPolicy getConnectorPolicy(String name) {
        return policies.get(name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void configure() {
        Map<String, ConnectorPolicy> policyComponents = applicationContext.getBeansOfType(ConnectorPolicy.class);
        policies = newHashMap();

        for (ConnectorPolicy policy : policyComponents.values()) {
            policies.put(policy.getName(), policy);
        }

        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.expiry", "30000");
        props.put("TEST.service.forward.addresses", "localhost:7002");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        Service service = new Service("TEST", config, getConnectorPolicy("Round Robin"));
        services = copyOf(newArrayList(service));
    }
}