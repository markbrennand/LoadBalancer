package uk.org.shonky.loadbalancer.engine.policy.impl;

import java.util.List;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.policy.ConnectorPolicy;

import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractPolicy implements ConnectorPolicy {

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
