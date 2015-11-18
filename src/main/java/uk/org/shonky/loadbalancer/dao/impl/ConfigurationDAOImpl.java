package uk.org.shonky.loadbalancer.dao.impl;

import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Repository;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.PropertiesConfiguration;
import uk.org.shonky.loadbalancer.engine.policy.impl.RoundRobinPolicy;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.ImmutableList.copyOf;

@Repository("ConfigurationDAO")
public class ConfigurationDAOImpl implements ConfigurationDAO {

    @Override
    public List<Service> getServices() {
        Properties props = new Properties();
        props.put("TEST.service.listen.address", "7001");
        props.put("TEST.service.expiry", "30000");
        props.put("TEST.service.forward.addresses", "localhost:7002");
        PropertiesConfiguration config = new PropertiesConfiguration(props);
        Service service = new Service("TEST", config, new RoundRobinPolicy());
        return copyOf(newArrayList(service));
    }
}