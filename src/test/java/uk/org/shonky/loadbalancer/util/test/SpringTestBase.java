package uk.org.shonky.loadbalancer.util.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.stereotype.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;
import uk.org.shonky.loadbalancer.dao.impl.AbstractConfigurationDAO;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public abstract class SpringTestBase {
    @Autowired
    protected ConfigurationService configurationService;

    @Before
    public void initialise() {
        configurationService.setConfigurationDAO("TestConfiguration");
    }

    @Repository("TestConfiguration")
    private static class TestConfigurationDAO extends AbstractConfigurationDAO {
        private List<Forwarder> forwarders;

        @Autowired
        public TestConfigurationDAO(List<ConnectorPolicy> policies) {
            super(policies);

            Properties properties = new Properties();
            InputStream in = getClass().getClassLoader().getResourceAsStream("LoadBalancerTest.properties");
            if (in == null) {
                throw new ConfigurationException("PropertiesConfigurationNotFound");
            }

            try {
                properties.load(in);
            } catch(IOException ioe) {
                throw new ConfigurationException("PropertiesConfigurationResourceLoadFailed");
            }

            PropertiesConfiguration config = new PropertiesConfiguration(properties);
            List<Forwarder> forwarderList = newArrayList();
            for (String name : getForwardereNames(properties)) {
                forwarderList.add(new Forwarder(name, config));
            }
            this.forwarders = copyOf(forwarderList);

            for (Forwarder forwarder : forwarders) {
                forwarder.initialiseConnector(this.policies.get(forwarder.getConnectorPolicyName()));
            }
        }

        @Override
        public List<Forwarder> getForwarders() {
            return forwarders;
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
}