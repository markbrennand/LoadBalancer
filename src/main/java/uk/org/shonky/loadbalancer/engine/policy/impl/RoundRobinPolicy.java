package uk.org.shonky.loadbalancer.engine.policy.impl;

import java.util.Properties;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.PolicyException;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

import static com.google.common.base.Strings.isNullOrEmpty;

public class RoundRobinPolicy extends AbstractPolicy {

    @Override
    public ConfigurationItem[] getConfigurationItems() {
        throw new PolicyException("Not yet implemented");
    }

    @Override
    public Connector newConnector(Properties config) {
        String addresses = config.getProperty("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new ConfigurationException("Missing forwarding addresses definition");
        }

        return new RoundRobinConnector(parseEndpointList(addresses));
    }

    private static class RoundRobinConnector implements Connector {
        private Endpoint[] endpoints;
        private int max;
        private int current;

        public RoundRobinConnector(Endpoint[] endpoints) {
            this.endpoints = endpoints;
            this.max = endpoints.length;
        }

        @Override
        public synchronized Endpoint nextEndpoint() {
            if (current == max) {
                current = 0;
            }
            return endpoints[current++];
        }

        @Override
        public void endpointClosed(Endpoint endpoint) {
        }
    }
}
