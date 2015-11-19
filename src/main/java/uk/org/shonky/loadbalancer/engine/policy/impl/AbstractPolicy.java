package uk.org.shonky.loadbalancer.engine.policy.impl;

import java.util.Map;
import java.util.List;
import java.lang.annotation.Annotation;

import org.springframework.stereotype.Component;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;
import uk.org.shonky.loadbalancer.engine.policy.PolicyException;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractPolicy implements ConnectorPolicy {
    private String name;

    public AbstractPolicy() {
        for (Annotation annotation : getClass().getAnnotations()) {
            if (annotation instanceof Component) {
                name = ((Component) annotation).value();
                break;
            }
        }

        if (name == null) {
            throw new PolicyException("Policy {} does not have a Component annotation", getClass().getName());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    protected long getExpiry(Service service) {
        String value = service.getConfiguration().get("expiry");
        if (isNullOrEmpty(value)) {
            throw new ConfigurationException("Service {0} has no expiry definition", service.getName());
        }

        try {
            return Long.parseLong(value);
        } catch(NumberFormatException nfe) {
            throw new ConfigurationException("Service {0} has non numeric expiry", service.getName());
        }
    }

    protected Endpoint parseEndpoint(String value) {
        return Endpoint.parse(value, false);
    }

    protected Endpoint[] parseEndpointList(String values) {
        List<Endpoint> endpoints = newArrayList();
        for (String value : values.split(",")) {
            endpoints.add(parseEndpoint(value));
        }
        Endpoint[] array = new Endpoint[endpoints.size()];
        endpoints.toArray(array);
        return array;
    }
}
