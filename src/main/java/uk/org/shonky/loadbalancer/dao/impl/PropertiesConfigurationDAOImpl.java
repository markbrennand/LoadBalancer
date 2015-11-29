package uk.org.shonky.loadbalancer.dao.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.util.DefaultImplementation;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.base.Strings.isNullOrEmpty;

@DefaultImplementation
@Repository("PropertiesConfiguration")
public class PropertiesConfigurationDAOImpl extends AbstractConfigurationDAO {
    private final static String CONFIG_FILENAME = "LoadBalancer.properties";

    private List<Forwarder> forwarders;

    @Autowired
    public PropertiesConfigurationDAOImpl(List<ConnectorPolicy> policies) {
        super(policies);
    }

    @Override
    public List<Forwarder> getForwarders() {
        if (forwarders == null) {
            load();
        }
        return forwarders;
    }

    private void load() {
        String configPath = System.getProperty("loadbalancer.config.path");
        if (isNullOrEmpty(configPath)) {
            configPath = new StringBuffer(System.getProperty("user.home")).
                    append(File.separator).
                    append(CONFIG_FILENAME).
                    toString();
        }

        Properties properties = new Properties();
        if (new File(configPath).exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(configPath);
                properties.load(in);
            } catch(IOException ioe) {
                throw new ConfigurationException("PropertiesConfigurationLoadFailed", configPath);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        } else {
            InputStream in = getClass().getClassLoader().getParent().getResourceAsStream(CONFIG_FILENAME);
            if (in == null) {
                throw new ConfigurationException("PropertiesConfigurationNotFound");
            }

            try {
                properties.load(in);
            } catch(IOException ioe) {
                throw new ConfigurationException("PropertiesConfigurationResourceLoadFailed");
            }
        }

        PropertiesConfiguration config = new PropertiesConfiguration(properties);
        List<Forwarder> forwarderList = newArrayList();
        for (String name : getForwardereNames(properties)) {
            forwarderList.add(new Forwarder(name, config));
        }
        this.forwarders = copyOf(forwarderList);

        for (Forwarder forwarder : forwarders) {
            ConnectorPolicy policy = policies.get(forwarder.getConnectorPolicyName());
            if (policy == null) {
                throw new ConfigurationException("NoSuchConnectorPolicy", forwarder.getConnectorPolicyName());
            }
            forwarder.initialiseConnector(policy);
        }
    }

    private Set<String> getForwardereNames(Properties properties) {
        Set<String> names = newHashSet();
        for (String key : properties.stringPropertyNames()) {
            int split = key.indexOf(".forwarder.");
            if (split > 0) {
                names.add(key.substring(0, split));
            }
        }
        return names;
    }
}