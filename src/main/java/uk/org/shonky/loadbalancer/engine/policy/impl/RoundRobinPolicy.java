package uk.org.shonky.loadbalancer.engine.policy.impl;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Service;
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
    public Connector newConnector(Service service) {
        String addresses = service.getConfiguration().get("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new PolicyException("Missing forwarding addresses definition");
        }

        return new RoundRobinConnector(parseEndpointList(addresses), getExpiry(service));
    }

    private static class RoundRobinConnector implements Connector {
        private Endpoint[] endpoints;
        private int max;
        private int current;
        private long expiry;

        public RoundRobinConnector(Endpoint[] endpoints, long expiry) {
            this.endpoints = endpoints;
            this.max = endpoints.length;
            this.expiry = expiry;
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

        @Override
        public long getExpiry() {
            return expiry;
        }
    }
}
