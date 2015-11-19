package uk.org.shonky.loadbalancer.engine.policy.impl;

import org.springframework.stereotype.Component;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.PolicyException;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component("Round Robin Policy")
public class RoundRobinPolicy extends AbstractPolicy {

    @Override
    public ConfigurationItem[] getConfigurationItems() {
        return new ConfigurationItem[] {
                new ConfigurationItem(
                        "listen.address",
                        "Listening Address",
                        "Address and port on which service will listen. Format is (host:)port",
                        "string",
                        "validateListeningAddress",
                        false),
                new ConfigurationItem(
                        "forward.addresses",
                        "Forwarding Addresses",
                        "Address to which a forward connection will be made. Format is host:port",
                        "string",
                        "validateForwardingAddress",
                        true
                )
        };
    }

    @Override
    public Connector newConnector(Forwarder forwarder) {
        String addresses = forwarder.getConfiguration().get("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new PolicyException("Forwarder {0} has no forwarding addresses'", forwarder.getName());
        }

        return new RoundRobinConnector(parseEndpointList(addresses), getExpiry(forwarder));
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
        public synchronized Endpoints nextConnectionEndpoints() {
            if (current == max) {
                current = 0;
            }
            Endpoints nextEndpoints =  new Endpoints(endpoints, current, current);
            current++;
            return nextEndpoints;
        }

        @Override
        public void endpointConnected(Endpoint endpoint) {
        }

        @Override
        public void endpointDisconnected(Endpoint endpoint) {
        }

        @Override
        public long getExpiry() {
            return expiry;
        }
    }
}
