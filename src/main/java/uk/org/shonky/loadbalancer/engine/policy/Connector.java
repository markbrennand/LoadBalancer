package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;

public interface Connector {
    public Endpoint nextEndpoint();
    public void endpointClosed(Endpoint endpoint);
    public long getExpiry();
}