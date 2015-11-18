package uk.org.shonky.loadbalancer.dao;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Service;

public interface ConfigurationDAO {
    public List<Service> getServices();
}
