package uk.org.shonky.loadbalancer.engine.policy;

import uk.org.shonky.loadbalancer.engine.config.Endpoint;
import uk.org.shonky.loadbalancer.engine.config.Endpoints;

public interface Connector {
    public Endpoints nextConnectionEndpoints();
    public void endpointConnected(Endpoint endpoint);
    public void endpointDisconnected(Endpoint endpoint);
    public long getExpiry();
}