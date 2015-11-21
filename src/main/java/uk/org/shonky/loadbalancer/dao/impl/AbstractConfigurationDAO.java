package uk.org.shonky.loadbalancer.dao.impl;

import java.util.List;
import java.util.Map;
import java.lang.annotation.Annotation;

import org.springframework.stereotype.Repository;

import uk.org.shonky.loadbalancer.dao.ConfigurationDAO;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.ImmutableList.copyOf;

public abstract class AbstractConfigurationDAO implements ConfigurationDAO {
    protected Map<String, ConnectorPolicy> policies;
    private String name;

    public AbstractConfigurationDAO(List<ConnectorPolicy> policies) {
        this.policies = newHashMap();
        for (ConnectorPolicy policy : policies) {
            this.policies.put(policy.getName(), policy);
        }

        for (Annotation annotation : getClass().getAnnotations()) {
            if (annotation instanceof Repository) {
                name = ((Repository) annotation).value();
            }
        }

        if (name == null) {
            throw new ConfigurationException("Configuration implementation {0] is missing Repository annotation",
                    getClass().getName());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ConnectorPolicy> getConnectorPolicies() {
        List<ConnectorPolicy> retList = newArrayList();
        for (ConnectorPolicy policy : policies.values()) {
            retList.add(policy);
        }
        return copyOf(retList);
    }
}
