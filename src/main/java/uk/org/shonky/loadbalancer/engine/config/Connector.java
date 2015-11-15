package uk.org.shonky.loadbalancer.engine.config;

import java.net.InetAddress;

public interface Connector {
    public Endpoint nextEndpoint();
    public void endpointClosed(Endpoint endpoint);
}