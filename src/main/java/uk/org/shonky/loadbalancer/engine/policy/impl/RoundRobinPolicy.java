package uk.org.shonky.loadbalancer.engine.policy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationItem;
import uk.org.shonky.loadbalancer.engine.config.ConfigurationException;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component("RoundRobin")
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
                        "validateForwardingAddresses",
                        true
                )
        };
    }

    @Override
    public Connector newConnector(Forwarder forwarder) {
        String addresses = forwarder.getConfiguration().get("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new ConfigurationException("ForwarderHasNoAddresses", forwarder.getName());
        }

        return new RoundRobinConnector(parseEndpointList(addresses), getExpiry(forwarder));
    }

    private static class RoundRobinConnector implements Connector {
        private static final Logger logger = LoggerFactory.getLogger(RoundRobinConnector.class);

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
            Endpoints nextEndpoints = new Endpoints(endpoints, current, current);
            current++;
            return nextEndpoints;
        }

        @Override
        public void endpointConnected(Endpoint endpoint) {
            logger.info("Endpoint {} connected", endpoint);
        }

        @Override
        public void endpointDisconnected(Endpoint endpoint) {
            logger.info("Endpoint {} disconnected", endpoint);
        }

        @Override
        public void endpointUnavailable(Endpoint endpoint) {
            logger.info("Endpoint {} unavailable", endpoint);
        }

        @Override
        public long getExpiry() {
            return expiry;
        }
    }
}
