package uk.org.shonky.loadbalancer.dao.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.base.Strings.isNullOrEmpty;

@Repository("ConfigurationDAO")
public class PropertiesConfigurationDAOImpl implements ConfigurationDAO {
    private List<Service> services;
    private Map<String, ConnectorPolicy> policies;

    @Autowired
    public PropertiesConfigurationDAOImpl(List<ConnectorPolicy> policies) {
        this.policies = newHashMap();
        for (ConnectorPolicy policy : policies) {
            this.policies.put(policy.getName(), policy);
        }

        String configPath = System.getProperty("loadbalancer.config.path");
        if (isNullOrEmpty(configPath)) {
            configPath = new StringBuffer(System.getProperty("user.home")).
                    append(File.separator).
                    append("LoadBalancer.properties").
                    toString();
        }

        InputStream in = null;
        Properties properties = new Properties();
        try {
            in = new FileInputStream(configPath);
            properties.load(in);
        } catch(IOException ioe) {
            throw new ConfigurationException("Failed to load configuration file {0}", configPath);
        } finally{
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }

        PropertiesConfiguration config = new PropertiesConfiguration(properties);
        List<Service> serviceList = newArrayList();
        for (String name : getServiceNames(properties)) {
            serviceList.add(new Service(name, config));
        }
        services = copyOf(serviceList);
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
        for (Service service : services) {
            service.initialiseConnector(policies.get(service.getConnectorPolicyName()));
        }
    }

    private Set<String> getServiceNames(Properties properties) {
        Set<String> names = newHashSet();
        for (String key : properties.stringPropertyNames()) {
            int split = key.indexOf(".service.");
            if (split > 0) {
                names.add(key.substring(0, split));
            }
        }
        return names;
    }
}