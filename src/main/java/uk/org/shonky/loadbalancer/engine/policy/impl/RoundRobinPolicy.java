package uk.org.shonky.loadbalancer.engine.policy.impl;

import org.springframework.stereotype.Component;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Service;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.PolicyException;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component("RoundRobinPolicy")
public class RoundRobinPolicy extends AbstractPolicy {

    public RoundRobinPolicy() {
        super("Round Robin");
    }

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
                        "Address to which a forward connection will be made. Fomrat is host:port",
                        "string",
                        "validateForwardingAddress",
                        true
                )
        };
    }

    @Override
    public Connector newConnector(Service service) {
        String addresses = service.getConfiguration().get("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new PolicyException("Service {0} has no forwarding addresses'", service.getName());
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
