package uk.org.shonky.loadbalancer.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

@Repository("ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    private ConfigurationDAO configDAO;

    @Override
    public List<Service> getServices() {
        return configDAO.getServices();
    }

    @Override
    public Service getService(String name) {
        for (Service service : configDAO.getServices()) {
            if (name.equals(service.getName())) {
                return service;
            }
        }
        return null;
    }
}
