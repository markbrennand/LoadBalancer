package uk.org.shonky.loadbalancer.services.impl;

import java.util.Map;
import java.util.List;
import java.lang.annotation.Annotation;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.util.DefaultImplementation;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.services.ConfigurationService;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.base.Preconditions.checkNotNull;

@Service("ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {
    private Map<String, ConfigurationDAO> configDaoMap;
    private ConfigurationDAO configDao;

    @Autowired
    public ConfigurationServiceImpl(List<ConfigurationDAO> configDaoList) {
        configDaoMap = newHashMap();
        for (ConfigurationDAO dao : configDaoList) {
            for (Annotation annotation : dao.getClass().getAnnotations()) {
                if (annotation instanceof DefaultImplementation) {
                    if (configDao != null) {
                        throw new ConfigurationException("ConfigurationDaoTooManyDefaults");
                    } else {
                        configDao = dao;
                    }
                }
            }
            configDaoMap.put(dao.getName(), dao);
        }
        if (configDao == null) {
            throw new ConfigurationException("ConfigurationDaoNoDefault");
        }
    }

    @Override
    public List<Forwarder> getForwarders() {
        return configDao.getForwarders();
    }

    @Override
    public Forwarder getForwarder(String name) {
        for (Forwarder forwarder : configDao.getForwarders()) {
            if (name.equals(forwarder.getName())) {
                return forwarder;
            }
        }
        throw new ConfigurationException("ForwarderNotFound", name);
    }

    @Override
    public List<ConnectorPolicy> getConnectorPolicies() {
        return configDao.getConnectorPolicies();
    }

    @Override
    public ConnectorPolicy getConnectorPolicy(String name) {
        for (ConnectorPolicy policy : configDao.getConnectorPolicies()) {
            if (name.equals(policy.getName())) {
                return policy;
            }
        }
        return null;
    }

    @Override
    public void setConfigurationDAO(String name) {
        configDao = configDaoMap.get(checkNotNull(name));
        if (configDao == null) {
            throw new ConfigurationException("ConfigurationDaoNotFound", name);
        }
    }
}
