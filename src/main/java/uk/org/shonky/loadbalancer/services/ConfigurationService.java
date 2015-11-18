package uk.org.shonky.loadbalancer.services;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Service;

public interface ConfigurationService {
    public List<Service> getServices();
    public Service getService(String name);
}
