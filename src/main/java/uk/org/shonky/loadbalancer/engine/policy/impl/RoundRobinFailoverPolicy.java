package uk.org.shonky.loadbalancer.engine.policy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;
import uk.org.shonky.loadbalancer.engine.config.Forwarder;
import uk.org.shonky.loadbalancer.engine.policy.Connector;
import uk.org.shonky.loadbalancer.engine.policy.PolicyException;

import static com.google.common.base.Strings.isNullOrEmpty;

@Component("RoundRobinWithFailover")
public class RoundRobinFailoverPolicy extends RoundRobinPolicy {

    @Override
    public Connector newConnector(Forwarder forwarder) {
        String addresses = forwarder.getConfiguration().get("forward.addresses");
        if (isNullOrEmpty(addresses)) {
            throw new PolicyException("Forwarder {0} has no forwarding addresses'", forwarder.getName());
        }

        return new RoundRobinFailoverConnector(parseEndpointList(addresses), getExpiry(forwarder));
    }

    private static class RoundRobinFailoverConnector implements Connector {
        private static final Logger logger = LoggerFactory.getLogger(RoundRobinFailoverConnector.class);

        private Endpoint[] endpoints;
        private int max;
        private int current;
        private long expiry;

        public RoundRobinFailoverConnector(Endpoint[] endpoints, long expiry) {
            this.endpoints = endpoints;
            this.max = endpoints.length;
            this.expiry = expiry;
        }

        @Override
        public synchronized Endpoints nextConnectionEndpoints() {
            if (current == max) {
                current = 0;
            }
            Endpoints nextEndpoints =  new Endpoints(endpoints, current, (current == 0) ? max-1 : current-1);
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
        public long getExpiry() {
            return expiry;
        }
    }
}
